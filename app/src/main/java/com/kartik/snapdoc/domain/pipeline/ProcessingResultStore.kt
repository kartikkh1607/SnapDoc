package com.kartik.snapdoc.domain.pipeline

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds processed image bytes + validation results between Processing → Preview → Export
 * so downstream screens don't have to re-read or re-validate from disk.
 *
 * Why in-memory: nav args can only carry URIs, and we want the validation result
 * to be available immediately on Preview without re-running ML Kit.
 */
@Singleton
class ProcessingResultStore @Inject constructor() {

    data class Entry(
        val processedUri: Uri,
        val rawJpegBytes: ByteArray,
        val sizeKb: Int,
        val widthPx: Int,
        val heightPx: Int,
        val validation: ValidationResult,
    )

    private val byUri = mutableMapOf<String, Entry>()

    fun put(entry: Entry) {
        byUri[entry.processedUri.toString()] = entry
    }

    fun get(uri: String): Entry? = byUri[uri]

    fun clear() {
        byUri.clear()
    }
}
