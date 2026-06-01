package com.kartik.snapdoc.domain.pipeline

import android.graphics.Bitmap
import android.graphics.Color
import com.kartik.snapdoc.data.specs.model.BackgroundSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundCompositor @Inject constructor() {

    fun composite(removed: RemovedBackground, background: BackgroundSpec): Bitmap {
        val source = removed.source
        val mask = removed.mask
        val width = source.width
        val height = source.height

        val maskW = mask.width
        val maskH = mask.height
        val buf = mask.buffer.duplicate().asReadOnlyBuffer().apply { rewind() }
        val maskValues = FloatArray(maskW * maskH).also { buf.asFloatBuffer().get(it) }

        val bgColor = parseHex(background.colorHex)
        val bgR = Color.red(bgColor)
        val bgG = Color.green(bgColor)
        val bgB = Color.blue(bgColor)

        val srcPixels = IntArray(width * height).also {
            source.getPixels(it, 0, width, 0, 0, width, height)
        }
        val outPixels = IntArray(width * height)

        val scaleX = maskW.toFloat() / width
        val scaleY = maskH.toFloat() / height

        for (y in 0 until height) {
            val mY = (y * scaleY).toInt().coerceAtMost(maskH - 1)
            val rowOffset = mY * maskW
            val pixOffset = y * width
            for (x in 0 until width) {
                val mX = (x * scaleX).toInt().coerceAtMost(maskW - 1)
                val confidence = maskValues[rowOffset + mX]
                val alpha = confidence.coerceIn(0f, 1f)

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

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun parseHex(hex: String): Int {
        val cleaned = hex.removePrefix("#")
        val v = cleaned.toLong(16)
        return if (cleaned.length == 6) {
            (0xFF000000.toInt()) or v.toInt()
        } else {
            v.toInt()
        }
    }
}
