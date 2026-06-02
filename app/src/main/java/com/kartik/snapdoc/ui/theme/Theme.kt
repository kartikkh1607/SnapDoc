package com.kartik.snapdoc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

private val DarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = DarkPrimarySoft,
    onPrimaryContainer = Color.White,
    secondary = Amber,
    onSecondary = OnPrimary,
    secondaryContainer = DarkPrimaryFaint,
    onSecondaryContainer = Amber,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = ErrorRed,
    onError = OnPrimary,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
)

@Composable
fun SnapDocTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = SnapDocShapes,
        content = content,
    )
}
