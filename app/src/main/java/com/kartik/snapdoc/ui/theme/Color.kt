package com.kartik.snapdoc.ui.theme

import androidx.compose.ui.graphics.Color

// SnapDoc design tokens. Mirrors tokens.jsx from the design source.

val Primary = Color(0xFF1B5E20)
val PrimaryDark = Color(0xFF0E3F12)
val PrimarySoft = Color(0xFFE8F2E9)
val PrimaryFaint = Color(0xFFF2F8F3)

val Amber = Color(0xFFFFA000)
val AmberDark = Color(0xFFE08800)
val AmberSoft = Color(0xFFFFF4DD)

val Background = Color(0xFFFAFAFA)
val Surface = Color(0xFFFFFFFF)

val Ink = Color(0xFF1A1A1A)
val Ink2 = Color(0xFF3D3D3D)
val Ink3 = Color(0xFF6B7280)
val Ink4 = Color(0xFF9CA3AF)

val Hairline = Color(0xFFECECEC)
val Hairline2 = Color(0xFFF2F2F2)

// Scrim used behind modal sheets (bottom-sheet style overlays).
val Scrim = Color(0xFF0F140F)
// Drag-handle indicator color for modal sheets.
val Grabber = Color(0xFFE0E0E0)

val Success = Color(0xFF2E7D32)
val ErrorRed = Color(0xFFC62828)

val OnPrimary = Color.White
val OnSurface = Ink
val OnSurfaceVariant = Ink3
val SurfaceVariant = PrimaryFaint
val Outline = Hairline

// Legacy alias kept for the dark-mode color scheme.
val PrimaryVariant = Success

// Permanent-dark surface tokens for screens that stay dark regardless of theme
// (camera, review). These are NOT the dark color scheme — they're hand-picked
// constants for camera-style chrome.
val ReviewSurface = Color(0xFF0D0E0D)
val CameraChipScrim = Color(0xFF14141B)
val CameraVignette = Color(0xFF080A08)

// Dark scheme tokens. These power [DarkColors] in Theme.kt. Screens that hardcode
// dark surfaces (camera, review) remain dark in both themes — intentional.
val DarkBackground = Color(0xFF0F1110)
val DarkSurface = Color(0xFF181B19)
val DarkSurfaceVariant = Color(0xFF1F2320)
val DarkOnSurface = Color(0xFFE6E8E6)
val DarkOnSurfaceVariant = Color(0xFFB8BDB8)
val DarkOutline = Color(0xFF2A2E2B)
val DarkPrimarySoft = Color(0xFF1F3624)
val DarkPrimaryFaint = Color(0xFF14201A)
