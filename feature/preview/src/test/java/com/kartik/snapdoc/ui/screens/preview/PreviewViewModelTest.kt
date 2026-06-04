package com.kartik.snapdoc.ui.screens.preview

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.kartik.snapdoc.data.billing.BillingError
import com.kartik.snapdoc.data.billing.EntitlementState
import com.kartik.snapdoc.data.billing.PurchaseRepository
import com.kartik.snapdoc.data.specs.SpecCatalogRepository
import com.kartik.snapdoc.data.specs.model.BackgroundSpec
import com.kartik.snapdoc.data.specs.model.CategorySpec
import com.kartik.snapdoc.data.specs.model.DimensionsSpec
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.data.specs.model.FaceSpec
import com.kartik.snapdoc.data.specs.model.FileSpec
import com.kartik.snapdoc.data.specs.model.RulesSpec
import com.kartik.snapdoc.domain.pipeline.ProcessingResultStore
import com.kartik.snapdoc.domain.pipeline.ValidationCheck
import com.kartik.snapdoc.domain.pipeline.ValidationCheckKind
import com.kartik.snapdoc.domain.pipeline.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class PreviewViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state hydrates from result store entry`() = runTest {
        val resultStore = ProcessingResultStore()
        val entryUri = Uri.parse("file:///tmp/processed.jpg")
        resultStore.put(
            ProcessingResultStore.Entry(
                processedUri = entryUri,
                sizeKb = 75,
                widthPx = 413,
                heightPx = 531,
                validation = ValidationResult(
                    passed = true,
                    checks = listOf(
                        ValidationCheck(
                            kind = ValidationCheckKind.Dimensions,
                            expected = "413 × 531 px",
                            actual = "413 × 531 px",
                            passed = true,
                        ),
                    ),
                ),
            ),
        )

        val viewModel = PreviewViewModel(
            savedState(entryUri.toString()),
            FakeRepo(),
            resultStore,
            FakePurchases(EntitlementState.Locked),
        )

        val state = viewModel.state.value
        assertThat(state.processedUri).isEqualTo(entryUri)
        assertThat(state.sizeKb).isEqualTo(75)
        assertThat(state.widthPx).isEqualTo(413)
        assertThat(state.heightPx).isEqualTo(531)
        assertThat(state.allPassed).isTrue()
        assertThat(state.checks).hasSize(1)
    }

    @Test
    fun `watermarked is true when entitlement is locked`() = runTest {
        val viewModel = PreviewViewModel(
            savedState(),
            FakeRepo(),
            ProcessingResultStore(),
            FakePurchases(EntitlementState.Locked),
        )

        assertThat(viewModel.state.value.watermarked).isTrue()
    }

    @Test
    fun `watermarked is false when photo export is unlocked`() = runTest {
        val viewModel = PreviewViewModel(
            savedState(),
            FakeRepo(),
            ProcessingResultStore(),
            FakePurchases(EntitlementState(photoExportUnlocked = true, studioBundleUnlocked = false)),
        )

        assertThat(viewModel.state.value.watermarked).isFalse()
    }

    @Test
    fun `unknown processed uri leaves placeholder state with empty checks`() = runTest {
        val viewModel = PreviewViewModel(
            savedState(),
            FakeRepo(),
            ProcessingResultStore(),
            FakePurchases(EntitlementState.Locked),
        )

        val state = viewModel.state.value
        assertThat(state.checks).isEmpty()
        assertThat(state.allPassed).isFalse()
        assertThat(state.sizeKb).isEqualTo(0)
    }

    private fun savedState(imageUri: String = IMAGE_URI): SavedStateHandle =
        SavedStateHandle(mapOf("docId" to DOC_ID, "imageUri" to imageUri))

    private class FakeRepo : SpecCatalogRepository {
        private val passport = DocumentSpec(
            id = DOC_ID,
            displayName = "Indian Passport",
            shortName = "Passport",
            categoryId = "in_government",
            popularity = 100,
            dimensions = DimensionsSpec(35f, 45f, 413, 531, 300),
            background = BackgroundSpec("#FFFFFF", "White", 5),
            face = FaceSpec(70, 80, 50, 70),
            file = FileSpec("JPG", 10, 100),
            rules = RulesSpec(),
        )
        override fun all(): List<DocumentSpec> = listOf(passport)
        override fun categories(): List<CategorySpec> =
            listOf(CategorySpec("in_government", "Indian Government", "ic_govt"))
        override fun byId(id: String): DocumentSpec? = passport.takeIf { id == DOC_ID }
        override fun popular(limit: Int): List<DocumentSpec> = listOf(passport)
        override fun byCategory(categoryId: String): List<DocumentSpec> = listOf(passport)
        override fun search(query: String, categoryId: String?): List<DocumentSpec> = listOf(passport)
    }

    private class FakePurchases(initial: EntitlementState) : PurchaseRepository {
        private val _entitlement = MutableStateFlow(initial)
        override val entitlement: StateFlow<EntitlementState> = _entitlement.asStateFlow()
        private val _errors = MutableSharedFlow<BillingError>()
        override val errors: SharedFlow<BillingError> = _errors.asSharedFlow()
        override suspend fun launchPurchase(activity: Activity, productId: String): Boolean = true
        override suspend fun restorePurchases() = Unit
    }

    private companion object {
        const val DOC_ID = "in_passport"
        const val IMAGE_URI = "file:///tmp/unknown.jpg"
    }
}
