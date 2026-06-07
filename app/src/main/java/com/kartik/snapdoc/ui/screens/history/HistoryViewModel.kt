package com.kartik.snapdoc.ui.screens.history

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kartik.snapdoc.data.history.HistoryItem
import com.kartik.snapdoc.data.history.HistoryRepository
import com.kartik.snapdoc.data.specs.SpecCatalogRepository
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryRow(
    val item: HistoryItem,
    val doc: DocumentSpec?,
)

data class HistoryUiState(
    val loading: Boolean = true,
    val rows: List<HistoryRow> = emptyList(),
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepo: HistoryRepository,
    private val specRepo: SpecCatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val items = historyRepo.list()
            val rows = items.map { HistoryRow(item = it, doc = specRepo.byId(it.docId)) }
            _state.update { it.copy(loading = false, rows = rows) }
        }
    }

    fun delete(uri: Uri) {
        viewModelScope.launch {
            if (historyRepo.delete(uri)) refresh()
        }
    }
}
