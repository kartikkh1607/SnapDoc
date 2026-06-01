package com.kartik.snapdoc.domain.print

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PrintSheetLayoutTest {

    @Test
    fun `passport on A4 fits at least 24 copies`() {
        // 35×45 mm passport, A4 (210×297 mm), 2 mm gutter, 5 mm margins.
        // Tighter packing gets ~32; the floor we care about is "enough for the print shop".
        val layout = PrintSheetLayout.compute(SheetSize.A4, photoWidthMm = 35f, photoHeightMm = 45f)
        assertThat(layout.copies).isAtLeast(24)
        assertThat(layout.cols * layout.rows).isEqualTo(layout.copies)
    }

    @Test
    fun `4x6 holds 6 passport photos in 2x3`() {
        val layout = PrintSheetLayout.compute(SheetSize.FourBySix, photoWidthMm = 35f, photoHeightMm = 45f)
        // 4×6 inch = ~102×152 mm. Should fit roughly 2 cols × 3 rows.
        assertThat(layout.cols).isEqualTo(2)
        assertThat(layout.rows).isEqualTo(3)
        assertThat(layout.copies).isEqualTo(6)
    }

    @Test
    fun `letter sheet copies are in the same ballpark as A4`() {
        val a4 = PrintSheetLayout.compute(SheetSize.A4, 35f, 45f)
        val letter = PrintSheetLayout.compute(SheetSize.Letter, 35f, 45f)
        // Letter (~216×279 mm) is shorter than A4 (210×297 mm) by ~18 mm, so it
        // commonly drops one row of passport-size cells. Difference should still be modest.
        assertThat(Math.abs(a4.copies - letter.copies)).isAtMost(8)
        assertThat(letter.copies).isAtLeast(20)
    }

    @Test
    fun `oversized photo still fits at least one copy`() {
        // A square 50×50 mm photo on a 4×6 — should still produce a layout, never 0.
        val layout = PrintSheetLayout.compute(SheetSize.FourBySix, 50f, 50f)
        assertThat(layout.copies).isAtLeast(1)
        assertThat(layout.cols).isAtLeast(1)
        assertThat(layout.rows).isAtLeast(1)
    }

    @Test
    fun `margins center the grid on the sheet`() {
        val layout = PrintSheetLayout.compute(SheetSize.A4, 35f, 45f)
        val totalGridWidth = layout.cols * layout.photoWidthPt + (layout.cols - 1) * layout.gutterPt
        val totalGridHeight = layout.rows * layout.photoHeightPt + (layout.rows - 1) * layout.gutterPt
        // Side margin on each edge should be (sheet - grid) / 2 — verify by checking sum.
        val widthBalance = 2 * layout.marginLeftPt + totalGridWidth
        val heightBalance = 2 * layout.marginTopPt + totalGridHeight
        assertThat(widthBalance).isWithin(0.01f).of(layout.sheet.widthPt.toFloat())
        assertThat(heightBalance).isWithin(0.01f).of(layout.sheet.heightPt.toFloat())
    }

    @Test
    fun `gutter scales correctly from mm to pt`() {
        val layout = PrintSheetLayout.compute(SheetSize.A4, 35f, 45f, gutterMm = 2f)
        // 2 mm at 72 dpi = 2 / 25.4 * 72 ≈ 5.669 pt
        assertThat(layout.gutterPt).isWithin(0.01f).of(5.669f)
    }
}
