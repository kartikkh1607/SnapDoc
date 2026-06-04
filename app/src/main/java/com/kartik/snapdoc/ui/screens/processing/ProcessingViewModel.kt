package com.kartik.snapdoc.ui.screens.processing

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kartik.snapdoc.data.specs.SpecCatalogRepository
import com.kartik.snapdoc.domain.pipeline.PhotoProcessor
import com.kartik.snapdoc.domain.pipeline.ProcessingOutcome
import com.kartik.snapdoc.domain.pipeline.ProcessingStage
import com.kartik.snapdoc.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProcessingEvent {
    data class Done(val docId: String, val imageUri: String) : ProcessingEvent
}

@HiltViewModel
class ProcessingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: SpecCatalogRepository,
    private val processor: PhotoProcessor,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<Routes.Processing>()
    val docId: String = args.docId
    val imageUri: String = args.imageUri
    private val decodedImageUri: String = imageUri

    private val _state = MutableStateFlow(ProcessingUiState())
    val state: StateFlow<ProcessingUiState> = _state.asStateFlow()

    // One-shot navigation events. UNLIMITED capacity so emission is non-suspending,
    // but it's effectively single-shot — Done/Failed fires exactly once per run.
    private val _events = Channel<ProcessingEvent>(capacity = Channel.UNLIMITED)
    val events: Flow<ProcessingEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            processor.stage.collect { stage ->
                _state.update { it.copy(stage = stage, progress = stage.fraction()) }
            }
        }
        start()
    }

    fun retry() {
        if (_state.value.error == null) return
        _state.update {
            it.copy(
                stage = ProcessingStage.DetectingFace,
                progress = 0f,
                error = null,
                resultUri = null,
            )
        }
        start()
    }

    private fun start() {
        viewModelScope.launch {
            val spec = repo.byId(docId)
            if (spec == null) {
                _state.update { it.copy(error = "Unknown document") }
                return@launch
            }
            when (val outcome = processor.process(Uri.parse(decodedImageUri), spec)) {
                is ProcessingOutcome.Success -> {
                    val uri = outcome.entry.processedUri.toString()
                    _state.update {
                        it.copy(
                            stage = ProcessingStage.Done,
                            progress = 1f,
                            resultUri = uri,
                        )
                    }
                    _events.trySend(ProcessingEvent.Done(docId, uri))
                }
                is ProcessingOutcome.Failure -> {
                    _state.update { it.copy(error = outcome.message) }
                }
            }
        }
    }

    private fun ProcessingStage.fraction(): Float {
        val all = ProcessingStage.entries
        val idx = all.indexOf(this).coerceAtLeast(0)
        return (idx + 1f) / all.size
    }
}
