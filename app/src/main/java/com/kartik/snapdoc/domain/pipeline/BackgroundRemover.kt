package com.kartik.snapdoc.domain.pipeline

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class RemovedBackground(
    val source: Bitmap,
    val mask: SegmentationMask,
)

@Singleton
class BackgroundRemover @Inject constructor() : Closeable {

    // Default-size mask. enableRawSizeMask() was producing dead masks on some
    // devices with the 16.0.0-beta6 selfie segmenter — sticking with the
    // built-in downsampled mode is more reliable, and the compositor scales it
    // back up via bilinear interpolation anyway.
    private val segmenter = Segmentation.getClient(
        SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .build(),
    )

    suspend fun remove(bitmap: Bitmap): RemovedBackground = suspendCancellableCoroutine { cont ->
        // ML Kit's selfie segmenter is documented as working best at <= ~1920px.
        // Larger images sometimes come back with completely empty masks on
        // older devices. Downscale a working copy before handing it to ML Kit;
        // the caller keeps the full-res original via [RemovedBackground.source].
        val downscaled = downscaleForMlKit(bitmap)
        val input = InputImage.fromBitmap(downscaled, 0)
        Log.d(
            TAG,
            "segmenting bitmap=${bitmap.width}x${bitmap.height} as ${downscaled.width}x${downscaled.height}",
        )
        segmenter.process(input)
            .addOnSuccessListener { mask ->
                if (downscaled !== bitmap) downscaled.recycle()
                cont.resume(RemovedBackground(bitmap, mask))
            }
            .addOnFailureListener {
                if (downscaled !== bitmap) downscaled.recycle()
                cont.resumeWithException(it)
            }
    }

    private fun downscaleForMlKit(source: Bitmap): Bitmap {
        val longest = maxOf(source.width, source.height)
        if (longest <= ML_KIT_MAX_DIMENSION) return source
        val scale = ML_KIT_MAX_DIMENSION.toFloat() / longest
        val newW = (source.width * scale).toInt().coerceAtLeast(1)
        val newH = (source.height * scale).toInt().coerceAtLeast(1)
        val matrix = Matrix().apply { postScale(scale, scale) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    override fun close() {
        segmenter.close()
    }

    private companion object {
        const val TAG = "BackgroundRemover"
        // ML Kit selfie segmentation is documented as targeting up to ~1920px.
        // We use a slightly tighter bound to leave headroom for any model.
        const val ML_KIT_MAX_DIMENSION = 1600
    }
}
