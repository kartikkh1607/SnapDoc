package com.kartik.snapdoc.data.billing

object ProductIds {
    const val PHOTO_EXPORT = "snapdoc_photo_export"
    const val STUDIO_BUNDLE = "snapdoc_studio_bundle"

    val all: List<String> = listOf(PHOTO_EXPORT, STUDIO_BUNDLE)
}

data class EntitlementState(
    val photoExportUnlocked: Boolean = false,
    val studioBundleUnlocked: Boolean = false,
) {
    val canExport: Boolean get() = photoExportUnlocked || studioBundleUnlocked
    val canPrintSheet: Boolean get() = studioBundleUnlocked

    companion object {
        val Locked = EntitlementState()
    }
}
