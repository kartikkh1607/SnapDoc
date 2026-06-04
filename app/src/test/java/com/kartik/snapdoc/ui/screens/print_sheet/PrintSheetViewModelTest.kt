package com.kartik.snapdoc.ui.screens.print_sheet

import android.app.Activity
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
import com.kartik.snapdoc.domain.export.DocumentExporter
import com.kartik.snapdoc.domain.export.PhotoExporter
import com.kartik.snapdoc.domain.pipeline.ProcessingResultStore
import com.kartik.snapdoc.domain.print.PrintSheetGenerator
import com.kartik.snapdoc.domain.print.SheetSize
import io.mockk.mockk
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
class PrintSheetViewModelTest {

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
    fun `init computes initial layout for the default A4 sheet`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.state.value
        assertThat(state.doc).isNotNull()
        assertThat(state.sheet).isEqualTo(SheetSize.A4)
        assertThat(state.layout).isNotNull()
        // Verify the layout placed at least one photo on the sheet.
        assertThat(state.layout!!.copies).isGreaterThan(0)
    }

    @Test
    fun `selectSheet recomputes layout for the new sheet size`() = runTest {
        val viewModel = createViewModel()
        val a4Layout = viewModel.state.value.layout!!

        viewModel.selectSheet(SheetSize.FourBySix)

        val state = viewModel.state.value
        assertThat(state.sheet).isEqualTo(SheetSize.FourBySix)
        // 4x6 is smaller than A4, so different placement count is expected.
        assertThat(state.layout).isNotNull()
        assertThat(state.layout).isNotEqualTo(a4Layout)
    }

    @Test
    fun `locked reflects entitlement state and tracks unlock`() = runTest {
        val purchases = FakePurchases(EntitlementState.Locked)
        val viewModel = createViewModel(purchases = purchases)

        assertThat(viewModel.state.value.locked).isTrue()

        purchases.unlock(EntitlementState(photoExportUnlocked = true, studioBundleUnlocked = true))

        assertThat(viewModel.state.value.locked).isFalse()
    }

    @Test
    fun `resetPhase clears any error and returns to Idle`() = runTest {
        val viewModel = createViewModel()
        // Trigger an error path by trying to save a PDF without a stored entry.
        viewModel.savePdf()
        assertThat(viewModel.state.value.phase).isEqualTo(PrintExportPhase.Error)

        viewModel.resetPhase()

        assertThat(viewModel.state.value.phase).isEqualTo(PrintExportPhase.Idle)
        assertThat(viewModel.state.value.errorRes).isNull()
    }

    private fun createViewModel(
        purchases: PurchaseRepository = FakePurchases(EntitlementState.Locked),
    ): PrintSheetViewModel = PrintSheetViewModel(
        savedStateHandle = SavedStateHandle(mapOf("docId" to DOC_ID, "imageUri" to "file:///tmp/in.jpg")),
        repo = FakeRepo(),
        resultStore = ProcessingResultStore(),
        generator = mockk<PrintSheetGenerator>(),
        documentExporter = mockk<DocumentExporter>(),
        photoExporter = mockk<PhotoExporter>(),
        purchases = purchases,
    )

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

        fun unlock(state: EntitlementState) {
            _entitlement.value = state
        }
    }

    private companion object {
        const val DOC_ID = "in_passport"
    }
}
