package com.kartik.snapdoc.ui.screens.print_sheet

import android.app.Activity
import android.graphics.BitmapFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kartik.snapdoc.R
import com.kartik.snapdoc.data.specs.SpecCatalogRepository
import com.kartik.snapdoc.domain.export.DocumentExporter
import com.kartik.snapdoc.domain.export.PhotoExporter
import com.kartik.snapdoc.domain.pipeline.ProcessingResultStore
import com.kartik.snapdoc.domain.print.PrintSheetGenerator
import com.kartik.snapdoc.domain.print.PrintSheetLayout
import com.kartik.snapdoc.domain.print.SheetSize
import com.kartik.snapdoc.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrintSheetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: SpecCatalogRepository,
    private val resultStore: ProcessingResultStore,
    private val generator: PrintSheetGenerator,
    private val documentExporter: DocumentExporter,
    private val photoExporter: PhotoExporter,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<Routes.PrintSheet>()
    val docId: String = args.docId
    private val decodedUri: String = args.imageUri

    private val _state = MutableStateFlow(PrintSheetUiState())
    val state: StateFlow<PrintSheetUiState> = _state.asStateFlow()

    init {
        val doc = repo.byId(docId)
        _state.update { it.copy(doc = doc) }
        if (doc != null) {
            _state.update {
                it.copy(layout = PrintSheetLayout.compute(it.sheet, doc.dimensions.widthMm, doc.dimensions.heightMm))
            }
        }
    }

    fun selectSheet(sheet: SheetSize) {
        val doc = _state.value.doc ?: return
        _state.update {
            it.copy(
                sheet = sheet,
                layout = PrintSheetLayout.compute(sheet, doc.dimensions.widthMm, doc.dimensions.heightMm),
            )
        }
    }

    fun savePdf(activity: Activity? = null) {
        val s = _state.value
        val doc = s.doc ?: return
        val layout = s.layout ?: return
        val entry = resultStore.get(decodedUri) ?: run {
            _state.update {
                it.copy(phase = PrintExportPhase.Error, errorRes = R.string.printsheet_error_missing)
            }
            return
        }
        _state.update { it.copy(phase = PrintExportPhase.Generating, errorRes = null) }
        viewModelScope.launch {
            runCatching {
                val bytes = entry.readBytes()
                val photo = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val pdf = generator.generatePdf(photo, layout, doc)
                photo.recycle()
                val publicUri = documentExporter.savePdfToDownloads(
                    source = pdf,
                    displayName = "snapdoc_${doc.id}_${layout.sheet.name}_${System.currentTimeMillis()}.pdf",
                )
                val shareUri = documentExporter.shareUri(pdf)
                publicUri to shareUri
            }.onSuccess { (saved, share) ->
                _state.update {
                    it.copy(
                        phase = PrintExportPhase.Saved,
                        savedUri = saved,
                        shareUri = share,
                        shareMime = "application/pdf",
                    )
                }
                // No interstitial — app is fully free.
            }.onFailure {
                _state.update {
                    it.copy(phase = PrintExportPhase.Error, errorRes = R.string.printsheet_error_pdf)
                }
            }
        }
    }

    fun saveJpg(activity: Activity? = null) {
        val s = _state.value
        val doc = s.doc ?: return
        val layout = s.layout ?: return
        val entry = resultStore.get(decodedUri) ?: run {
            _state.update {
                it.copy(phase = PrintExportPhase.Error, errorRes = R.string.printsheet_error_missing)
            }
            return
        }
        _state.update { it.copy(phase = PrintExportPhase.Generating, errorRes = null) }
        viewModelScope.launch {
            runCatching {
                val bytes = entry.readBytes()
                val photo = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val jpegBytes = generator.generateJpegBytes(photo, layout)
                photo.recycle()
                photoExporter.saveToGallery(jpegBytes, "${doc.id}_sheet")
            }.onSuccess { uri ->
                _state.update {
                    it.copy(
                        phase = PrintExportPhase.Saved,
                        savedUri = uri,
                        shareUri = uri,
                        shareMime = "image/jpeg",
                    )
                }
                // No interstitial — app is fully free.
            }.onFailure {
                _state.update {
                    it.copy(phase = PrintExportPhase.Error, errorRes = R.string.printsheet_error_jpg)
                }
            }
        }
    }

    fun resetPhase() {
        _state.update { it.copy(phase = PrintExportPhase.Idle, errorRes = null) }
    }
}
