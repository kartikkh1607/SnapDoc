package com.kartik.snapdoc.ui.screens.preview

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.kartik.snapdoc.data.specs.SpecCatalogRepository
import com.kartik.snapdoc.domain.pipeline.ProcessingResultStore
import com.kartik.snapdoc.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: SpecCatalogRepository,
    resultStore: ProcessingResultStore,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<Routes.Preview>()
    val docId: String = args.docId
    val imageUri: String = args.imageUri
    private val decodedUri: String = imageUri

    private val _state = MutableStateFlow(PreviewUiState())
    val state: StateFlow<PreviewUiState> = _state.asStateFlow()

    init {
        val doc = repo.byId(docId)
        val entry = resultStore.get(decodedUri)
        _state.value = PreviewUiState(
            doc = doc,
            processedUri = Uri.parse(decodedUri),
            checks = entry?.validation?.checks.orEmpty(),
            allPassed = entry?.validation?.passed ?: false,
            sizeKb = entry?.sizeKb ?: 0,
            widthPx = entry?.widthPx ?: 0,
            heightPx = entry?.heightPx ?: 0,
        )
    }
}
