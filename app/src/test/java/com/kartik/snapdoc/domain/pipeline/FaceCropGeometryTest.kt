package com.kartik.snapdoc.domain.pipeline

import com.google.common.truth.Truth.assertThat
import com.kartik.snapdoc.data.specs.model.BackgroundSpec
import com.kartik.snapdoc.data.specs.model.DimensionsSpec
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.data.specs.model.FaceSpec
import com.kartik.snapdoc.data.specs.model.FileSpec
import com.kartik.snapdoc.data.specs.model.RulesSpec
import org.junit.Test

class FaceCropGeometryTest {

    @Test
    fun `output rect height accounts for face-box-to-head ratio and mid head-height percent`() {
        // Spec wants the head to occupy 70-80% of the photo height (mid 75%).
        // ML Kit's face box is only ~70% of the full crown-to-chin head, so the
        // estimated head height is faceBox / 0.70. A 300px face box → 428.57px
        // estimated head → 571.43px output (head fills 75% of that).
        val spec = passportLike(headMin = 70, headMax = 80, eyeMin = 50, eyeMax = 60)

        val result = FaceCropGeometry.computeCropRect(
            faceBoxHeightPx = 300f,
            eyeX = 500f,
            eyeY = 500f,
            spec = spec,
            sourceWidth = 2000,
            sourceHeight = 2000,
        )

        val rect = (result as CropRectResult.Ok).rect
        assertThat(rect.height).isWithin(0.01f).of(300f / 0.70f / 0.75f)
    }

    @Test
    fun `output rect width follows spec aspect ratio`() {
        // 413x531 px spec = ~0.778 aspect; height 400 → width ~311.1.
        val spec = passportLike(widthPx = 413, heightPx = 531)

        val result = FaceCropGeometry.computeCropRect(
            faceBoxHeightPx = 300f,
            eyeX = 500f,
            eyeY = 500f,
            spec = spec,
            sourceWidth = 2000,
            sourceHeight = 2000,
        )

        val rect = (result as CropRectResult.Ok).rect
        val expectedWidth = rect.height * (413f / 531f)
        assertThat(rect.width).isWithin(0.01f).of(expectedWidth)
    }

    @Test
    fun `crop is centered horizontally on the eye anchor`() {
        val spec = passportLike()

        val result = FaceCropGeometry.computeCropRect(
            faceBoxHeightPx = 300f,
            eyeX = 800f,
            eyeY = 500f,
            spec = spec,
            sourceWidth = 2000,
            sourceHeight = 2000,
        )

        val rect = (result as CropRectResult.Ok).rect
        val centerX = rect.left + rect.width / 2f
        assertThat(centerX).isWithin(0.01f).of(800f)
    }

    @Test
    fun `eye line lands at the spec-defined percent from the top`() {
        // eye-line 50-60% (mid 55%) means eyes sit 55% down from the top edge.
        val spec = passportLike(eyeMin = 50, eyeMax = 60)

        val result = FaceCropGeometry.computeCropRect(
            faceBoxHeightPx = 300f,
            eyeX = 500f,
            eyeY = 500f,
            spec = spec,
            sourceWidth = 2000,
            sourceHeight = 2000,
        )

        val rect = (result as CropRectResult.Ok).rect
        val eyeOffsetFromTop = 500f - rect.top
        val expected = rect.height * 0.55f
        assertThat(eyeOffsetFromTop).isWithin(0.01f).of(expected)
    }

    @Test
    fun `returns Ok even when output rect exceeds the source bitmap so the caller can pad`() {
        // Big face box on a small source: output height would be ~1905px,
        // larger than the 1000px-tall source. The geometry no longer rejects
        // this — FaceCropper.padWithBackground extends the canvas instead, so
        // a close-up shot still produces a photo rather than a hard failure.
        val spec = passportLike(headMin = 70, headMax = 80)

        val result = FaceCropGeometry.computeCropRect(
            faceBoxHeightPx = 1000f,
            eyeX = 500f,
            eyeY = 500f,
            spec = spec,
            sourceWidth = 1000,
            sourceHeight = 1000,
        )

        val rect = (result as CropRectResult.Ok).rect
        assertThat(rect.height).isGreaterThan(1000f)
    }

    @Test
    fun `off-center eye anchor produces an off-center crop with negative left padding`() {
        // Eye close to the left edge — crop rect should run past the source's
        // left edge (negative left), which the caller pads.
        val spec = passportLike()

        val result = FaceCropGeometry.computeCropRect(
            faceBoxHeightPx = 600f,
            eyeX = 50f,
            eyeY = 500f,
            spec = spec,
            sourceWidth = 2000,
            sourceHeight = 2000,
        )

        val rect = (result as CropRectResult.Ok).rect
        assertThat(rect.left).isLessThan(0f)
    }

    private fun passportLike(
        widthPx: Int = 413,
        heightPx: Int = 531,
        headMin: Int = 70,
        headMax: Int = 80,
        eyeMin: Int = 50,
        eyeMax: Int = 60,
    ) = DocumentSpec(
        id = "test",
        displayName = "Test",
        shortName = "Test",
        categoryId = "test",
        dimensions = DimensionsSpec(
            widthMm = 35f,
            heightMm = 45f,
            widthPx = widthPx,
            heightPx = heightPx,
            dpi = 300,
        ),
        background = BackgroundSpec(colorHex = "#FFFFFF", displayName = "White"),
        face = FaceSpec(
            headHeightPercentMin = headMin,
            headHeightPercentMax = headMax,
            eyeLineFromTopPercentMin = eyeMin,
            eyeLineFromTopPercentMax = eyeMax,
        ),
        file = FileSpec(format = "JPEG", minSizeKb = 10, maxSizeKb = 100),
        rules = RulesSpec(),
    )
}
