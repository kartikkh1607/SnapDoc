package com.kartik.snapdoc.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kartik.snapdoc.core.common.AppInfo
import com.kartik.snapdoc.data.billing.PurchaseRepository
import com.kartik.snapdoc.feature.settings.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val purchases: PurchaseRepository,
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
                            restoreMessage = appContext.getString(
                                if (canExport) R.string.settings_restore_success
                                else R.string.settings_restore_none,
                            ),
                        )
                    }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(
                            restoring = false,
                            restoreMessage = t.message
                                ?: appContext.getString(R.string.settings_restore_failed),
                        )
                    }
                }
        }
    }

    fun dismissRestoreMessage() {
        _state.update { it.copy(restoreMessage = null) }
    }
}
