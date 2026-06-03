package com.kartik.snapdoc.domain.pipeline

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.pow
import kotlin.math.sqrt

data class ValidationCheck(
    val name: String,
    val expected: String,
    val actual: String,
    val passed: Boolean,
)

data class ValidationResult(
    val passed: Boolean,
    val checks: List<ValidationCheck>,
)

@Singleton
class SpecValidator @Inject constructor() : Closeable {

    // Lazy so unit tests can construct the validator without ML Kit's native
    // detector being initialized at class-load time (Robolectric doesn't ship
    // the ML Kit native lib).
    private val detector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setMinFaceSize(0.15f)
                .build(),
        )
    }

    suspend fun validate(
        bitmap: Bitmap,
        fileSizeKb: Int,
        spec: DocumentSpec,
    ): ValidationResult {
        val checks = staticChecks(bitmap, fileSizeKb, spec).toMutableList()

        val face = detect(bitmap)
        checks += ValidationCheck(
            name = "Face detected",
            expected = "1 face",
            actual = if (face != null) "1 face" else "no face",
            passed = face != null,
        )

        if (face != null) {
            val ratio = face.boundingBox.height().toFloat() / bitmap.height * 100f
            val ratioPassed = ratio >= spec.face.headHeightPercentMin && ratio <= spec.face.headHeightPercentMax
            checks += ValidationCheck(
                name = "Head ratio",
                expected = "${spec.face.headHeightPercentMin}–${spec.face.headHeightPercentMax}%",
                actual = "${ratio.toInt()}%",
                passed = ratioPassed,
            )
        }

        return ValidationResult(passed = checks.all { it.passed }, checks = checks)
    }

    /** Synchronous checks that don't require ML Kit — useful for unit tests. */
    internal fun staticChecks(
        bitmap: Bitmap,
        fileSizeKb: Int,
        spec: DocumentSpec,
    ): List<ValidationCheck> {
        val dims = ValidationCheck(
            name = "Dimensions",
            expected = "${spec.dimensions.widthPx} × ${spec.dimensions.heightPx} px",
            actual = "${bitmap.width} × ${bitmap.height} px",
            passed = bitmap.width == spec.dimensions.widthPx &&
                bitmap.height == spec.dimensions.heightPx,
        )
        val size = ValidationCheck(
            name = "File size",
            expected = "${spec.file.minSizeKb}–${spec.file.maxSizeKb} KB",
            actual = "$fileSizeKb KB",
            passed = fileSizeKb in spec.file.minSizeKb..spec.file.maxSizeKb,
        )
        val avgBg = sampleCornerAverage(bitmap)
        val targetBg = parseHexColor(spec.background.colorHex)
        val deltaE = labDelta(avgBg, targetBg)
        val bg = ValidationCheck(
            name = "Background",
            expected = "${spec.background.displayName} (${spec.background.colorHex})",
            actual = "#%02X%02X%02X".format(Color.red(avgBg), Color.green(avgBg), Color.blue(avgBg)),
            // Tolerance multiplier widens the acceptance band because the corner-pixel
            // average is sampled from a tiny 4×100-pixel patch and tends to drift a few
            // ΔE units from the true background even on a clean white. Without this,
            // photos that look fine to humans (and to government portals) get rejected.
            passed = deltaE <= spec.background.toleranceLab.toFloat() * BG_TOLERANCE_MULTIPLIER,
        )
        return listOf(dims, size, bg)
    }

    private suspend fun detect(bitmap: Bitmap): Face? = suspendCancellableCoroutine { cont ->
        detector.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { faces -> cont.resume(faces.maxByOrNull { it.boundingBox.height() }) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    private fun sampleCornerAverage(bitmap: Bitmap): Int {
        val w = bitmap.width
        val h = bitmap.height
        val patches = listOf(
            Triple(2, 2, "tl"),
            Triple(w - 12, 2, "tr"),
            Triple(2, h - 12, "bl"),
            Triple(w - 12, h - 12, "br"),
        )
        var r = 0L; var g = 0L; var b = 0L; var count = 0
        for ((x, y) in patches.map { it.first to it.second }) {
            for (dy in 0 until 10) {
                for (dx in 0 until 10) {
                    val px = bitmap.getPixel(x + dx, y + dy)
                    r += Color.red(px)
                    g += Color.green(px)
                    b += Color.blue(px)
                    count++
                }
            }
        }
        return Color.rgb((r / count).toInt(), (g / count).toInt(), (b / count).toInt())
    }

    /**
     * Quick CIE76 Lab delta E between two sRGB colors.
     * Good enough for the "is this background close to white/grey" check.
     */
    private fun labDelta(c1: Int, c2: Int): Float {
        val (l1, a1, b1) = srgbToLab(c1)
        val (l2, a2, b2) = srgbToLab(c2)
        return sqrt((l1 - l2).pow(2) + (a1 - a2).pow(2) + (b1 - b2).pow(2))
    }

    private fun srgbToLab(color: Int): Triple<Float, Float, Float> {
        val r = pivotRgb(Color.red(color) / 255f)
        val g = pivotRgb(Color.green(color) / 255f)
        val b = pivotRgb(Color.blue(color) / 255f)

        val x = (r * 0.4124f + g * 0.3576f + b * 0.1805f) / 0.95047f
        val y = (r * 0.2126f + g * 0.7152f + b * 0.0722f) / 1.00000f
        val z = (r * 0.0193f + g * 0.1192f + b * 0.9505f) / 1.08883f

        val fx = pivotXyz(x)
        val fy = pivotXyz(y)
        val fz = pivotXyz(z)

        val l = 116f * fy - 16f
        val a = 500f * (fx - fy)
        val bb = 200f * (fy - fz)
        return Triple(l, a, bb)
    }

    private fun pivotRgb(v: Float): Float =
        if (v > 0.04045f) ((v + 0.055f) / 1.055f).pow(2.4f) else v / 12.92f

    private fun pivotXyz(v: Float): Float =
        if (v > 0.008856f) v.pow(1f / 3f) else (7.787f * v) + (16f / 116f)

    override fun close() {
        detector.close()
    }

    private companion object {
        const val BG_TOLERANCE_MULTIPLIER = 3f
    }
}
