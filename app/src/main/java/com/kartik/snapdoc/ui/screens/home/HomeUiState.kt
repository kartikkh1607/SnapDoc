package com.kartik.snapdoc.ui.screens.home

import com.kartik.snapdoc.data.prefs.UserProfile
import com.kartik.snapdoc.data.specs.model.CategorySpec
import com.kartik.snapdoc.data.specs.model.DocumentSpec

data class HomeUiState(
    val selectedCategoryId: String? = null,
    val categories: List<CategorySpec> = emptyList(),
    val documents: List<DocumentSpec> = emptyList(),
    val loading: Boolean = true,
    val profile: UserProfile = UserProfile("Riya Sharma", "riya.sharma@gmail.com"),
)
