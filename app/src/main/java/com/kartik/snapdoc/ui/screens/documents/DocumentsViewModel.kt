package com.kartik.snapdoc.ui.screens.documents

import androidx.lifecycle.ViewModel
import com.kartik.snapdoc.data.specs.SpecCatalogRepository
import com.kartik.snapdoc.data.specs.model.CategorySpec
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class DocumentsUiState(
    val query: String = "",
    val selectedCategoryId: String? = null,
    val categories: List<CategorySpec> = emptyList(),
    val documents: List<DocumentSpec> = emptyList(),
)

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val repo: SpecCatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        DocumentsUiState(
            categories = repo.categories(),
            documents = repo.search("", null),
        ),
    )
    val state: StateFlow<DocumentsUiState> = _state.asStateFlow()

    fun setQuery(value: String) {
        _state.update {
            it.copy(query = value, documents = repo.search(value, it.selectedCategoryId))
        }
    }

    fun setCategory(id: String?) {
        _state.update {
            it.copy(selectedCategoryId = id, documents = repo.search(it.query, id))
        }
    }
}
