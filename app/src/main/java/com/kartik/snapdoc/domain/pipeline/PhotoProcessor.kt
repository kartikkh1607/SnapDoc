package com.kartik.snapdoc.domain.pipeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.media.ExifInterface
import androidx.core.net.toFile
import java.io.ByteArrayInputStream
import com.kartik.snapdoc.data.monitoring.CrashReporter
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
    data class Failure(val reason: PipelineFailureReason) : ProcessingOutcome
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
    private val crashReporter: CrashReporter,
) {

    private val _stage = MutableStateFlow(ProcessingStage.DetectingFace)
    val stage: StateFlow<ProcessingStage> = _stage.asStateFlow()

    private val processMutex = Mutex()

    suspend fun process(sourceUri: Uri, spec: DocumentSpec): ProcessingOutcome = processMutex.withLock {
      withContext(Dispatchers.Default) {
        try {
            _stage.value = ProcessingStage.DetectingFace
            ensureActive()
            val source = decodeOriented(sourceUri)
                ?: return@withContext ProcessingOutcome.Failure(PipelineFailureReason.SourceUnreadable)

            // Detect on the untouched source. Running detection on the post-
            // segmentation composite (the previous behavior) caused ML Kit to
            // miss faces because mask edges can subtly distort facial geometry.
            // Doing it here also lets us fail fast before the expensive
            // segmentation step when no face is in frame.
            val face = faceCropper.detect(source)
            if (face == null) {
                source.recycle()
                return@withContext ProcessingOutcome.Failure(PipelineFailureReason.NoFaceDetected)
            }

            _stage.value = ProcessingStage.RemovingBackground
            ensureActive()
            // Segmentation can fail (ML Kit init issue, weird lighting, etc.).
            // If it does, we don't lose the whole photo — fall back to the
            // original source bitmap so the user still gets a cropped portrait.
            val removed = runCatching { backgroundRemover.remove(source) }
                .onFailure { android.util.Log.w(TAG, "Selfie segmentation failed", it) }
                .getOrNull()

            _stage.value = ProcessingStage.ApplyingBackground
            ensureActive()
            val composited = if (removed != null) {
                runCatching { backgroundCompositor.composite(removed, spec.background) }
                    .onFailure { android.util.Log.w(TAG, "Background composite failed", it) }
                    .getOrNull()
            } else null

            // Trust the composite if we got one. The previous secondary
            // face-detect check was rejecting valid composites just because
            // ML Kit's face model gets confused by uniform backgrounds,
            // which forced every photo into the no-bg-replacement fallback.
            val cropBase = if (composited != null) {
                if (composited !== source) source.recycle()
                composited
            } else {
                source
            }

            _stage.value = ProcessingStage.Cropping
            ensureActive()
            // Face coords from `face` are valid for cropBase — both source and
            // composite share the same dimensions.
            val cropResult = faceCropper.cropToSpec(cropBase, face, spec)
            val cropped = when (cropResult) {
                is CropResult.Success -> cropResult.bitmap.also { if (it !== cropBase) cropBase.recycle() }
                is CropResult.Failure -> {
                    cropBase.recycle()
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
                ?: return@withContext ProcessingOutcome.Failure(PipelineFailureReason.FinalDecodeFailed)
            val validation = try {
                validator.validate(finalBitmap, compressed.sizeKb, spec)
            } finally {
                finalBitmap.recycle()
            }

            val outFile = File(context.cacheDir, "snapdoc_processed_${spec.id}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { it.write(compressed.bytes) }

            val entry = ProcessingResultStore.Entry(
                processedUri = Uri.fromFile(outFile),
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
            crashReporter.setKey("pipeline_stage", _stage.value.name)
            crashReporter.setKey("spec_id", spec.id)
            crashReporter.recordException(t, message = "PhotoProcessor failed at ${_stage.value}")
            ProcessingOutcome.Failure(PipelineFailureReason.Unexpected(t.message))
        }
      }
    }

    private fun decodeOriented(uri: Uri): Bitmap? {
        // Buffer the URI once: content URIs from other apps (e.g. Google Photos)
        // do not guarantee a second openInputStream() returns the same bytes.
        val bytes = readAllBytes(uri) ?: return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = computeInSampleSize(bounds.outWidth, bounds.outHeight, MAX_INPUT_DIMENSION_PX)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts) ?: return null

        val orientation = runCatching {
            ByteArrayInputStream(bytes).use { ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            ) }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = exifMatrix(orientation) ?: return raw
        val oriented = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
        if (oriented !== raw) raw.recycle()
        return oriented
    }

    private fun exifMatrix(orientation: Int): Matrix? {
        val m = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { m.postRotate(90f); m.preScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> { m.postRotate(270f); m.preScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
            else -> return null
        }
        return m
    }

    private fun computeInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var sample = 1
        val longest = maxOf(width, height)
        while (longest / sample > maxDim) sample *= 2
        return sample
    }

    private fun readAllBytes(uri: Uri): ByteArray? {
        val stream = when (uri.scheme) {
            "file", null -> runCatching { uri.toFile().inputStream() }.getOrNull()
            else -> runCatching { context.contentResolver.openInputStream(uri) }.getOrNull()
        } ?: return null
        return runCatching { stream.use { it.readBytes() } }.getOrNull()
    }

    private companion object {
        const val TAG = "PhotoProcessor"
        // Caps decoded input at ~4096px longest side to keep peak ARGB_8888 allocation
        // under ~67MB even from 50MP camera shots.
        const val MAX_INPUT_DIMENSION_PX = 4096
    }
}
