package com.kartik.snapdoc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimarySoft,
    onPrimaryContainer = Primary,
    secondary = Amber,
    onSecondary = OnPrimary,
    secondaryContainer = AmberSoft,
    onSecondaryContainer = AmberDark,
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorRed,
    onError = OnPrimary,
    outline = Outline,
    outlineVariant = Hairline2,
)

@Composable
fun SnapDocTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    // SnapDoc is light-only by design.
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
