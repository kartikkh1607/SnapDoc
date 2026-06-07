package com.kartik.snapdoc.domain.pipeline

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.kartik.snapdoc.data.specs.model.BackgroundSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

/** Thrown when the segmentation mask is too sparse to be trustworthy. */
class DeadMaskException(message: String) : RuntimeException(message)

@Singleton
class BackgroundCompositor @Inject constructor() {

    /**
     * Composites [removed]'s foreground onto a solid [background] colour.
     *
     * If the mask comes back implausibly empty (fewer than [MIN_FOREGROUND_RATIO]
     * of pixels classified as foreground) we throw [DeadMaskException] instead of
     * silently producing an all-background image. PhotoProcessor catches this
     * and falls back to the unmodified source so the user always gets a photo
     * with their face in it.
     */
    suspend fun composite(removed: RemovedBackground, background: BackgroundSpec): Bitmap =
        withContext(Dispatchers.Default) {
            val source = removed.source
            val mask = removed.mask
            val width = source.width
            val height = source.height

            val maskW = mask.width
            val maskH = mask.height
            // Be defensive about the buffer: ML Kit returns it in native byte
            // order, but force it explicitly so we never read garbage on a
            // platform that defaults the duplicate to BIG_ENDIAN.
            val buf = mask.buffer.duplicate()
                .order(ByteOrder.nativeOrder())
                .apply { rewind() }
            val expectedFloats = maskW * maskH
            val availableFloats = buf.remaining() / 4
            val count = minOf(expectedFloats, availableFloats)
            val maskValues = FloatArray(expectedFloats)
            if (count > 0) {
                buf.asFloatBuffer().get(maskValues, 0, count)
            }

            // Sanity-check the mask. A healthy selfie should have >= 5% of the
            // frame as foreground. If we got ~0%, the segmentation result is
            // dead — fall back to the source rather than producing a blank image.
            var fg = 0
            var sum = 0f
            for (v in maskValues) {
                sum += v
                if (v >= 0.5f) fg++
            }
            val mean = if (maskValues.isNotEmpty()) sum / maskValues.size else 0f
            val fgRatio = if (maskValues.isNotEmpty()) fg.toFloat() / maskValues.size else 0f
            Log.d(TAG, "mask=${maskW}x${maskH} mean=%.3f fg=%.1f%% (expected=$expectedFloats available=$availableFloats)"
                .format(mean, fgRatio * 100))
            if (fgRatio < MIN_FOREGROUND_RATIO) {
                throw DeadMaskException(
                    "Mask only ${"%.2f".format(fgRatio * 100)}% foreground — segmentation likely failed",
                )
            }

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
                // Sharp cutoff at 0.5 — preserves the face cleanly. Edges are
                // a touch harder than a blend, but for ID photos that's fine.
                pixels[pixOffset + x] = if (alpha >= 0.5f) src else Color.rgb(bgR, bgG, bgB)
            }
        }
    }

    private companion object {
        const val TAG = "BackgroundCompositor"
        const val BYTES_PER_PIXEL = 4
        // 8MB band buffer keeps transient pipeline memory predictable: roughly
        // 524 rows at 4000px wide, 1024 rows at 2048px wide.
        const val BAND_BUDGET_BYTES = 8 * 1024 * 1024
        // If less than 5% of the mask is classified as foreground on a selfie
        // photo, the segmentation result is unusable. A real selfie occupies
        // ~15-40% of the frame, so 5% is a very conservative floor.
        const val MIN_FOREGROUND_RATIO = 0.05f
    }
}
