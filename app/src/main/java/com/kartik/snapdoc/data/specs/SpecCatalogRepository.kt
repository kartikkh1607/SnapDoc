package com.kartik.snapdoc.data.specs

import com.kartik.snapdoc.data.specs.model.CategorySpec
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.data.specs.model.SpecCatalog
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

interface SpecCatalogRepository {
    fun all(): List<DocumentSpec>
    fun categories(): List<CategorySpec>
    fun byId(id: String): DocumentSpec?
    fun popular(limit: Int = 5): List<DocumentSpec>
    fun byCategory(categoryId: String): List<DocumentSpec>
    fun search(query: String, categoryId: String? = null): List<DocumentSpec>
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SpecCatalogRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSpecCatalogRepository(impl: DefaultSpecCatalogRepository): SpecCatalogRepository
}

@Singleton
class DefaultSpecCatalogRepository @Inject constructor(
    private val loader: SpecCatalogLoader,
) : SpecCatalogRepository {
    // Synchronized Lazy: whichever thread touches it first does the parse,
    // the rest block until it's ready. The init block below kicks the IO
    // thread off immediately so the main-thread first-access (via a VM) almost
    // always hits the cached value instead of doing the 22KB JSON parse on UI.
    private val catalogLazy: Lazy<SpecCatalog> = lazy { loader.load() }
    private val catalog: SpecCatalog get() = catalogLazy.value
    // Built once on first access so byId() stays O(1) regardless of catalog size.
    private val byIdIndex: Map<String, DocumentSpec> by lazy {
        catalog.documents.associateBy { it.id }
    }

    private val warmScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        warmScope.launch { catalogLazy.value }
    }

    override fun all(): List<DocumentSpec> = catalog.documents

    override fun categories(): List<CategorySpec> = catalog.categories

    override fun byId(id: String): DocumentSpec? = byIdIndex[id]

    override fun popular(limit: Int): List<DocumentSpec> =
        catalog.documents.sortedByDescending { it.popularity }.take(limit)

    override fun byCategory(categoryId: String): List<DocumentSpec> =
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
    override fun search(query: String, categoryId: String?): List<DocumentSpec> {
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
        if (display.contains(rawQuery)) score += W_DISPLAY_SUBSTRING
        if (short.contains(rawQuery)) score += W_SHORT_SUBSTRING
        if (notes.contains(rawQuery)) score += W_NOTES_SUBSTRING

        // Acronym match: "pcc" → "Police Clearance Certificate".
        if (display.acronym() == rawQuery) score += W_DISPLAY_ACRONYM
        if (short.acronym() == rawQuery) score += W_SHORT_ACRONYM

        // Per-token prefix presence on word boundaries.
        val displayWords = display.split(WHITESPACE)
        val shortWords = short.split(WHITESPACE)
        for (token in tokens) {
            if (displayWords.any { it.startsWith(token) }) score += W_DISPLAY_PREFIX
            else if (display.contains(token)) score += W_DISPLAY_CONTAINS
            if (shortWords.any { it.startsWith(token) }) score += W_SHORT_PREFIX
            if (notes.contains(token)) score += W_NOTES_TOKEN
        }
        return score
    }

    private fun String.acronym(): String =
        split(WHITESPACE).mapNotNull { it.firstOrNull()?.lowercaseChar() }.joinToString("")

    private companion object {
        val WHITESPACE = "\\s+".toRegex()

        // Search scoring weights. Picked so that whole-query and acronym hits
        // dominate, prefix hits matter, and notes only nudge the ranking.
        const val W_DISPLAY_SUBSTRING = 100
        const val W_DISPLAY_ACRONYM = 90
        const val W_SHORT_SUBSTRING = 80
        const val W_SHORT_ACRONYM = 70
        const val W_DISPLAY_PREFIX = 25
        const val W_SHORT_PREFIX = 20
        const val W_NOTES_SUBSTRING = 20
        const val W_DISPLAY_CONTAINS = 10
        const val W_NOTES_TOKEN = 5
    }
}
