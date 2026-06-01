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
)

@Serializable
data class BackgroundSpec(
    val colorHex: String,
    val displayName: String,
    val toleranceLab: Int = 5,
)

@Serializable
data class FaceSpec(
    val headHeightPercentMin: Int,
    val headHeightPercentMax: Int,
    val eyeLineFromTopPercentMin: Int,
    val eyeLineFromTopPercentMax: Int,
)

@Serializable
data class FileSpec(
    val format: String,
    val minSizeKb: Int,
    val maxSizeKb: Int,
)

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
