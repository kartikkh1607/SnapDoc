package com.kartik.snapdoc.ui.screens.export

import android.net.Uri
import com.kartik.snapdoc.data.billing.EntitlementState

enum class ExportPhase { Paywall, Purchasing, Saving, Saved, Error }

data class ExportUiState(
    val phase: ExportPhase = ExportPhase.Paywall,
    val entitlement: EntitlementState = EntitlementState.Locked,
    val savedUri: Uri? = null,
    val error: String? = null,
    val selectedProductId: String? = null,
)
