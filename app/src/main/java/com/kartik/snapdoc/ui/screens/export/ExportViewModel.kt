package com.kartik.snapdoc.ui.screens.export

import android.app.Activity
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kartik.snapdoc.R
import com.kartik.snapdoc.domain.export.PhotoExporter
import com.kartik.snapdoc.domain.pipeline.ProcessingResultStore
import com.kartik.snapdoc.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resultStore: ProcessingResultStore,
    private val exporter: PhotoExporter,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<Routes.Export>()
    val docId: String = args.docId
    private val decodedUri: String = args.imageUri

    private val _state = MutableStateFlow(ExportUiState(hdUnlocked = true))
    val state: StateFlow<ExportUiState> = _state.asStateFlow()

    // App is fully free — both entry points just call save(). The "HD" variant
    // is kept as a separate function so existing call sites still compile.
    fun saveStandard(activity: Activity) = save(activity)
    fun saveHdViaRewardedAd(activity: Activity) = save(activity)

    private fun save(activity: Activity) {
        _state.update { it.copy(phase = ExportPhase.Saving, errorRes = null) }
        viewModelScope.launch {
            val entry = resultStore.get(decodedUri)
            if (entry == null) {
                _state.update {
                    it.copy(phase = ExportPhase.Error, errorRes = R.string.export_error_missing_processed)
                }
                return@launch
            }
            runCatching { exporter.saveToGallery(entry.readBytes(), docId) }
                .onSuccess { uri ->
                    _state.update { it.copy(phase = ExportPhase.Saved, savedUri = uri) }
                }
                .onFailure { t ->
                    Log.w(TAG, "Save failed", t)
                    _state.update {
                        it.copy(phase = ExportPhase.Error, errorRes = R.string.export_error_save_failed)
                    }
                }
        }
    }

    private companion object { const val TAG = "ExportViewModel" }
}
