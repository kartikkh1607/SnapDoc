package com.kartik.snapdoc.ui.screens.settings

import com.kartik.snapdoc.data.prefs.UserProfile

data class SettingsUiState(
    val profile: UserProfile = UserProfile("Riya Sharma", "riya.sharma@gmail.com"),
    val saveToGallery: Boolean = true,
    val versionName: String = "",
    val versionCode: Int = 0,
)
