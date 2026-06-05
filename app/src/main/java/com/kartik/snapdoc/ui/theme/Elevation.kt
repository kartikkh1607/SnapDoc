package com.kartik.snapdoc.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// SnapDoc has three subtle surface elevation tiers. Compose's
// `shadow()` doesn't take a colored offset like CSS box-shadow, so
// these are a single-layer approximation tuned to the design.

fun Modifier.s1(corner: Dp = 14.dp): Modifier =
    shadow(
        elevation = 1.dp,
        shape = RoundedCornerShape(corner),
        ambientColor = Color(0x14000000),
        spotColor = Color(0x14000000),
    )

fun Modifier.s2(corner: Dp = 18.dp): Modifier =
    shadow(
        elevation = 6.dp,
        shape = RoundedCornerShape(corner),
        ambientColor = Color(0x1A000000),
        spotColor = Color(0x1F000000),
    )

fun Modifier.s3(corner: Dp = 22.dp): Modifier =
    shadow(
        elevation = 14.dp,
        shape = RoundedCornerShape(corner),
        ambientColor = Color(0x24000000),
        spotColor = Color(0x29000000),
    )

fun Modifier.sGreen(corner: Dp = 20.dp): Modifier =
    shadow(
        elevation = 12.dp,
        shape = RoundedCornerShape(corner),
        ambientColor = Color(0x381B5E20),
        spotColor = Color(0x4D1B5E20),
    )

fun Modifier.sAmber(corner: Dp = 18.dp): Modifier =
    shadow(
        elevation = 12.dp,
        shape = RoundedCornerShape(corner),
        ambientColor = Color(0x47FFA000),
        spotColor = Color(0x66FFA000),
    )
