package com.kartik.snapdoc.domain.pipeline

import android.graphics.Bitmap
import android.graphics.Color
import com.kartik.snapdoc.data.specs.model.BackgroundSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundCompositor @Inject constructor() {

    /**
     * Composites [removed]'s foreground onto a solid [background] colour.
     *
     * The image is processed in horizontal bands whose pixel buffer is capped
     * around [BAND_BUDGET_BYTES]. Each band is read into a temporary IntArray,
     * composited in parallel (workers split the band by row), and written back
     * into the output bitmap. Peak transient memory is therefore one band buffer
     * (~8MB on a typical photo) plus the output bitmap, rather than the previous
     * two full-image IntArrays.
     */
    suspend fun composite(removed: RemovedBackground, background: BackgroundSpec): Bitmap =
        withContext(Dispatchers.Default) {
            val source = removed.source
            val mask = removed.mask
            val width = source.width
            val height = source.height

            val maskW = mask.width
            val maskH = mask.height
            val buf = mask.buffer.duplicate().asReadOnlyBuffer().apply { rewind() }
            val maskValues = FloatArray(maskW * maskH).also { buf.asFloatBuffer().get(it) }

            val bgColor = parseHexColor(background.colorHex)
            val bgR = Color.red(bgColor)
            val bgG = Color.green(bgColor)
            val bgB = Color.blue(bgColor)

            val scaleX = maskW.toFloat() / width
            val scaleY = maskH.toFloat() / height

            val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val workers = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
            val bandHeight = chooseBandHeight(width, height)
            val bandBuffer = IntArray(bandHeight * width)

            var bandStart = 0
            while (bandStart < height) {
                val bandRows = (height - bandStart).coerceAtMost(bandHeight)
                source.getPixels(bandBuffer, 0, width, 0, bandStart, width, bandRows)

                coroutineScope {
                    (0 until workers)
                        .map { worker ->
                            async {
                                val sliceStart = worker * bandRows / workers
                                val sliceEnd = (worker + 1) * bandRows / workers
                                if (sliceStart < sliceEnd) {
                                    compositeSlice(
                                        bandYOffset = bandStart,
                                        sliceStart = sliceStart,
                                        sliceEnd = sliceEnd,
                                        width = width,
                                        maskW = maskW, maskH = maskH,
                                        scaleX = scaleX, scaleY = scaleY,
                                        maskValues = maskValues,
                                        pixels = bandBuffer,
                                        bgR = bgR, bgG = bgG, bgB = bgB,
                                    )
                                }
                            }
                        }
                        .awaitAll()
                }

                out.setPixels(bandBuffer, 0, width, 0, bandStart, width, bandRows)
                bandStart += bandRows
            }

            out
        }

    private fun chooseBandHeight(width: Int, height: Int): Int {
        val bytesPerRow = width * BYTES_PER_PIXEL
        if (bytesPerRow <= 0) return height.coerceAtLeast(1)
        val rowsByBudget = (BAND_BUDGET_BYTES / bytesPerRow).coerceAtLeast(1)
        return rowsByBudget.coerceAtMost(height).coerceAtLeast(1)
    }

    private fun compositeSlice(
        bandYOffset: Int,
        sliceStart: Int,
        sliceEnd: Int,
        width: Int,
        maskW: Int,
        maskH: Int,
        scaleX: Float,
        scaleY: Float,
        maskValues: FloatArray,
        pixels: IntArray,
        bgR: Int,
        bgG: Int,
        bgB: Int,
    ) {
        val maxX = maskW - 1
        val maxY = maskH - 1
        for (row in sliceStart until sliceEnd) {
            val fy = (bandYOffset + row) * scaleY
            val y0 = fy.toInt().coerceIn(0, maxY)
            val y1 = (y0 + 1).coerceAtMost(maxY)
            val ty = (fy - y0).coerceIn(0f, 1f)
            val row0 = y0 * maskW
            val row1 = y1 * maskW
            val pixOffset = row * width
            for (x in 0 until width) {
                val fx = x * scaleX
                val x0 = fx.toInt().coerceIn(0, maxX)
                val x1 = (x0 + 1).coerceAtMost(maxX)
                val tx = (fx - x0).coerceIn(0f, 1f)

                val v00 = maskValues[row0 + x0]
                val v01 = maskValues[row0 + x1]
                val v10 = maskValues[row1 + x0]
                val v11 = maskValues[row1 + x1]
                val top = v00 + (v01 - v00) * tx
                val bot = v10 + (v11 - v10) * tx
                val alpha = (top + (bot - top) * ty).coerceIn(0f, 1f)

                val src = pixels[pixOffset + x]
                pixels[pixOffset + x] = when {
                    alpha >= 0.95f -> src
                    alpha <= 0.05f -> Color.rgb(bgR, bgG, bgB)
                    else -> {
                        val r = (Color.red(src) * alpha + bgR * (1 - alpha)).toInt()
                        val g = (Color.green(src) * alpha + bgG * (1 - alpha)).toInt()
                        val b = (Color.blue(src) * alpha + bgB * (1 - alpha)).toInt()
                        Color.rgb(r, g, b)
                    }
                }
            }
        }
    }

    private companion object {
        const val BYTES_PER_PIXEL = 4
        // 8MB band buffer keeps transient pipeline memory predictable: roughly
        // 524 rows at 4000px wide, 1024 rows at 2048px wide.
        const val BAND_BUDGET_BYTES = 8 * 1024 * 1024
    }
}
