package com.kartik.snapdoc.domain.pipeline

/**
 * Parses a `#RRGGBB` or `#AARRGGBB` hex string into an ARGB int.
 * 6-digit values get an opaque alpha applied.
 */
internal fun parseHexColor(hex: String): Int {
    val cleaned = hex.removePrefix("#")
    val v = cleaned.toLong(16)
    val argb = if (cleaned.length == 6) v or 0xFF000000L else v
    return argb.toInt()
}
