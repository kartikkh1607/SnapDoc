package com.kartik.snapdoc.data.specs

import com.kartik.snapdoc.data.specs.model.CategorySpec
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.data.specs.model.SpecCatalog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpecCatalogRepository @Inject constructor(
    private val loader: SpecCatalogLoader,
) {
    private val catalog: SpecCatalog by lazy { loader.load() }

    fun all(): List<DocumentSpec> = catalog.documents

    fun categories(): List<CategorySpec> = catalog.categories

    fun byId(id: String): DocumentSpec? = catalog.documents.firstOrNull { it.id == id }

    fun popular(limit: Int = 5): List<DocumentSpec> =
        catalog.documents.sortedByDescending { it.popularity }.take(limit)

    fun byCategory(categoryId: String): List<DocumentSpec> =
        catalog.documents.filter { it.categoryId == categoryId }

    /**
     * Token-aware fuzzy search across displayName + shortName + notes.
     *
     * Scoring blends:
     *  - exact substring presence (large boost)
     *  - per-token prefix hits (helps "pas" → "Passport")
     *  - acronym match against word-initial letters ("pcc" → "Police Clearance Certificate")
     *  - popularity (tie-breaker so well-known docs rank above niche ones)
     *
     * Docs scoring 0 are filtered out so we never show random results.
     */
    fun search(query: String, categoryId: String? = null): List<DocumentSpec> {
        val pool = if (categoryId == null) catalog.documents else byCategory(categoryId)
        if (query.isBlank()) return pool.sortedByDescending { it.popularity }
        val tokens = query.trim().lowercase().split(WHITESPACE).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return pool.sortedByDescending { it.popularity }
        val rawQuery = query.trim().lowercase()

        return pool
            .mapNotNull { doc ->
                val score = score(doc, rawQuery, tokens)
                if (score <= 0) null else doc to score
            }
            .sortedWith(compareByDescending<Pair<DocumentSpec, Int>> { it.second }.thenByDescending { it.first.popularity })
            .map { it.first }
    }

    private fun score(doc: DocumentSpec, rawQuery: String, tokens: List<String>): Int {
        val display = doc.displayName.lowercase()
        val short = doc.shortName.lowercase()
        val notes = doc.notes.lowercase()
        var score = 0

        // Whole-query substring matches — strongest signal.
        if (display.contains(rawQuery)) score += 100
        if (short.contains(rawQuery)) score += 80
        if (notes.contains(rawQuery)) score += 20

        // Acronym match: "pcc" → "Police Clearance Certificate".
        if (display.acronym() == rawQuery) score += 90
        if (short.acronym() == rawQuery) score += 70

        // Per-token prefix presence on word boundaries.
        val displayWords = display.split(WHITESPACE)
        val shortWords = short.split(WHITESPACE)
        for (token in tokens) {
            if (displayWords.any { it.startsWith(token) }) score += 25
            else if (display.contains(token)) score += 10
            if (shortWords.any { it.startsWith(token) }) score += 20
            if (notes.contains(token)) score += 5
        }
        return score
    }

    private fun String.acronym(): String =
        split(WHITESPACE).mapNotNull { it.firstOrNull()?.lowercaseChar() }.joinToString("")

    private companion object {
        val WHITESPACE = "\\s+".toRegex()
    }
}
