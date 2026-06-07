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

    /**
     * ML Kit's face bounding box covers roughly eyebrows-to-chin, NOT the full
     * head (crown-to-chin). Document specs measure head height as crown-to-chin,
     * so we have to scale the face box up before mapping it into the spec.
     *
     * In typical adult-portrait proportions the face box is ~70% of the full
     * head — the upper ~30% is hair and the crown above the eyebrows. Using
     * the raw face box without this correction produced crops where the head
     * filled ~95% of the frame and the hair got chopped off the top.
     */
    private const val FACE_BOX_TO_HEAD_RATIO = 0.70f

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

        // Estimate the full head height (crown-to-chin) from the face box.
        val estimatedHeadHeightPx = faceBoxHeightPx / FACE_BOX_TO_HEAD_RATIO
        val outHeight = estimatedHeadHeightPx / headHeightPercent
        val outWidth = outHeight * targetAspect

        // No bounds check here: if the computed rect overflows the source,
        // FaceCropper.padWithBackground extends the canvas with the sampled
        // corner colour. The previous hard-reject (FaceTooClose) was firing on
        // any reasonably close shot — the live FaceGuidanceAnalyzer is the
        // right place to nudge the user back, not a failure at the crop stage.

        val top = eyeY - outHeight * eyeLinePercent
        val left = eyeX - outWidth / 2f
        return CropRectResult.Ok(CropRect(left = left, top = top, width = outWidth, height = outHeight))
    }
}
