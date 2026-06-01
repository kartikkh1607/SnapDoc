package com.kartik.snapdoc.domain.pipeline

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class RemovedBackground(
    val source: Bitmap,
    val mask: SegmentationMask,
)

@Singleton
class BackgroundRemover @Inject constructor() {

    private val segmenter = Segmentation.getClient(
        SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .enableRawSizeMask()
            .build(),
    )

    suspend fun remove(bitmap: Bitmap): RemovedBackground = suspendCancellableCoroutine { cont ->
        val input = InputImage.fromBitmap(bitmap, 0)
        segmenter.process(input)
            .addOnSuccessListener { mask -> cont.resume(RemovedBackground(bitmap, mask)) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    fun close() {
        segmenter.close()
    }
}
