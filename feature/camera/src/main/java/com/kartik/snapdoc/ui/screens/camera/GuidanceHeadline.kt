package com.kartik.snapdoc.ui.screens.camera

import androidx.annotation.StringRes
import com.kartik.snapdoc.feature.camera.R
import com.kartik.snapdoc.domain.camera.FaceGuidanceState

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
