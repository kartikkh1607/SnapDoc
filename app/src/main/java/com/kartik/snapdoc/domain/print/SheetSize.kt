package com.kartik.snapdoc.domain.print

/** PDF points (1 point = 1/72 inch). */
enum class SheetSize(
    val displayName: String,
    val widthPt: Int,
    val heightPt: Int,
) {
    FourBySix("4×6 inch", 288, 432),
    A4("A4", 595, 842),
    Letter("US Letter", 612, 792);

    val widthMm: Float get() = widthPt * MM_PER_PT
    val heightMm: Float get() = heightPt * MM_PER_PT

    companion object {
        const val MM_PER_PT: Float = 25.4f / 72f
        const val PT_PER_MM: Float = 72f / 25.4f
        fun mmToPt(mm: Float): Float = mm * PT_PER_MM
    }
}

/** Result of fitting `photoWidthMm` × `photoHeightMm` copies on a sheet with a gutter. */
data class SheetLayout(
    val sheet: SheetSize,
    val cols: Int,
    val rows: Int,
    val photoWidthPt: Float,
    val photoHeightPt: Float,
    val gutterPt: Float,
    val marginLeftPt: Float,
    val marginTopPt: Float,
) {
    val copies: Int get() = cols * rows
}

object PrintSheetLayout {
    /** Computes grid of copies that fit on `sheet`. Gutter defaults to 2mm. */
    fun compute(
        sheet: SheetSize,
        photoWidthMm: Float,
        photoHeightMm: Float,
        gutterMm: Float = 2f,
        marginMm: Float = 5f,
    ): SheetLayout {
        val photoWPt = SheetSize.mmToPt(photoWidthMm)
        val photoHPt = SheetSize.mmToPt(photoHeightMm)
        val gutterPt = SheetSize.mmToPt(gutterMm)
        val marginPt = SheetSize.mmToPt(marginMm)

        val usableWidth = sheet.widthPt - 2 * marginPt
        val usableHeight = sheet.heightPt - 2 * marginPt

        val cols = ((usableWidth + gutterPt) / (photoWPt + gutterPt)).toInt().coerceAtLeast(1)
        val rows = ((usableHeight + gutterPt) / (photoHPt + gutterPt)).toInt().coerceAtLeast(1)

        val gridWidth = cols * photoWPt + (cols - 1) * gutterPt
        val gridHeight = rows * photoHPt + (rows - 1) * gutterPt
        val marginLeft = (sheet.widthPt - gridWidth) / 2f
        val marginTop = (sheet.heightPt - gridHeight) / 2f

        return SheetLayout(
            sheet = sheet,
            cols = cols,
            rows = rows,
            photoWidthPt = photoWPt,
            photoHeightPt = photoHPt,
            gutterPt = gutterPt,
            marginLeftPt = marginLeft,
            marginTopPt = marginTop,
        )
    }
}
