package com.kartik.snapdoc.domain.pipeline

import com.kartik.snapdoc.data.specs.model.DocumentSpec

data class CropRect(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
)

sealed interface CropRectResult {
    data class Ok(val rect: CropRect) : CropRectResult
    data object FaceTooClose : CropRectResult
}

object FaceCropGeometry {

    fun computeCropRect(
        faceBoxHeightPx: Float,
        eyeX: Float,
        eyeY: Float,
        spec: DocumentSpec,
        sourceWidth: Int,
        sourceHeight: Int,
    ): CropRectResult {
        val targetAspect = spec.dimensions.widthPx.toFloat() / spec.dimensions.heightPx
        val headHeightPercent =
            (spec.face.headHeightPercentMin + spec.face.headHeightPercentMax) / 2f / 100f
        val eyeLinePercent =
            (spec.face.eyeLineFromTopPercentMin + spec.face.eyeLineFromTopPercentMax) / 2f / 100f

        val outHeight = faceBoxHeightPx / headHeightPercent
        val outWidth = outHeight * targetAspect

        if (outWidth > sourceWidth || outHeight > sourceHeight) {
            return CropRectResult.FaceTooClose
        }

        val top = eyeY - outHeight * eyeLinePercent
        val left = eyeX - outWidth / 2f
        return CropRectResult.Ok(CropRect(left = left, top = top, width = outWidth, height = outHeight))
    }
}
