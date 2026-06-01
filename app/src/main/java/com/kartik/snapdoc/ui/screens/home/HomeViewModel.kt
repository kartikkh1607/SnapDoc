package com.kartik.snapdoc.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kartik.snapdoc.data.specs.SpecCatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: SpecCatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    categories = repo.categories(),
                    popular = repo.popular(5),
                    documents = repo.search("", null),
                )
            }
        }
    }

    fun onQueryChange(query: String) {
        _state.update {
            it.copy(
                query = query,
                documents = repo.search(query, it.selectedCategoryId),
            )
        }
    }

    fun onCategorySelect(categoryId: String?) {
        _state.update {
            it.copy(
                selectedCategoryId = categoryId,
                documents = repo.search(it.query, categoryId),
            )
        }
    }
}
