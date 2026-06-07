package com.kartik.snapdoc.ui.screens.processing

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kartik.snapdoc.data.specs.SpecCatalogRepository
import com.kartik.snapdoc.domain.pipeline.PhotoProcessor
import com.kartik.snapdoc.domain.pipeline.PipelineFailureReason
import com.kartik.snapdoc.domain.pipeline.ProcessingOutcome
import com.kartik.snapdoc.domain.pipeline.ProcessingStage
import com.kartik.snapdoc.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    // Be defensive about double-encoded URIs from typed-route round-trips.
    // If the value contains an unencoded scheme like "file:" we trust it as-is;
    // otherwise we Uri.decode once so "file%3A%2F%2F%2F..." is still parseable.
    private val decodedImageUri: String =
        if (imageUri.contains(":/")) imageUri else Uri.decode(imageUri)

    private val _state = MutableStateFlow(ProcessingUiState())
    val state: StateFlow<ProcessingUiState> = _state.asStateFlow()

    // One-shot navigation events. UNLIMITED capacity so emission is non-suspending,
    // but it's effectively single-shot — Done/Failed fires exactly once per run.
    private val _events = Channel<ProcessingEvent>(capacity = Channel.UNLIMITED)
    val events: Flow<ProcessingEvent> = _events.receiveAsFlow()

    private var processJob: Job? = null

    init {
        // Skip the singleton PhotoProcessor's *current* stage value on subscribe
        // (which may be `Done` from a previous run on this same singleton) and
        // only react to fresh emissions while this VM's process() is running.
        viewModelScope.launch {
            processor.stage.collect { stage ->
                // Don't let a stale "Done" from a previous run flip us into the
                // success state before our own process() has even started.
                if (_state.value.error != null) return@collect
                if (processJob?.isActive != true && stage == ProcessingStage.Done) return@collect
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
        // Don't double-start if a previous job is still in flight.
        processJob?.cancel()
        processJob = viewModelScope.launch {
            val spec = repo.byId(docId)
            if (spec == null) {
                Log.w(TAG, "Processing aborted: unknown docId=$docId")
                _state.update { it.copy(error = PipelineFailureReason.UnknownDocument) }
                return@launch
            }
            val parsedUri = runCatching { Uri.parse(decodedImageUri) }.getOrNull()
            if (parsedUri == null) {
                Log.w(TAG, "Processing aborted: unparseable imageUri=$imageUri")
                _state.update {
                    it.copy(error = PipelineFailureReason.SourceUnreadable)
                }
                return@launch
            }
            Log.d(TAG, "Start processing docId=$docId uri=$parsedUri")
            when (val outcome = processor.process(parsedUri, spec)) {
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
                    Log.w(TAG, "Processing failed: ${outcome.reason}")
                    _state.update { it.copy(error = outcome.reason) }
                }
            }
        }
    }

    private fun ProcessingStage.fraction(): Float {
        val all = ProcessingStage.entries
        val idx = all.indexOf(this).coerceAtLeast(0)
        return (idx + 1f) / all.size
    }

    private companion object {
        const val TAG = "ProcessingVM"
    }
}
