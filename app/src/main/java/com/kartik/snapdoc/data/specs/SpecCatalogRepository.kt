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

    fun search(query: String, categoryId: String? = null): List<DocumentSpec> {
        val pool = if (categoryId == null) catalog.documents else byCategory(categoryId)
        if (query.isBlank()) return pool.sortedByDescending { it.popularity }
        val q = query.trim().lowercase()
        return pool.filter {
            it.displayName.lowercase().contains(q) || it.shortName.lowercase().contains(q)
        }.sortedByDescending { it.popularity }
    }
}
