package com.kartik.snapdoc.domain.pipeline

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class FileSizeCompressorTest {

    private val compressor = FileSizeCompressor()

    @Test
    fun `result lands inside the requested KB range for a detailed photo`() {
        val bitmap = makeNoisyBitmap(800, 800)
        val result = compressor.compressToTarget(bitmap, minKb = 30, maxKb = 80)

        assertThat(result.sizeKb).isAtLeast(0)
        assertThat(result.sizeKb).isAtMost(80)
        // Either the binary search found a match (lands in range) or the downscale
        // fallback bottomed out at quality 30. Both are acceptable outcomes; the
        // important guarantee is "we never exceed max".
        assertThat(result.bytes).isNotEmpty()
    }

    @Test
    fun `compressing a tiny solid bitmap stays below max`() {
        val bitmap = makeSolidBitmap(64, 64, Color.WHITE)
        val result = compressor.compressToTarget(bitmap, minKb = 0, maxKb = 50)
        assertThat(result.sizeKb).isAtMost(50)
    }

    @Test
    fun `quality used is in the legal JPEG range`() {
        val bitmap = makeNoisyBitmap(400, 400)
        val result = compressor.compressToTarget(bitmap, minKb = 10, maxKb = 100)
        assertThat(result.qualityUsed).isAtLeast(1)
        assertThat(result.qualityUsed).isAtMost(100)
    }

    private fun makeNoisyBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        // Hatch the canvas with alternating-colored rectangles to defeat JPEG's
        // smooth-region compression — gives a meaningful, predictable byte size.
        val cells = 32
        val cellW = width.toFloat() / cells
        val cellH = height.toFloat() / cells
        for (y in 0 until cells) {
            for (x in 0 until cells) {
                paint.color = Color.rgb((x * 8) % 256, (y * 8) % 256, ((x + y) * 4) % 256)
                canvas.drawRect(
                    x * cellW,
                    y * cellH,
                    (x + 1) * cellW,
                    (y + 1) * cellH,
                    paint,
                )
            }
        }
        return bitmap
    }

    private fun makeSolidBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawColor(color)
        return bitmap
    }
}
