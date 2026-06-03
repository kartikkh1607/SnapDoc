package com.kartik.snapdoc.domain.pipeline

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ProcessingResultStoreTest {

    private val store = ProcessingResultStore()

    @Test
    fun `get returns the entry put under the same uri key`() {
        val entry = makeEntry("file:///cache/a.jpg")
        store.put(entry)

        assertThat(store.get("file:///cache/a.jpg")).isEqualTo(entry)
    }

    @Test
    fun `get returns null for unknown uris`() {
        store.put(makeEntry("file:///cache/a.jpg"))

        assertThat(store.get("file:///cache/missing.jpg")).isNull()
    }

    @Test
    fun `remove evicts a single entry without affecting others`() {
        store.put(makeEntry("file:///cache/a.jpg"))
        store.put(makeEntry("file:///cache/b.jpg"))

        store.remove("file:///cache/a.jpg")

        assertThat(store.get("file:///cache/a.jpg")).isNull()
        assertThat(store.get("file:///cache/b.jpg")).isNotNull()
    }

    @Test
    fun `clear removes all entries`() {
        store.put(makeEntry("file:///cache/a.jpg"))
        store.put(makeEntry("file:///cache/b.jpg"))

        store.clear()

        assertThat(store.get("file:///cache/a.jpg")).isNull()
        assertThat(store.get("file:///cache/b.jpg")).isNull()
    }

    @Test
    fun `inserting a fourth entry evicts the least-recently-used`() {
        // Cap is 3 — see ProcessingResultStore.MAX_ENTRIES.
        store.put(makeEntry("uri://a"))
        store.put(makeEntry("uri://b"))
        store.put(makeEntry("uri://c"))

        // Touch 'a' so 'b' becomes the eldest.
        store.get("uri://a")
        store.put(makeEntry("uri://d"))

        assertThat(store.get("uri://b")).isNull()
        assertThat(store.get("uri://a")).isNotNull()
        assertThat(store.get("uri://c")).isNotNull()
        assertThat(store.get("uri://d")).isNotNull()
    }

    private fun makeEntry(uri: String): ProcessingResultStore.Entry =
        ProcessingResultStore.Entry(
            processedUri = Uri.parse(uri),
            sizeKb = 50,
            widthPx = 413,
            heightPx = 531,
            validation = ValidationResult(passed = true, checks = emptyList()),
        )
}
