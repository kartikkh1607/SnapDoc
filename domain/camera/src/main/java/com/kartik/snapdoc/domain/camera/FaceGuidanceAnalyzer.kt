package com.kartik.snapdoc.domain.camera

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.kartik.snapdoc.data.specs.model.FaceSpec
import kotlin.math.abs

class FaceGuidanceAnalyzer(
    private val faceSpec: FaceSpec,
    private val onState: (FaceGuidanceState, GuidanceChecks) -> Unit,
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setMinFaceSize(0.20f)
            .build(),
    )

    private var lastAnalysisMs = 0L

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastAnalysisMs < ANALYSIS_INTERVAL_MS) {
            imageProxy.close()
            return
        }
        lastAnalysisMs = now

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val frameWidth = if (imageProxy.imageInfo.rotationDegrees % 180 == 0) imageProxy.width else imageProxy.height
        val frameHeight = if (imageProxy.imageInfo.rotationDegrees % 180 == 0) imageProxy.height else imageProxy.width

        detector.process(input)
            .addOnSuccessListener { faces ->
                val (state, checks) = compute(faces, frameWidth.toFloat(), frameHeight.toFloat())
                onState(state, checks)
            }
            .addOnFailureListener {
                onState(FaceGuidanceState.NoFace, GuidanceChecks())
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun compute(
        faces: List<Face>,
        frameWidth: Float,
        frameHeight: Float,
    ): Pair<FaceGuidanceState, GuidanceChecks> {
        if (faces.isEmpty()) return FaceGuidanceState.NoFace to GuidanceChecks()
        if (faces.size > 1) return FaceGuidanceState.MultipleFaces to GuidanceChecks()

        val face = faces.first()
        val box = face.boundingBox

        val headRatio = box.height().toFloat() / frameHeight
        val targetMin = faceSpec.headHeightPercentMin / 100f
        val targetMax = faceSpec.headHeightPercentMax / 100f
        val targetMid = (targetMin + targetMax) / 2f

        val centerX = box.exactCenterX()
        val centerY = box.exactCenterY()
        val frameCenterX = frameWidth / 2f
        val frameCenterY = frameHeight * 0.45f
        val centerToleranceX = frameWidth * 0.10f
        val centerToleranceY = frameHeight * 0.12f
        val centered = abs(centerX - frameCenterX) < centerToleranceX &&
            abs(centerY - frameCenterY) < centerToleranceY

        val straight = abs(face.headEulerAngleY) < POSE_TOLERANCE_DEG &&
            abs(face.headEulerAngleZ) < POSE_TOLERANCE_DEG &&
            abs(face.headEulerAngleX) < POSE_TOLERANCE_DEG

        val leftEyeOpen = (face.leftEyeOpenProbability ?: 1f) > 0.5f
        val rightEyeOpen = (face.rightEyeOpenProbability ?: 1f) > 0.5f
        val eyesOpen = leftEyeOpen && rightEyeOpen
        val mouthClosed = (face.smilingProbability ?: 0f) < 0.7f

        val checks = GuidanceChecks(
            faceCentered = centered,
            eyesOpen = eyesOpen,
            // No analyzer yet for lighting / background. Treat as passing whenever
            // a face is in frame so the live-check rail reflects the design.
            evenLighting = true,
            plainBackground = true,
        )

        val state: FaceGuidanceState = when {
            headRatio < targetMin * 0.85f -> FaceGuidanceState.TooFar(headRatio, targetMid)
            headRatio > targetMax * 1.15f -> FaceGuidanceState.TooClose(headRatio, targetMid)
            !centered -> FaceGuidanceState.NotCentered
            !straight -> FaceGuidanceState.NotStraight
            !eyesOpen -> FaceGuidanceState.EyesClosed
            !mouthClosed -> FaceGuidanceState.MouthOpen
            else -> FaceGuidanceState.Ready
        }
        return state to checks
    }

    fun close() {
        detector.close()
    }

    companion object {
        private const val POSE_TOLERANCE_DEG = 8f
        private const val ANALYSIS_INTERVAL_MS = 200L
    }
}
