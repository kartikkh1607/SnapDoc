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
    val evenLighting: Boolean = false,
    val plainBackground: Boolean = false,
    val eyesOpen: Boolean = false,
)

fun FaceGuidanceState.headline(): String = when (this) {
    FaceGuidanceState.NoFace -> "No face detected"
    FaceGuidanceState.MultipleFaces -> "Only one person in frame"
    is FaceGuidanceState.TooFar -> "Move closer"
    is FaceGuidanceState.TooClose -> "Move back"
    FaceGuidanceState.NotCentered -> "Center your face"
    FaceGuidanceState.NotStraight -> "Look straight at the camera"
    FaceGuidanceState.EyesClosed -> "Open your eyes"
    FaceGuidanceState.MouthOpen -> "Close your mouth"
    FaceGuidanceState.Ready -> "Perfect — hold still"
}
