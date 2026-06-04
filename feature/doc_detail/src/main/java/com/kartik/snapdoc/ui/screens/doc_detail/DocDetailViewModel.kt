package com.kartik.snapdoc.ui.screens.doc_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.kartik.snapdoc.data.specs.SpecCatalogRepository
import com.kartik.snapdoc.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DocDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: SpecCatalogRepository,
) : ViewModel() {

    val docId: String = savedStateHandle.toRoute<Routes.DocDetail>().docId

    private val _state = MutableStateFlow(
        repo.byId(docId)?.let { DocDetailUiState(doc = it) }
            ?: DocDetailUiState(notFound = true),
    )
    val state: StateFlow<DocDetailUiState> = _state.asStateFlow()
}
