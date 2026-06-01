package com.kartik.snapdoc.data.billing

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EntitlementStateTest {

    @Test
    fun `locked state allows nothing`() {
        val state = EntitlementState.Locked
        assertThat(state.canExport).isFalse()
        assertThat(state.canPrintSheet).isFalse()
    }

    @Test
    fun `photo export alone allows export but not print sheet`() {
        val state = EntitlementState(photoExportUnlocked = true, studioBundleUnlocked = false)
        assertThat(state.canExport).isTrue()
        assertThat(state.canPrintSheet).isFalse()
    }

    @Test
    fun `studio bundle implies export entitlement`() {
        val state = EntitlementState(photoExportUnlocked = false, studioBundleUnlocked = true)
        assertThat(state.canExport).isTrue()
        assertThat(state.canPrintSheet).isTrue()
    }

    @Test
    fun `studio bundle plus photo export is consistent`() {
        val state = EntitlementState(photoExportUnlocked = true, studioBundleUnlocked = true)
        assertThat(state.canExport).isTrue()
        assertThat(state.canPrintSheet).isTrue()
    }

    @Test
    fun `product ids list contains both skus`() {
        assertThat(ProductIds.all).containsExactly(
            ProductIds.PHOTO_EXPORT,
            ProductIds.STUDIO_BUNDLE,
        )
    }
}
