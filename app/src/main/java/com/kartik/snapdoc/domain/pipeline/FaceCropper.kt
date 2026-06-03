package com.kartik.snapdoc.domain.pipeline

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.roundToInt

sealed interface CropResult {
    data class Success(val bitmap: Bitmap) : CropResult
    data class Failure(val reason: String) : CropResult
}

@Singleton
class FaceCropper @Inject constructor() : Closeable {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setMinFaceSize(0.15f)
            .build(),
    )

    suspend fun cropToSpec(composited: Bitmap, spec: DocumentSpec): CropResult {
        val face = detect(composited) ?: return CropResult.Failure("No face detected")

        val faceBox = face.boundingBox
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
        val eyeY = if (leftEye != null && rightEye != null) (leftEye.y + rightEye.y) / 2f
        else faceBox.top + faceBox.height() * 0.35f
        val eyeX = if (leftEye != null && rightEye != null) (leftEye.x + rightEye.x) / 2f
        else faceBox.exactCenterX()

        val geom = FaceCropGeometry.computeCropRect(
            faceBoxHeightPx = faceBox.height().toFloat(),
            eyeX = eyeX,
            eyeY = eyeY,
            spec = spec,
            sourceWidth = composited.width,
            sourceHeight = composited.height,
        )
        val rect = when (geom) {
            is CropRectResult.Ok -> geom.rect
            CropRectResult.FaceTooClose ->
                return CropResult.Failure("Move further from the camera and retake")
        }

        val cropLeft = rect.left
        val cropTop = rect.top
        val outWidth = rect.width
        val outHeight = rect.height
        val srcW = composited.width
        val srcH = composited.height

        val padLeft = max(0f, -cropLeft).roundToInt()
        val padTop = max(0f, -cropTop).roundToInt()
        val padRight = max(0f, (cropLeft + outWidth) - srcW).roundToInt()
        val padBottom = max(0f, (cropTop + outHeight) - srcH).roundToInt()
        val needsPad = padLeft + padTop + padRight + padBottom > 0

        val canvasSrc = if (needsPad) padWithBackground(composited, padLeft, padTop, padRight, padBottom)
        else composited

        val adjLeft = (cropLeft + padLeft).roundToInt().coerceAtLeast(0)
        val adjTop = (cropTop + padTop).roundToInt().coerceAtLeast(0)
        val cropW = outWidth.roundToInt().coerceAtMost(canvasSrc.width - adjLeft)
        val cropH = outHeight.roundToInt().coerceAtMost(canvasSrc.height - adjTop)

        val cropped = Bitmap.createBitmap(canvasSrc, adjLeft, adjTop, cropW, cropH)
        if (needsPad && canvasSrc !== composited) canvasSrc.recycle()
        return CropResult.Success(cropped)
    }

    private suspend fun detect(bitmap: Bitmap): Face? = suspendCancellableCoroutine { cont ->
        val input = InputImage.fromBitmap(bitmap, 0)
        detector.process(input)
            .addOnSuccessListener { faces ->
                cont.resume(faces.maxByOrNull { it.boundingBox.height() })
            }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    private fun padWithBackground(source: Bitmap, left: Int, top: Int, right: Int, bottom: Int): Bitmap {
        val outW = source.width + left + right
        val outH = source.height + top + bottom
        val out = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(out)
        canvas.drawColor(sampleCornerColor(source))
        canvas.drawBitmap(source, left.toFloat(), top.toFloat(), null)
        return out
    }

    private fun sampleCornerColor(bitmap: Bitmap): Int {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return Color.WHITE
        val inset = minOf(2, (w - 1).coerceAtLeast(0), (h - 1).coerceAtLeast(0))
        val rightX = (w - 1 - inset).coerceAtLeast(0)
        val bottomY = (h - 1 - inset).coerceAtLeast(0)
        val samples = intArrayOf(
            bitmap.getPixel(inset, inset),
            bitmap.getPixel(rightX, inset),
            bitmap.getPixel(inset, bottomY),
            bitmap.getPixel(rightX, bottomY),
        )
        val r = samples.map { Color.red(it) }.average().toInt()
        val g = samples.map { Color.green(it) }.average().toInt()
        val b = samples.map { Color.blue(it) }.average().toInt()
        return Color.rgb(r, g, b)
    }

    override fun close() {
        detector.close()
    }
}
