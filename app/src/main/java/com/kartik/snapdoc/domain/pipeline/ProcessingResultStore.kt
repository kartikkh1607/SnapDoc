package com.kartik.snapdoc.domain.pipeline

import android.net.Uri
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds processed image metadata + validation results between Processing → Preview → Export
 * so downstream screens don't have to re-validate from disk.
 *
 * The processed JPEG itself lives on disk (cache dir) and is reachable via [Entry.processedUri].
 * Callers that need bytes invoke [Entry.readBytes], which streams from the cache file —
 * this keeps the store's memory footprint to ~kilobytes per entry instead of ~500KB+ each.
 *
 * Entries are evicted explicitly via [remove] once consumed (export saved, sheet saved,
 * user navigated away). A small LRU cap bounds growth if eviction is ever missed.
 */
@Singleton
class ProcessingResultStore @Inject constructor() {

    data class Entry(
        val processedUri: Uri,
        val sizeKb: Int,
        val widthPx: Int,
        val heightPx: Int,
        val validation: ValidationResult,
    ) {
        /** Reads the processed JPEG bytes from the cache file backing [processedUri]. */
        fun readBytes(): ByteArray {
            val path = processedUri.path ?: error("processedUri has no path: $processedUri")
            return File(path).readBytes()
        }
    }

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
