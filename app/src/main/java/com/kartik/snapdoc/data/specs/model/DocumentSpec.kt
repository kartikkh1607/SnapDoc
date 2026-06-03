package com.kartik.snapdoc.data.specs.model

import kotlinx.serialization.Serializable

@Serializable
data class SpecCatalog(
    val version: Int,
    val updatedAt: String,
    val categories: List<CategorySpec>,
    val documents: List<DocumentSpec>,
)

@Serializable
data class CategorySpec(
    val id: String,
    val displayName: String,
    val icon: String,
)

@Serializable
data class DocumentSpec(
    val id: String,
    val displayName: String,
    val shortName: String,
    val categoryId: String,
    val popularity: Int = 0,
    val dimensions: DimensionsSpec,
    val background: BackgroundSpec,
    val face: FaceSpec,
    val file: FileSpec,
    val rules: RulesSpec,
    val sourceUrl: String = "",
    val lastUpdated: String = "",
    val notes: String = "",
)

@Serializable
data class DimensionsSpec(
    val widthMm: Float,
    val heightMm: Float,
    val widthPx: Int,
    val heightPx: Int,
    val dpi: Int,
) {
    init {
        require(widthMm > 0f && heightMm > 0f) { "widthMm/heightMm must be positive" }
        require(widthPx > 0 && heightPx > 0) { "widthPx/heightPx must be positive" }
        require(dpi > 0) { "dpi must be positive" }
    }
}

@Serializable
data class BackgroundSpec(
    val colorHex: String,
    val displayName: String,
    val toleranceLab: Int = 5,
) {
    init {
        require(colorHex.matches(HEX_COLOR_REGEX)) { "colorHex must be #RRGGBB or #AARRGGBB, was $colorHex" }
        require(toleranceLab >= 0) { "toleranceLab must be non-negative" }
    }

    private companion object {
        val HEX_COLOR_REGEX = Regex("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")
    }
}

@Serializable
data class FaceSpec(
    val headHeightPercentMin: Int,
    val headHeightPercentMax: Int,
    val eyeLineFromTopPercentMin: Int,
    val eyeLineFromTopPercentMax: Int,
) {
    init {
        require(headHeightPercentMin in 0..100 && headHeightPercentMax in 0..100) {
            "head height percent must be in 0..100"
        }
        require(headHeightPercentMin <= headHeightPercentMax) { "head height min must be <= max" }
        require(eyeLineFromTopPercentMin in 0..100 && eyeLineFromTopPercentMax in 0..100) {
            "eye line percent must be in 0..100"
        }
        require(eyeLineFromTopPercentMin <= eyeLineFromTopPercentMax) { "eye line min must be <= max" }
    }
}

@Serializable
data class FileSpec(
    val format: String,
    val minSizeKb: Int,
    val maxSizeKb: Int,
) {
    init {
        require(format.isNotBlank()) { "format must not be blank" }
        require(minSizeKb >= 0) { "minSizeKb must be non-negative" }
        require(minSizeKb <= maxSizeKb) { "minSizeKb must be <= maxSizeKb" }
    }
}

@Serializable
data class RulesSpec(
    val glassesAllowed: Boolean = false,
    val headCoveringAllowed: Boolean = false,
    val headCoveringReligiousOnly: Boolean = false,
    val mouthClosed: Boolean = true,
    val neutralExpression: Boolean = true,
    val eyesOpen: Boolean = true,
    val noShadows: Boolean = true,
)
