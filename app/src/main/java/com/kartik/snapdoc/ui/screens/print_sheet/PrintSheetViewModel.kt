package com.kartik.snapdoc.ui.screens.print_sheet

import android.graphics.BitmapFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kartik.snapdoc.data.billing.PurchaseRepository
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
    purchases: PurchaseRepository,
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
        viewModelScope.launch {
            purchases.entitlement.collect { e -> _state.update { it.copy(entitlement = e) } }
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

    fun savePdf() {
        val s = _state.value
        val doc = s.doc ?: return
        val layout = s.layout ?: return
        val entry = resultStore.get(decodedUri) ?: run {
            _state.update { it.copy(phase = PrintExportPhase.Error, error = "Processed photo missing — retake.") }
            return
        }
        _state.update { it.copy(phase = PrintExportPhase.Generating, error = null) }
        viewModelScope.launch {
            runCatching {
                val photo = BitmapFactory.decodeByteArray(entry.rawJpegBytes, 0, entry.rawJpegBytes.size)
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
            }.onFailure { t ->
                _state.update { it.copy(phase = PrintExportPhase.Error, error = t.message ?: "PDF export failed") }
            }
        }
    }

    fun saveJpg() {
        val s = _state.value
        val doc = s.doc ?: return
        val layout = s.layout ?: return
        val entry = resultStore.get(decodedUri) ?: run {
            _state.update { it.copy(phase = PrintExportPhase.Error, error = "Processed photo missing — retake.") }
            return
        }
        _state.update { it.copy(phase = PrintExportPhase.Generating, error = null) }
        viewModelScope.launch {
            runCatching {
                val photo = BitmapFactory.decodeByteArray(entry.rawJpegBytes, 0, entry.rawJpegBytes.size)
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
            }.onFailure { t ->
                _state.update { it.copy(phase = PrintExportPhase.Error, error = t.message ?: "JPG export failed") }
            }
        }
    }

    fun resetPhase() {
        _state.update { it.copy(phase = PrintExportPhase.Idle, error = null) }
    }
}
