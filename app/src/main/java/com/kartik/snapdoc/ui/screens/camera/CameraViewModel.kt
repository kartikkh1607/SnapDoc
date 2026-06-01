package com.kartik.snapdoc.ui.screens.camera

import androidx.camera.core.CameraSelector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kartik.snapdoc.data.specs.SpecCatalogRepository
import com.kartik.snapdoc.domain.camera.FaceGuidanceState
import com.kartik.snapdoc.domain.camera.GuidanceChecks
import com.kartik.snapdoc.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: SpecCatalogRepository,
) : ViewModel() {

    val docId: String = savedStateHandle.get<String>(Routes.Args.DOC_ID).orEmpty()

    private val _state = MutableStateFlow(CameraUiState(doc = repo.byId(docId)))
    val state: StateFlow<CameraUiState> = _state.asStateFlow()

    fun onGuidance(guidance: FaceGuidanceState, checks: GuidanceChecks) {
        _state.update { it.copy(guidance = guidance, checks = checks) }
    }

    fun toggleLens() {
        _state.update {
            it.copy(
                lensFacing = if (it.lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                },
            )
        }
    }

    fun setCapturing(value: Boolean) {
        _state.update { it.copy(capturing = value) }
    }

    fun setError(message: String?) {
        _state.update { it.copy(error = message) }
    }
}
