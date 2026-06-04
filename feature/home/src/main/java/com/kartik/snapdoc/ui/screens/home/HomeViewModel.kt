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
                    documents = repo.search("", null),
                    loading = false,
                )
            }
        }
    }

    fun onCategorySelect(categoryId: String?) {
        _state.update {
            it.copy(
                selectedCategoryId = categoryId,
                documents = repo.search("", categoryId),
            )
        }
    }
}
