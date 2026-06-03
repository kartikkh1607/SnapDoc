package com.kartik.snapdoc.domain.pipeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.media.ExifInterface
import androidx.core.net.toFile
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ProcessingOutcome {
    data class Success(val entry: ProcessingResultStore.Entry) : ProcessingOutcome
    data class Failure(val message: String) : ProcessingOutcome
}

@Singleton
class PhotoProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backgroundRemover: BackgroundRemover,
    private val backgroundCompositor: BackgroundCompositor,
    private val faceCropper: FaceCropper,
    private val imageResizer: ImageResizer,
    private val compressor: FileSizeCompressor,
    private val validator: SpecValidator,
    private val resultStore: ProcessingResultStore,
) {

    private val _stage = MutableStateFlow(ProcessingStage.DetectingFace)
    val stage: StateFlow<ProcessingStage> = _stage.asStateFlow()

    private val processMutex = Mutex()

    suspend fun process(sourceUri: Uri, spec: DocumentSpec): ProcessingOutcome = processMutex.withLock {
      withContext(Dispatchers.Default) {
        try {
            _stage.value = ProcessingStage.DetectingFace
            ensureActive()
            val source = decodeOriented(sourceUri) ?: return@withContext ProcessingOutcome.Failure("Couldn't read photo")

            _stage.value = ProcessingStage.RemovingBackground
            ensureActive()
            val removed = backgroundRemover.remove(source)

            _stage.value = ProcessingStage.ApplyingBackground
            ensureActive()
            val composited = backgroundCompositor.composite(removed, spec.background)
            if (composited !== source) source.recycle()

            _stage.value = ProcessingStage.Cropping
            ensureActive()
            val cropResult = faceCropper.cropToSpec(composited, spec)
            val cropped = when (cropResult) {
                is CropResult.Success -> cropResult.bitmap.also { if (it !== composited) composited.recycle() }
                is CropResult.Failure -> {
                    composited.recycle()
                    return@withContext ProcessingOutcome.Failure(cropResult.reason)
                }
            }

            _stage.value = ProcessingStage.Resizing
            ensureActive()
            val resized = imageResizer.resize(cropped, spec.dimensions.widthPx, spec.dimensions.heightPx)
            if (resized !== cropped) cropped.recycle()

            _stage.value = ProcessingStage.Compressing
            ensureActive()
            val compressed = compressor.compressToTarget(resized, spec.file.minSizeKb, spec.file.maxSizeKb)
            resized.recycle()

            _stage.value = ProcessingStage.Validating
            ensureActive()
            // Validate against the actual saved bytes — the compressor may have
            // downscaled `resized` past us when the size target was too tight,
            // so decoding the final JPEG is the only way to validate truth.
            val finalBitmap = BitmapFactory.decodeByteArray(compressed.bytes, 0, compressed.bytes.size)
                ?: return@withContext ProcessingOutcome.Failure("Couldn't decode compressed photo")
            val validation = try {
                validator.validate(finalBitmap, compressed.sizeKb, spec)
            } finally {
                finalBitmap.recycle()
            }

            val outFile = File(context.cacheDir, "snapdoc_processed_${spec.id}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { it.write(compressed.bytes) }

            val entry = ProcessingResultStore.Entry(
                processedUri = Uri.fromFile(outFile),
                rawJpegBytes = compressed.bytes,
                sizeKb = compressed.sizeKb,
                widthPx = compressed.widthPx,
                heightPx = compressed.heightPx,
                validation = validation,
            )
            resultStore.put(entry)

            _stage.value = ProcessingStage.Done
            ProcessingOutcome.Success(entry)
        } catch (ce: CancellationException) {
            // Honor cancellation — the caller's coroutine is gone.
            throw ce
        } catch (t: Throwable) {
            ProcessingOutcome.Failure(t.message ?: "Processing failed")
        }
      }
    }

    private fun decodeOriented(uri: Uri): Bitmap? {
        val stream = openStream(uri) ?: return null
        val raw = stream.use { BitmapFactory.decodeStream(it) } ?: return null

        val orientation = runCatching {
            openStream(uri)?.use { ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            ) }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (rotation == 0f) return raw
        val matrix = Matrix().apply { postRotate(rotation) }
        val rotated = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
        if (rotated !== raw) raw.recycle()
        return rotated
    }

    private fun openStream(uri: Uri) = when (uri.scheme) {
        "file", null -> runCatching { uri.toFile().inputStream() }.getOrNull()
        else -> runCatching { context.contentResolver.openInputStream(uri) }.getOrNull()
    }
}
