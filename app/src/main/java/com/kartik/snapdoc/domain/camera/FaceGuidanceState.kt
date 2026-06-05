package com.kartik.snapdoc.domain.camera

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
    val evenLighting: Boolean = false,
    val plainBackground: Boolean = false,
)
