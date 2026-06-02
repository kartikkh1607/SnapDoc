package com.kartik.snapdoc.domain.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrintSheetGenerator @Inject constructor(

    @ApplicationContext private val context: Context,
) {

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(120, 0, 0, 0)
        style = Paint.Style.STROKE
        strokeWidth = 0.5f
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(3f, 3f), 0f)
    }

    suspend fun generatePdf(photo: Bitmap, layout: SheetLayout, spec: DocumentSpec): File = withContext(Dispatchers.IO) {
        val doc = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(layout.sheet.widthPt, layout.sheet.heightPt, 1).create()
            val page = doc.startPage(pageInfo)
            drawTiles(page.canvas, photo, layout)
            doc.finishPage(page)

            val outFile = File(context.cacheDir, "snapdoc_sheet_${spec.id}_${layout.sheet.name}_${System.currentTimeMillis()}.pdf")
            FileOutputStream(outFile).use { doc.writeTo(it) }
            outFile
        } finally {
            doc.close()
        }
    }

    suspend fun generateJpegBytes(photo: Bitmap, layout: SheetLayout, dpi: Int = 300): ByteArray = withContext(Dispatchers.Default) {
        val pxPerPt = dpi / 72f
        val widthPx = (layout.sheet.widthPt * pxPerPt).toInt()
        val heightPx = (layout.sheet.heightPt * pxPerPt).toInt()

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.scale(pxPerPt, pxPerPt)
            drawTiles(canvas, photo, layout)
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            out.toByteArray()
        } finally {
            bitmap.recycle()
        }
    }

    private fun drawTiles(canvas: Canvas, photo: Bitmap, layout: SheetLayout) {
        for (r in 0 until layout.rows) {
            for (c in 0 until layout.cols) {
                val x = layout.marginLeftPt + c * (layout.photoWidthPt + layout.gutterPt)
                val y = layout.marginTopPt + r * (layout.photoHeightPt + layout.gutterPt)
                val rect = RectF(x, y, x + layout.photoWidthPt, y + layout.photoHeightPt)
                if (layout.rotated) {
                    // Rotate the photo 90° around the tile center so the source
                    // image (whose own aspect is W × H) fills a H × W tile.
                    canvas.save()
                    canvas.rotate(90f, rect.centerX(), rect.centerY())
                    val rotatedDest = RectF(
                        rect.centerX() - rect.height() / 2f,
                        rect.centerY() - rect.width() / 2f,
                        rect.centerX() + rect.height() / 2f,
                        rect.centerY() + rect.width() / 2f,
                    )
                    canvas.drawBitmap(photo, null, rotatedDest, null)
                    canvas.restore()
                } else {
                    canvas.drawBitmap(photo, null, rect, null)
                }
                canvas.drawRect(rect, borderPaint)
            }
        }
    }
}
