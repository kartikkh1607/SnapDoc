package com.kartik.snapdoc.ui.screens.home

import com.google.common.truth.Truth.assertThat
import com.kartik.snapdoc.data.specs.SpecCatalogLoader
import com.kartik.snapdoc.data.specs.SpecCatalogRepository
import com.kartik.snapdoc.data.specs.model.BackgroundSpec
import com.kartik.snapdoc.data.specs.model.CategorySpec
import com.kartik.snapdoc.data.specs.model.DimensionsSpec
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.data.specs.model.FaceSpec
import com.kartik.snapdoc.data.specs.model.FileSpec
import com.kartik.snapdoc.data.specs.model.RulesSpec
import com.kartik.snapdoc.data.specs.model.SpecCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repo = SpecCatalogRepository(FakeLoader())

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads categories and documents and clears loading`() = runTest {
        val viewModel = HomeViewModel(repo)

        val state = viewModel.state.value
        assertThat(state.loading).isFalse()
        assertThat(state.categories).hasSize(2)
        assertThat(state.documents).isNotEmpty()
    }

    @Test
    fun `init documents are ordered by popularity (blank search)`() = runTest {
        val viewModel = HomeViewModel(repo)

        val popularities = viewModel.state.value.documents.map { it.popularity }
        assertThat(popularities).isInOrder(Comparator.reverseOrder<Int>())
    }

    @Test
    fun `onCategorySelect filters documents to that category`() = runTest {
        val viewModel = HomeViewModel(repo)

        viewModel.onCategorySelect("in_government")

        val state = viewModel.state.value
        assertThat(state.selectedCategoryId).isEqualTo("in_government")
        assertThat(state.documents.map { it.categoryId }.toSet())
            .containsExactly("in_government")
    }

    @Test
    fun `onCategorySelect null restores the full document list`() = runTest {
        val viewModel = HomeViewModel(repo)
        viewModel.onCategorySelect("in_government")

        viewModel.onCategorySelect(null)

        val state = viewModel.state.value
        assertThat(state.selectedCategoryId).isNull()
        assertThat(state.documents.map { it.categoryId }.toSet())
            .containsAtLeast("in_government", "intl_visa")
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
                doc("in_passport", "Indian Passport", "in_government", 100),
                doc("in_pan", "PAN Card", "in_government", 90),
                doc("us_visa", "US Visa", "intl_visa", 85),
                doc("schengen_visa", "Schengen Visa", "intl_visa", 60),
            ),
        )

        private fun doc(
            id: String,
            displayName: String,
            categoryId: String,
            popularity: Int,
        ): DocumentSpec = DocumentSpec(
            id = id,
            displayName = displayName,
            shortName = displayName,
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
