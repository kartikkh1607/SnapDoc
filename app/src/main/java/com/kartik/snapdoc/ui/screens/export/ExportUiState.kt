package com.kartik.snapdoc.ui.screens.export

import android.net.Uri

enum class ExportPhase { Idle, Saving, Saved, WatchingAd, Error }

data class ExportUiState(
    val phase: ExportPhase = ExportPhase.Idle,
    val hdUnlocked: Boolean = false,
    val savedUri: Uri? = null,
    val errorRes: Int? = null,
)
