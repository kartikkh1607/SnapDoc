package com.kartik.snapdoc.domain.watermark

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class WatermarkApplier @Inject constructor() {

    fun apply(source: Bitmap, text: String = "SnapDoc Preview"): Bitmap {
        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)
        val fontSize = max(out.width, out.height) * 0.08f

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = (0.35f * 255).toInt()
            textSize = fontSize
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val strokePaint = Paint(fillPaint).apply {
            color = Color.BLACK
            alpha = (0.35f * 255).toInt()
            style = Paint.Style.STROKE
            strokeWidth = fontSize * 0.06f
        }

        canvas.save()
        canvas.rotate(-30f, out.width / 2f, out.height / 2f)

        val textWidth = fillPaint.measureText(text)
        val stepX = textWidth * 1.4f
        val stepY = fontSize * 2.4f
        val startX = -out.width.toFloat()
        val endX = out.width * 2f
        val startY = -out.height.toFloat()
        val endY = out.height * 2f

        var y = startY
        while (y < endY) {
            var x = startX
            while (x < endX) {
                canvas.drawText(text, x, y, strokePaint)
                canvas.drawText(text, x, y, fillPaint)
                x += stepX
            }
            y += stepY
        }
        canvas.restore()
        return out
    }
}
