package com.kartik.snapdoc.domain.camera

import androidx.annotation.StringRes
import com.kartik.snapdoc.R

sealed interface FaceGuidanceState {
    object NoFace : FaceGuidanceState
    object MultipleFaces : FaceGuidanceState
    data class TooFar(val currentRatio: Float, val targetRatio: Float) : FaceGuidanceState
    data class TooClose(val currentRatio: Float, val targetRatio: Float) : FaceGuidanceState
    object NotCentered : FaceGuidanceState
    object NotStraight : FaceGuidanceState
    object EyesClosed : FaceGuidanceState
    object MouthOpen : FaceGuidanceState
    object Ready : FaceGuidanceState
}

data class GuidanceChecks(
    val faceCentered: Boolean = false,
    val eyesOpen: Boolean = false,
)

@StringRes
fun FaceGuidanceState.headlineRes(): Int = when (this) {
    FaceGuidanceState.NoFace -> R.string.guidance_no_face
    FaceGuidanceState.MultipleFaces -> R.string.guidance_multiple_faces
    is FaceGuidanceState.TooFar -> R.string.guidance_too_far
    is FaceGuidanceState.TooClose -> R.string.guidance_too_close
    FaceGuidanceState.NotCentered -> R.string.guidance_not_centered
    FaceGuidanceState.NotStraight -> R.string.guidance_not_straight
    FaceGuidanceState.EyesClosed -> R.string.guidance_eyes_closed
    FaceGuidanceState.MouthOpen -> R.string.guidance_mouth_open
    FaceGuidanceState.Ready -> R.string.guidance_ready
}
