package com.kartik.snapdoc.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kartik.snapdoc.BuildConfig
import com.kartik.snapdoc.data.billing.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val purchases: PurchaseRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
        ),
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            purchases.entitlement.collect { e -> _state.update { it.copy(entitlement = e) } }
        }
    }

    fun restore() {
        if (_state.value.restoring) return
        _state.update { it.copy(restoring = true, restoreMessage = null) }
        viewModelScope.launch {
            runCatching { purchases.restorePurchases() }
                .onSuccess {
                    val canExport = _state.value.entitlement.canExport
                    _state.update {
                        it.copy(
                            restoring = false,
                            restoreMessage = if (canExport) "Purchases restored." else "No purchases to restore.",
                        )
                    }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(restoring = false, restoreMessage = t.message ?: "Restore failed")
                    }
                }
        }
    }

    fun dismissRestoreMessage() {
        _state.update { it.copy(restoreMessage = null) }
    }
}
