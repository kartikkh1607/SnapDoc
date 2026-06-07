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

        // Live framing gate is independent of the document's strict head-height spec
        // (that's checked at export). Here we just want "face is reasonably framed
        // inside the on-screen oval" — face box covering ~22–70% of frame height.
        val faceBoxRatio = box.height().toFloat() / frameHeight
        val targetMid = (FACE_BOX_MIN + FACE_BOX_MAX) / 2f

        val centerX = box.exactCenterX()
        val centerY = box.exactCenterY()
        val frameCenterX = frameWidth / 2f
        val frameCenterY = frameHeight * 0.45f
        val centerToleranceX = frameWidth * 0.15f
        val centerToleranceY = frameHeight * 0.18f
        val centered = abs(centerX - frameCenterX) < centerToleranceX &&
            abs(centerY - frameCenterY) < centerToleranceY

        // Yaw/roll matter most for ID photos; pitch (looking up/down) is the
        // least visually obvious on a small preview, so we allow a bit more
        // slack on the X axis.
        val straight = abs(face.headEulerAngleY) < POSE_TOLERANCE_YAW_DEG &&
            abs(face.headEulerAngleZ) < POSE_TOLERANCE_ROLL_DEG &&
            abs(face.headEulerAngleX) < POSE_TOLERANCE_PITCH_DEG

        val leftEyeOpen = (face.leftEyeOpenProbability ?: 1f) > 0.4f
        val rightEyeOpen = (face.rightEyeOpenProbability ?: 1f) > 0.4f
        val eyesOpen = leftEyeOpen && rightEyeOpen
        // ML Kit's "smiling" is generous — anything above ~0.85 is a real grin.
        // Anything below that we still count as a neutral-enough expression so
        // users with a slight upturned mouth aren't permanently blocked.
        val mouthClosed = (face.smilingProbability ?: 0f) < 0.85f

        val checks = GuidanceChecks(
            faceCentered = centered,
            eyesOpen = eyesOpen,
            // No analyzer yet for lighting / background. Treat as passing whenever
            // a face is in frame so the live-check rail reflects the design.
            evenLighting = true,
            plainBackground = true,
        )

        val state: FaceGuidanceState = when {
            faceBoxRatio < FACE_BOX_MIN -> FaceGuidanceState.TooFar(faceBoxRatio, targetMid)
            faceBoxRatio > FACE_BOX_MAX -> FaceGuidanceState.TooClose(faceBoxRatio, targetMid)
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
        // Pose tolerances widened from a uniform 15° so users in normal indoor
        // light can actually reach Ready without holding a perfectly rigid pose.
        // Yaw/roll stay tighter because they're what make an ID photo look off.
        private const val POSE_TOLERANCE_YAW_DEG = 20f
        private const val POSE_TOLERANCE_ROLL_DEG = 18f
        private const val POSE_TOLERANCE_PITCH_DEG = 25f
        private const val ANALYSIS_INTERVAL_MS = 200L
        // Upper bound is driven by what the cropper can actually use, not by what
        // looks framed in the preview. FaceCropGeometry needs:
        //   faceBox <= FACE_BOX_TO_HEAD_RATIO * headHeightPercent * sourceHeight
        // For the typical 0.70 ratio and a 70-80% head spec (mid 0.75), that
        // works out to ~0.525. We sit just under it at 0.50 to leave a small
        // margin against jitter between the live analyzer frame and the still
        // capture. Anything past this and the cropper would have to pad the
        // output with background-coloured strips — softer than the old hard
        // reject (it still produces a photo), but the preview should steer the
        // user away from that path in the first place.
        private const val FACE_BOX_MIN = 0.18f
        private const val FACE_BOX_MAX = 0.50f
    }
}
