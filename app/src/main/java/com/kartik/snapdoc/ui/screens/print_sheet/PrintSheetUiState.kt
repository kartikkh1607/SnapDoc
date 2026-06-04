package com.kartik.snapdoc.ui.screens.print_sheet

import android.net.Uri
import androidx.annotation.StringRes
import com.kartik.snapdoc.data.billing.EntitlementState
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.domain.print.SheetLayout
import com.kartik.snapdoc.domain.print.SheetSize

enum class PrintExportPhase { Idle, Generating, Saved, Error }

data class PrintSheetUiState(
    val doc: DocumentSpec? = null,
    val sheet: SheetSize = SheetSize.A4,
    val layout: SheetLayout? = null,
    val entitlement: EntitlementState = EntitlementState.Locked,
    val phase: PrintExportPhase = PrintExportPhase.Idle,
    val savedUri: Uri? = null,
    val shareUri: Uri? = null,
    val shareMime: String? = null,
    @param:StringRes val errorRes: Int? = null,
) {
    val locked: Boolean get() = !entitlement.canPrintSheet
}
