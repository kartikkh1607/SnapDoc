package com.kartik.snapdoc.ui.screens.preview

import android.net.Uri
import com.kartik.snapdoc.data.billing.EntitlementState
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.domain.pipeline.ValidationCheck

data class PreviewUiState(
    val doc: DocumentSpec? = null,
    val processedUri: Uri? = null,
    val checks: List<ValidationCheck> = emptyList(),
    val allPassed: Boolean = false,
    val entitlement: EntitlementState = EntitlementState.Locked,
    val sizeKb: Int = 0,
    val widthPx: Int = 0,
    val heightPx: Int = 0,
) {
    val watermarked: Boolean get() = !entitlement.canExport
}
