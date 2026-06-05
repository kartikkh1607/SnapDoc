package com.kartik.snapdoc.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kartik.snapdoc.core.common.AppInfo
import com.kartik.snapdoc.data.prefs.UserPrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPrefsRepository,
    appInfo: AppInfo,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(
            versionName = appInfo.versionName,
            versionCode = appInfo.versionCode,
        ),
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.profile.collect { p -> _state.update { it.copy(profile = p) } }
        }
        viewModelScope.launch {
            prefs.saveToGallery.collect { v -> _state.update { it.copy(saveToGallery = v) } }
        }
    }

    fun toggleSaveToGallery() {
        viewModelScope.launch { prefs.setSaveToGallery(!_state.value.saveToGallery) }
    }
}
