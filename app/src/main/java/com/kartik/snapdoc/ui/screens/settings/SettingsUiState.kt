package com.kartik.snapdoc.ui.screens.settings

import com.kartik.snapdoc.data.billing.EntitlementState

data class SettingsUiState(
    val entitlement: EntitlementState = EntitlementState.Locked,
    val versionName: String = "",
    val versionCode: Int = 0,
    val restoring: Boolean = false,
    val restoreMessage: String? = null,
)
