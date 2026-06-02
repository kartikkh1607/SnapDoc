package com.kartik.snapdoc.ui.screens.camera

import androidx.camera.core.CameraSelector
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.domain.camera.FaceGuidanceState
import com.kartik.snapdoc.domain.camera.GuidanceChecks

enum class FlashMode { Off, Auto, On }

data class CameraUiState(
    val doc: DocumentSpec? = null,
    val guidance: FaceGuidanceState = FaceGuidanceState.NoFace,
    val checks: GuidanceChecks = GuidanceChecks(),
    val lensFacing: Int = CameraSelector.LENS_FACING_FRONT,
    val flashMode: FlashMode = FlashMode.Off,
    val capturing: Boolean = false,
    val error: String? = null,
) {
    val captureEnabled: Boolean get() = guidance == FaceGuidanceState.Ready && !capturing
}
