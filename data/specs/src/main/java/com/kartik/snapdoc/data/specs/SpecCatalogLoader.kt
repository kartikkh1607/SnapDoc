package com.kartik.snapdoc.data.specs

import android.content.Context
import com.kartik.snapdoc.data.specs.model.BackgroundSpec
import com.kartik.snapdoc.data.specs.model.CategorySpec
import com.kartik.snapdoc.data.specs.model.DimensionsSpec
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.data.specs.model.FaceSpec
import com.kartik.snapdoc.data.specs.model.FileSpec
import com.kartik.snapdoc.data.specs.model.RulesSpec
import com.kartik.snapdoc.data.specs.model.SpecCatalog
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpecCatalogLoaderModule {
    @Binds
    @Singleton
    abstract fun bindSpecCatalogLoader(impl: AssetSpecCatalogLoader): SpecCatalogLoader
}

interface SpecCatalogLoader {
    fun load(): SpecCatalog
}

@Singleton
class AssetSpecCatalogLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpecCatalogLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun load(): SpecCatalog = runCatching {
        val raw = context.assets.open(CATALOG_PATH).bufferedReader().use { it.readText() }
        json.decodeFromString<SpecCatalog>(raw)
    }.getOrElse {
        android.util.Log.e("SpecCatalogLoader", "Falling back to in-code catalog", it)
        FALLBACK
    }

    companion object {
        private const val CATALOG_PATH = "specs/catalog_v1.json"

        private val FALLBACK = SpecCatalog(
            version = 1,
            updatedAt = "2026-01-15",
            categories = listOf(
                CategorySpec("in_government", "Indian Government", "ic_govt"),
                CategorySpec("custom", "Custom", "ic_custom"),
            ),
            documents = listOf(
                DocumentSpec(
                    id = "in_passport",
                    displayName = "Indian Passport",
                    shortName = "Passport",
                    categoryId = "in_government",
                    popularity = 100,
                    dimensions = DimensionsSpec(35f, 45f, 413, 531, 300),
                    background = BackgroundSpec("#FFFFFF", "White"),
                    face = FaceSpec(70, 80, 50, 70),
                    file = FileSpec("JPG", 10, 100),
                    rules = RulesSpec(),
                ),
            ),
        )
    }
}
