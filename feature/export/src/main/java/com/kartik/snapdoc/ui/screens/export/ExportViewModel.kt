package com.kartik.snapdoc.ui.screens.export

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import android.util.Log
import com.kartik.snapdoc.feature.export.R
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
                val messageRes = error.userMessageRes()
                _state.update {
                    val nextPhase = if (it.phase == ExportPhase.Purchasing) ExportPhase.Paywall else it.phase
                    it.copy(phase = nextPhase, errorRes = messageRes)
                }
            }
        }
    }

    private fun BillingError.userMessageRes(): Int = when (this) {
        is BillingError.ConnectionFailed -> R.string.export_error_connection
        is BillingError.PurchaseFailed -> R.string.export_error_purchase_failed
    }

    fun selectProduct(productId: String) {
        _state.update { it.copy(selectedProductId = productId) }
    }

    fun pay(activity: Activity) {
        val productId = _state.value.selectedProductId ?: ProductIds.PHOTO_EXPORT
        _state.update { it.copy(phase = ExportPhase.Purchasing, errorRes = null) }
        viewModelScope.launch {
            val launched = purchases.launchPurchase(activity, productId)
            if (!launched) {
                _state.update {
                    it.copy(
                        phase = ExportPhase.Paywall,
                        errorRes = R.string.export_error_purchase_unavailable,
                    )
                }
            }
        }
    }

    private fun save() {
        viewModelScope.launch {
            val entry = resultStore.get(decodedUri)
            if (entry == null) {
                _state.update {
                    it.copy(phase = ExportPhase.Error, errorRes = R.string.export_error_missing_processed)
                }
                return@launch
            }
            runCatching { exporter.saveToGallery(entry.readBytes(), docId) }
                .onSuccess { uri ->
                    _state.update { it.copy(phase = ExportPhase.Saved, savedUri = uri) }
                }
                .onFailure { t ->
                    Log.w(TAG, "Save failed", t)
                    _state.update {
                        it.copy(phase = ExportPhase.Error, errorRes = R.string.export_error_save_failed)
                    }
                }
        }
    }

    fun restore() {
        viewModelScope.launch { purchases.restorePurchases() }
    }

    private companion object {
        const val TAG = "ExportViewModel"
    }
}
