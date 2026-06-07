package com.kartik.snapdoc.ui.screens.export

import android.net.Uri

enum class ExportPhase { Idle, Saving, Saved, WatchingAd, Error }

data class ExportUiState(
    val phase: ExportPhase = ExportPhase.Idle,
    // App is fully free; HD is always available.
    val hdUnlocked: Boolean = true,
    val savedUri: Uri? = null,
    val errorRes: Int? = null,
)
