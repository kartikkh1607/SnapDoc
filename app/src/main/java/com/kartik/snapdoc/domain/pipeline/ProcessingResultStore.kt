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
 *
 * Entries are evicted explicitly via [remove] once consumed (export saved, sheet saved,
 * user navigated away). A small LRU cap bounds memory if eviction is ever missed.
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

    private val byUri = object : LinkedHashMap<String, Entry>(8, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Entry>?): Boolean =
            size > MAX_ENTRIES
    }

    @Synchronized
    fun put(entry: Entry) {
        byUri[entry.processedUri.toString()] = entry
    }

    @Synchronized
    fun get(uri: String): Entry? = byUri[uri]

    @Synchronized
    fun remove(uri: String) {
        byUri.remove(uri)
    }

    @Synchronized
    fun clear() {
        byUri.clear()
    }

    private companion object {
        const val MAX_ENTRIES = 3
    }
}
