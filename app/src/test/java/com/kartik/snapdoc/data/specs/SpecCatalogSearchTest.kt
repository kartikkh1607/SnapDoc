package com.kartik.snapdoc.data.specs

import com.google.common.truth.Truth.assertThat
import com.kartik.snapdoc.data.specs.model.BackgroundSpec
import com.kartik.snapdoc.data.specs.model.CategorySpec
import com.kartik.snapdoc.data.specs.model.DimensionsSpec
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.data.specs.model.FaceSpec
import com.kartik.snapdoc.data.specs.model.FileSpec
import com.kartik.snapdoc.data.specs.model.RulesSpec
import com.kartik.snapdoc.data.specs.model.SpecCatalog
import org.junit.Test

class SpecCatalogSearchTest {

    private val repo = SpecCatalogRepository(FakeLoader())

    @Test
    fun `exact substring matches and ranks above looser matches`() {
        val results = repo.search("passport")

        assertThat(results.first().id).isEqualTo("in_passport")
    }

    @Test
    fun `acronym match surfaces the full-name document`() {
        val results = repo.search("pcc")

        assertThat(results.map { it.id }).contains("in_pcc")
        assertThat(results.first().id).isEqualTo("in_pcc")
    }

    @Test
    fun `prefix on first word matches even with no substring overlap`() {
        // "pas" should still hit "Passport".
        val results = repo.search("pas")

        assertThat(results.map { it.id }).contains("in_passport")
    }

    @Test
    fun `multi-token query ranks the doc that matches both tokens first`() {
        // "india visa" — both tokens land cleanly on in_visa's displayName + shortName,
        // outranking us_visa even though us_visa has higher popularity.
        val results = repo.search("india visa")

        assertThat(results.first().id).isEqualTo("in_visa")
    }

    @Test
    fun `blank query returns everything ordered by popularity`() {
        val results = repo.search("")

        assertThat(results.map { it.popularity }).isInOrder(Comparator.reverseOrder<Int>())
    }

    @Test
    fun `category filter applies before scoring`() {
        val results = repo.search("p", categoryId = "in_government")

        assertThat(results.map { it.categoryId }.toSet()).containsExactly("in_government")
    }

    private class FakeLoader : SpecCatalogLoader {
        override fun load(): SpecCatalog = SpecCatalog(
            version = 1,
            updatedAt = "2026-01-15",
            categories = listOf(
                CategorySpec("in_government", "Indian Government", "ic_govt"),
                CategorySpec("intl_visa", "International Visas", "ic_visa"),
            ),
            documents = listOf(
                doc("in_passport", "Indian Passport", "Passport", "in_government", 100),
                doc("in_pcc", "Police Clearance Certificate", "PCC", "in_government", 40),
                doc("in_visa", "Visa to India (Foreigners)", "India Visa", "in_government", 50),
                doc("us_visa", "US Visa (DS-160)", "US Visa", "intl_visa", 85),
            ),
        )

        private fun doc(
            id: String,
            displayName: String,
            shortName: String,
            categoryId: String,
            popularity: Int,
        ): DocumentSpec = DocumentSpec(
            id = id,
            displayName = displayName,
            shortName = shortName,
            categoryId = categoryId,
            popularity = popularity,
            dimensions = DimensionsSpec(35f, 45f, 413, 531, 300),
            background = BackgroundSpec("#FFFFFF", "White", 5),
            face = FaceSpec(70, 80, 50, 70),
            file = FileSpec("JPG", 10, 100),
            rules = RulesSpec(),
        )
    }
}
