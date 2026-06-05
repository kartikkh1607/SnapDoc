package com.kartik.snapdoc.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Project corner radii. The Material3 [Shapes] mapping covers the common
 * cases; the named extras are for SnapDoc-specific rounded surfaces (icon
 * tiles, pills, hero cards) that don't fit Material's small/medium/large.
 */
val SnapDocShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(22.dp),
)

object SnapDocCorners {
    val tile = RoundedCornerShape(12.dp)
    val card = RoundedCornerShape(14.dp)
    val hero = RoundedCornerShape(20.dp)
    val pill = RoundedCornerShape(99.dp)
}
