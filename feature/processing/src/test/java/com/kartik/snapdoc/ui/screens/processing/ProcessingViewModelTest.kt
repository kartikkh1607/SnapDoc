package com.kartik.snapdoc.ui.screens.processing

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kartik.snapdoc.data.specs.SpecCatalogRepository
import com.kartik.snapdoc.data.specs.model.BackgroundSpec
import com.kartik.snapdoc.data.specs.model.CategorySpec
import com.kartik.snapdoc.data.specs.model.DimensionsSpec
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.data.specs.model.FaceSpec
import com.kartik.snapdoc.data.specs.model.FileSpec
import com.kartik.snapdoc.data.specs.model.RulesSpec
import com.kartik.snapdoc.domain.pipeline.PhotoProcessor
import com.kartik.snapdoc.domain.pipeline.PipelineFailureReason
import com.kartik.snapdoc.domain.pipeline.ProcessingOutcome
import com.kartik.snapdoc.domain.pipeline.ProcessingResultStore
import com.kartik.snapdoc.domain.pipeline.ProcessingStage
import com.kartik.snapdoc.domain.pipeline.ValidationResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class ProcessingViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repo = FakeRepo()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `success path emits Done event and clears error`() = runTest {
        val processor = fakeProcessor()
        val processedUri = Uri.parse("file:///tmp/out.jpg")
        coEvery { processor.process(any(), any()) } returns ProcessingOutcome.Success(
            ProcessingResultStore.Entry(
                processedUri = processedUri,
                sizeKb = 50,
                widthPx = 413,
                heightPx = 531,
                validation = ValidationResult(passed = true, checks = emptyList()),
            ),
        )

        val viewModel = ProcessingViewModel(savedState(), repo, processor)

        viewModel.events.test {
            val event = awaitItem()
            assertThat(event).isInstanceOf(ProcessingEvent.Done::class.java)
            event as ProcessingEvent.Done
            assertThat(event.docId).isEqualTo(DOC_ID)
            assertThat(event.imageUri).isEqualTo(processedUri.toString())
        }
        assertThat(viewModel.state.value.error).isNull()
        assertThat(viewModel.state.value.stage).isEqualTo(ProcessingStage.Done)
    }

    @Test
    fun `failure path surfaces typed reason and does not emit Done`() = runTest {
        val processor = fakeProcessor()
        coEvery { processor.process(any(), any()) } returns
            ProcessingOutcome.Failure(PipelineFailureReason.NoFaceDetected)

        val viewModel = ProcessingViewModel(savedState(), repo, processor)

        viewModel.events.test {
            expectNoEvents()
        }
        assertThat(viewModel.state.value.error).isEqualTo(PipelineFailureReason.NoFaceDetected)
    }

    @Test
    fun `unknown document records UnknownDocument reason`() = runTest {
        val processor = fakeProcessor()

        val viewModel = ProcessingViewModel(savedState(docId = "not_in_catalog"), repo, processor)

        assertThat(viewModel.state.value.error).isEqualTo(PipelineFailureReason.UnknownDocument)
    }

    @Test
    fun `retry re-runs the pipeline after a failure`() = runTest {
        val processor = fakeProcessor()
        var call = 0
        coEvery { processor.process(any(), any()) } answers {
            call++
            if (call == 1) ProcessingOutcome.Failure(PipelineFailureReason.NoFaceDetected)
            else ProcessingOutcome.Success(
                ProcessingResultStore.Entry(
                    processedUri = Uri.parse("file:///tmp/retry.jpg"),
                    sizeKb = 40,
                    widthPx = 413,
                    heightPx = 531,
                    validation = ValidationResult(passed = true, checks = emptyList()),
                ),
            )
        }

        val viewModel = ProcessingViewModel(savedState(), repo, processor)
        assertThat(viewModel.state.value.error).isEqualTo(PipelineFailureReason.NoFaceDetected)

        viewModel.retry()

        assertThat(viewModel.state.value.error).isNull()
        assertThat(viewModel.state.value.stage).isEqualTo(ProcessingStage.Done)
    }

    @Test
    fun `retry is a no-op when there is no error`() = runTest {
        val processor = fakeProcessor()
        var callCount = 0
        coEvery { processor.process(any(), any()) } answers {
            callCount++
            ProcessingOutcome.Success(
                ProcessingResultStore.Entry(
                    processedUri = Uri.parse("file:///tmp/ok.jpg"),
                    sizeKb = 40,
                    widthPx = 413,
                    heightPx = 531,
                    validation = ValidationResult(passed = true, checks = emptyList()),
                ),
            )
        }

        val viewModel = ProcessingViewModel(savedState(), repo, processor)
        assertThat(callCount).isEqualTo(1)

        viewModel.retry()

        assertThat(callCount).isEqualTo(1)
    }

    private fun fakeProcessor(): PhotoProcessor {
        val processor = mockk<PhotoProcessor>()
        every { processor.stage } returns MutableStateFlow(ProcessingStage.DetectingFace)
        return processor
    }

    private fun savedState(docId: String = DOC_ID, imageUri: String = IMAGE_URI): SavedStateHandle =
        SavedStateHandle(mapOf("docId" to docId, "imageUri" to imageUri))

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

    private companion object {
        const val DOC_ID = "in_passport"
        const val IMAGE_URI = "file:///tmp/input.jpg"
    }
}
