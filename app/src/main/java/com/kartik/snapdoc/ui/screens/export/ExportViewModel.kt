package com.kartik.snapdoc.ui.screens.export

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kartik.snapdoc.data.billing.BillingError
import com.kartik.snapdoc.data.billing.EntitlementState
import com.kartik.snapdoc.data.billing.ProductIds
import com.kartik.snapdoc.data.billing.PurchaseRepository
import com.kartik.snapdoc.domain.export.PhotoExporter
import com.kartik.snapdoc.domain.pipeline.ProcessingResultStore
import com.kartik.snapdoc.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val purchases: PurchaseRepository,
    private val resultStore: ProcessingResultStore,
    private val exporter: PhotoExporter,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<Routes.Export>()
    val docId: String = args.docId
    private val decodedUri: String = args.imageUri

    private val _state = MutableStateFlow(ExportUiState(selectedProductId = ProductIds.PHOTO_EXPORT))
    val state: StateFlow<ExportUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            purchases.entitlement.collect { entitlement ->
                _state.update { current ->
                    val nextPhase = when {
                        current.phase == ExportPhase.Saved -> current.phase
                        current.phase == ExportPhase.Saving -> current.phase
                        entitlement.canExport && current.savedUri == null -> ExportPhase.Saving
                        else -> current.phase
                    }
                    current.copy(entitlement = entitlement, phase = nextPhase)
                }
                if (entitlement.canExport && _state.value.savedUri == null && _state.value.phase != ExportPhase.Saved) {
                    save()
                }
            }
        }
        viewModelScope.launch {
            purchases.errors.collect { error ->
                val message = error.userMessage()
                _state.update {
                    val nextPhase = if (it.phase == ExportPhase.Purchasing) ExportPhase.Paywall else it.phase
                    it.copy(phase = nextPhase, error = message)
                }
            }
        }
    }

    private fun BillingError.userMessage(): String = when (this) {
        is BillingError.ConnectionFailed ->
            "Couldn't reach Google Play. Check your connection and try again."
        is BillingError.PurchaseFailed ->
            "Payment didn't go through. Try again or use a different payment method."
    }

    fun selectProduct(productId: String) {
        _state.update { it.copy(selectedProductId = productId) }
    }

    fun pay(activity: Activity) {
        val productId = _state.value.selectedProductId ?: ProductIds.PHOTO_EXPORT
        _state.update { it.copy(phase = ExportPhase.Purchasing, error = null) }
        val launched = purchases.launchPurchase(activity, productId)
        if (!launched) {
            _state.update {
                it.copy(
                    phase = ExportPhase.Paywall,
                    error = "Payments aren't available right now. Try again in a moment.",
                )
            }
        }
    }

    private fun save() {
        viewModelScope.launch {
            val entry = resultStore.get(decodedUri)
            if (entry == null) {
                _state.update { it.copy(phase = ExportPhase.Error, error = "Processed photo missing — retake.") }
                return@launch
            }
            runCatching { exporter.saveToGallery(entry.rawJpegBytes, docId) }
                .onSuccess { uri ->
                    _state.update { it.copy(phase = ExportPhase.Saved, savedUri = uri) }
                }
                .onFailure { t ->
                    _state.update { it.copy(phase = ExportPhase.Error, error = t.message ?: "Save failed") }
                }
        }
    }

    fun retrySave() {
        _state.update { it.copy(phase = ExportPhase.Saving, error = null) }
        save()
    }

    fun restore() {
        viewModelScope.launch { purchases.restorePurchases() }
    }
}
