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
    /**
     * Whether each photo cell should be rotated 90° on the sheet. True when
     * laying out a landscape source onto the sheet, or when rotating a
     * portrait photo gives more copies per page.
     */
    val rotated: Boolean = false,
) {
    val copies: Int get() = cols * rows
}

object PrintSheetLayout {
    /**
     * Computes the grid of copies that fit on [sheet]. Tries both the natural
     * orientation and the rotated one (swap width/height) and picks whichever
     * yields more copies — that's the "free upgrade" for landscape sources or
     * portrait photos that happen to tile better when laid sideways.
     *
     * Gutter defaults to 2mm; margin to 5mm.
     */
    fun compute(
        sheet: SheetSize,
        photoWidthMm: Float,
        photoHeightMm: Float,
        gutterMm: Float = 2f,
        marginMm: Float = 5f,
    ): SheetLayout {
        val natural = layout(sheet, photoWidthMm, photoHeightMm, gutterMm, marginMm, rotated = false)
        val rotated = layout(sheet, photoHeightMm, photoWidthMm, gutterMm, marginMm, rotated = true)
        return if (rotated.copies > natural.copies) rotated else natural
    }

    private fun layout(
        sheet: SheetSize,
        photoWidthMm: Float,
        photoHeightMm: Float,
        gutterMm: Float,
        marginMm: Float,
        rotated: Boolean,
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
            rotated = rotated,
        )
    }
}
