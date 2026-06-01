package com.kartik.snapdoc.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SETTINGS = "settings"

    private const val DOC_DETAIL_BASE = "docDetail"
    const val DOC_DETAIL = "$DOC_DETAIL_BASE/{docId}"
    fun docDetail(docId: String) = "$DOC_DETAIL_BASE/$docId"

    private const val CAMERA_BASE = "camera"
    const val CAMERA = "$CAMERA_BASE/{docId}"
    fun camera(docId: String) = "$CAMERA_BASE/$docId"

    private const val REVIEW_BASE = "review"
    const val REVIEW = "$REVIEW_BASE/{docId}/{imageUri}"
    fun review(docId: String, imageUri: String) = "$REVIEW_BASE/$docId/${java.net.URLEncoder.encode(imageUri, "UTF-8")}"

    private const val PROCESSING_BASE = "processing"
    const val PROCESSING = "$PROCESSING_BASE/{docId}/{imageUri}"
    fun processing(docId: String, imageUri: String) = "$PROCESSING_BASE/$docId/${java.net.URLEncoder.encode(imageUri, "UTF-8")}"

    private const val PREVIEW_BASE = "preview"
    const val PREVIEW = "$PREVIEW_BASE/{docId}/{imageUri}"
    fun preview(docId: String, imageUri: String) = "$PREVIEW_BASE/$docId/${java.net.URLEncoder.encode(imageUri, "UTF-8")}"

    private const val EXPORT_BASE = "export"
    const val EXPORT = "$EXPORT_BASE/{docId}/{imageUri}"
    fun export(docId: String, imageUri: String) = "$EXPORT_BASE/$docId/${java.net.URLEncoder.encode(imageUri, "UTF-8")}"

    private const val PRINT_SHEET_BASE = "printSheet"
    const val PRINT_SHEET = "$PRINT_SHEET_BASE/{docId}/{imageUri}"
    fun printSheet(docId: String, imageUri: String) = "$PRINT_SHEET_BASE/$docId/${java.net.URLEncoder.encode(imageUri, "UTF-8")}"

    object Args {
        const val DOC_ID = "docId"
        const val IMAGE_URI = "imageUri"
    }
}
