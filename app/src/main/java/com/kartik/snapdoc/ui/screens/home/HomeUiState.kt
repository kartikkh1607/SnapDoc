package com.kartik.snapdoc.ui.screens.home

import com.kartik.snapdoc.data.specs.model.CategorySpec
import com.kartik.snapdoc.data.specs.model.DocumentSpec

data class HomeUiState(
    val query: String = "",
    val selectedCategoryId: String? = null,
    val categories: List<CategorySpec> = emptyList(),
    val popular: List<DocumentSpec> = emptyList(),
    val documents: List<DocumentSpec> = emptyList(),
) {
    val showPopular: Boolean get() = query.isBlank() && selectedCategoryId == null
}
