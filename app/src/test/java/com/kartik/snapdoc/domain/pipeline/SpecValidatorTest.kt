package com.kartik.snapdoc.domain.pipeline

import android.graphics.Bitmap
import android.graphics.Color
import com.google.common.truth.Truth.assertThat
import com.kartik.snapdoc.data.specs.model.BackgroundSpec
import com.kartik.snapdoc.data.specs.model.DimensionsSpec
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.data.specs.model.FaceSpec
import com.kartik.snapdoc.data.specs.model.FileSpec
import com.kartik.snapdoc.data.specs.model.RulesSpec
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests only the synchronous validation paths exposed via [SpecValidator.staticChecks].
 * The face-detection arm is exercised by integration tests because Robolectric
 * can't load the ML Kit native detector.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class SpecValidatorTest {

    private val validator = SpecValidator()

    @Test
    fun `dimensions check passes when bitmap matches spec exactly`() {
        val bitmap = solidBitmap(413, 531, Color.WHITE)
        val spec = passportSpec()

        val check = validator.staticChecks(bitmap, fileSizeKb = 50, spec = spec)
            .first { it.name == "Dimensions" }

        assertThat(check.passed).isTrue()
        assertThat(check.actual).isEqualTo("413 × 531 px")
    }

    @Test
    fun `dimensions check fails when bitmap is the wrong size`() {
        val bitmap = solidBitmap(400, 500, Color.WHITE)
        val spec = passportSpec()

        val check = validator.staticChecks(bitmap, fileSizeKb = 50, spec = spec)
            .first { it.name == "Dimensions" }

        assertThat(check.passed).isFalse()
    }

    @Test
    fun `file size check passes inside the spec window and fails outside it`() {
        val bitmap = solidBitmap(413, 531, Color.WHITE)
        val spec = passportSpec()

        val inWindow = validator.staticChecks(bitmap, 50, spec).first { it.name == "File size" }
        val tooSmall = validator.staticChecks(bitmap, 5, spec).first { it.name == "File size" }
        val tooLarge = validator.staticChecks(bitmap, 500, spec).first { it.name == "File size" }

        assertThat(inWindow.passed).isTrue()
        assertThat(tooSmall.passed).isFalse()
        assertThat(tooLarge.passed).isFalse()
    }

    @Test
    fun `background check passes when corner samples match the target hex`() {
        val bitmap = solidBitmap(413, 531, Color.WHITE)
        val spec = passportSpec()

        val check = validator.staticChecks(bitmap, 50, spec).first { it.name == "Background" }

        assertThat(check.passed).isTrue()
    }

    @Test
    fun `background check fails when corners are far from target color`() {
        val bitmap = solidBitmap(413, 531, Color.BLACK)
        val spec = passportSpec()

        val check = validator.staticChecks(bitmap, 50, spec).first { it.name == "Background" }

        assertThat(check.passed).isFalse()
    }

    private fun solidBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // eraseColor is honored by Robolectric's getPixel where Canvas.drawColor isn't.
        bitmap.eraseColor(color)
        return bitmap
    }

    private fun passportSpec(): DocumentSpec = DocumentSpec(
        id = "test_passport",
        displayName = "Test Passport",
        shortName = "Passport",
        categoryId = "test",
        popularity = 0,
        dimensions = DimensionsSpec(35f, 45f, 413, 531, 300),
        background = BackgroundSpec(colorHex = "#FFFFFF", displayName = "White", toleranceLab = 5),
        face = FaceSpec(70, 80, 50, 70),
        file = FileSpec(format = "JPG", minSizeKb = 10, maxSizeKb = 100),
        rules = RulesSpec(),
    )
}
