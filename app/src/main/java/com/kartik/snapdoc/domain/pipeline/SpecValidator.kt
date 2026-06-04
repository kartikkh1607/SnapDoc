package com.kartik.snapdoc.domain.pipeline

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.pow
import kotlin.math.sqrt

enum class ValidationCheckKind {
    Dimensions,
    FileSize,
    Background,
    FaceDetected,
    HeadRatio,
    EyeLine,
}

data class ValidationCheck(
    val kind: ValidationCheckKind,
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
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
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
            kind = ValidationCheckKind.FaceDetected,
            expected = "1",
            actual = if (face != null) "1" else "0",
            passed = face != null,
        )

        if (face != null) {
            val ratio = face.boundingBox.height().toFloat() / bitmap.height * 100f
            val ratioPassed = ratio >= spec.face.headHeightPercentMin && ratio <= spec.face.headHeightPercentMax
            checks += ValidationCheck(
                kind = ValidationCheckKind.HeadRatio,
                expected = "${spec.face.headHeightPercentMin}–${spec.face.headHeightPercentMax}%",
                actual = "${ratio.toInt()}%",
                passed = ratioPassed,
            )

            val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
            val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
            // Fall back to a 35%-down-the-face estimate when landmarks are absent —
            // matches FaceCropper's fallback so validation aligns with crop placement.
            val eyeY = when {
                leftEye != null && rightEye != null -> (leftEye.y + rightEye.y) / 2f
                else -> face.boundingBox.top + face.boundingBox.height() * 0.35f
            }
            val eyePercent = eyeY / bitmap.height * 100f
            val eyePassed = eyePercent >= spec.face.eyeLineFromTopPercentMin &&
                eyePercent <= spec.face.eyeLineFromTopPercentMax
            checks += ValidationCheck(
                kind = ValidationCheckKind.EyeLine,
                expected = "${spec.face.eyeLineFromTopPercentMin}–${spec.face.eyeLineFromTopPercentMax}%",
                actual = "${eyePercent.toInt()}%",
                passed = eyePassed,
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
            kind = ValidationCheckKind.Dimensions,
            expected = "${spec.dimensions.widthPx} × ${spec.dimensions.heightPx} px",
            actual = "${bitmap.width} × ${bitmap.height} px",
            passed = bitmap.width == spec.dimensions.widthPx &&
                bitmap.height == spec.dimensions.heightPx,
        )
        val size = ValidationCheck(
            kind = ValidationCheckKind.FileSize,
            expected = "${spec.file.minSizeKb}–${spec.file.maxSizeKb} KB",
            actual = "$fileSizeKb KB",
            passed = fileSizeKb in spec.file.minSizeKb..spec.file.maxSizeKb,
        )
        val avgBg = sampleCornerAverage(bitmap)
        val targetBg = parseHexColor(spec.background.colorHex)
        val deltaE = labDelta(avgBg, targetBg)
        val bg = ValidationCheck(
            kind = ValidationCheckKind.Background,
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
        if (w <= 0 || h <= 0) return Color.WHITE
        val patch = minOf(PATCH_SIZE, w, h).coerceAtLeast(1)
        val inset = minOf(PATCH_INSET, (w - patch).coerceAtLeast(0), (h - patch).coerceAtLeast(0))
        val rightX = (w - patch - inset).coerceAtLeast(0)
        val bottomY = (h - patch - inset).coerceAtLeast(0)
        val corners = listOf(
            inset to inset,
            rightX to inset,
            inset to bottomY,
            rightX to bottomY,
        )
        var r = 0L; var g = 0L; var b = 0L; var count = 0
        for ((x, y) in corners) {
            for (dy in 0 until patch) {
                for (dx in 0 until patch) {
                    val px = bitmap.getPixel(x + dx, y + dy)
                    r += Color.red(px)
                    g += Color.green(px)
                    b += Color.blue(px)
                    count++
                }
            }
        }
        if (count == 0) return Color.WHITE
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
        const val PATCH_SIZE = 10
        const val PATCH_INSET = 2
    }
}
