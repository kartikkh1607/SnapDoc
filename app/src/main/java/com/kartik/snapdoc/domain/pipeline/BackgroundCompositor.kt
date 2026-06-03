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
     * Per-pixel work is split across CPU cores: each worker takes a horizontal
     * band of rows and writes into its slice of the shared output array.
     * Cancellation propagates between bands because each band is a child
     * coroutine of the caller's scope.
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

            val srcPixels = IntArray(width * height).also {
                source.getPixels(it, 0, width, 0, 0, width, height)
            }
            val outPixels = IntArray(width * height)

            val scaleX = maskW.toFloat() / width
            val scaleY = maskH.toFloat() / height

            val workers = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
            val bandHeight = (height + workers - 1) / workers

            coroutineScope {
                (0 until workers)
                    .map { worker ->
                        async {
                            val yStart = worker * bandHeight
                            val yEnd = ((worker + 1) * bandHeight).coerceAtMost(height)
                            compositeBand(
                                yStart = yStart, yEnd = yEnd, width = width,
                                maskW = maskW, maskH = maskH, scaleX = scaleX, scaleY = scaleY,
                                maskValues = maskValues, srcPixels = srcPixels, outPixels = outPixels,
                                bgR = bgR, bgG = bgG, bgB = bgB,
                            )
                        }
                    }
                    .awaitAll()
            }

            val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            out.setPixels(outPixels, 0, width, 0, 0, width, height)
            out
        }

    private fun compositeBand(
        yStart: Int,
        yEnd: Int,
        width: Int,
        maskW: Int,
        maskH: Int,
        scaleX: Float,
        scaleY: Float,
        maskValues: FloatArray,
        srcPixels: IntArray,
        outPixels: IntArray,
        bgR: Int,
        bgG: Int,
        bgB: Int,
    ) {
        val maxX = maskW - 1
        val maxY = maskH - 1
        for (y in yStart until yEnd) {
            val fy = y * scaleY
            val y0 = fy.toInt().coerceIn(0, maxY)
            val y1 = (y0 + 1).coerceAtMost(maxY)
            val ty = (fy - y0).coerceIn(0f, 1f)
            val row0 = y0 * maskW
            val row1 = y1 * maskW
            val pixOffset = y * width
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

                val src = srcPixels[pixOffset + x]
                outPixels[pixOffset + x] = when {
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

}
