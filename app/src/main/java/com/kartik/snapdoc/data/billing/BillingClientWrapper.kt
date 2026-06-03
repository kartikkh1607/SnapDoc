package com.kartik.snapdoc.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.Closeable
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingClientWrapper @Inject constructor(
    @ApplicationContext private val context: Context,
) : Closeable {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _entitlement = MutableStateFlow(EntitlementState.Locked)
    val entitlement: StateFlow<EntitlementState> = _entitlement.asStateFlow()

    private val _products = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val products: StateFlow<Map<String, ProductDetails>> = _products.asStateFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    // Fire-and-forget error events for the UI (snackbar / dialog).
    private val _errors = MutableSharedFlow<BillingError>(
        replay = 0,
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val errors: SharedFlow<BillingError> = _errors.asSharedFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases?.forEach { handlePurchase(it) }
            BillingClient.BillingResponseCode.USER_CANCELED -> Log.d(TAG, "User cancelled")
            else -> {
                Log.w(TAG, "Purchase update failed: ${result.debugMessage}")
                _errors.tryEmit(BillingError.PurchaseFailed(result.responseCode, result.debugMessage))
            }
        }
    }

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .build()

    private val connectAttempts = AtomicInteger(0)

    fun start() {
        if (client.isReady) return
        connect()
    }

    private fun connect() {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    connectAttempts.set(0)
                    _connected.value = true
                    scope.launch {
                        queryProducts()
                        queryPurchases()
                    }
                } else {
                    Log.w(TAG, "Billing setup failed: ${result.debugMessage}")
                    if (connectAttempts.get() >= 4) {
                        _errors.tryEmit(BillingError.ConnectionFailed(result.debugMessage))
                    }
                    retry()
                }
            }

            override fun onBillingServiceDisconnected() {
                _connected.value = false
                retry()
            }
        })
    }

    private fun retry() {
        val attempt = connectAttempts.incrementAndGet()
        if (attempt > 5) {
            connectAttempts.set(5)
            return
        }
        val backoffMs = (1000L * (1 shl attempt)).coerceAtMost(32_000L)
        scope.launch {
            delay(backoffMs)
            connect()
        }
    }

    private suspend fun queryProducts() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                ProductIds.all.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                },
            )
            .build()
        val result = client.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _products.value = result.productDetailsList.orEmpty().associateBy { it.productId }
        } else {
            Log.w(TAG, "Product query failed: ${result.billingResult.debugMessage}")
        }
    }

    suspend fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val result = client.queryPurchasesAsync(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // Authoritative snapshot from Play — replace, don't merge, so refunds
            // revoke entitlements instead of being masked by stale cached state.
            replaceEntitlement(result.purchasesList)
        }
    }

    fun launchPurchase(activity: Activity, productId: String): Boolean {
        val product = _products.value[productId] ?: run {
            Log.w(TAG, "Product not loaded yet: $productId")
            return false
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .build(),
                ),
            )
            .build()
        val result = client.launchBillingFlow(activity, params)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        mergePurchases(listOf(purchase))
        if (!purchase.isAcknowledged) {
            scope.launch {
                val ack = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                val result = client.acknowledgePurchase(ack)
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "Ack failed: ${result.debugMessage}")
                }
            }
        }
    }

    /**
     * Merge a single new purchase into entitlement state (additive — for the
     * purchase-update listener where we only see the newly purchased item).
     */
    private fun mergePurchases(purchases: List<Purchase>) {
        var photo = _entitlement.value.photoExportUnlocked
        var studio = _entitlement.value.studioBundleUnlocked
        for (p in purchases) {
            if (p.purchaseState != Purchase.PurchaseState.PURCHASED) continue
            for (id in p.products) {
                when (id) {
                    ProductIds.PHOTO_EXPORT -> photo = true
                    ProductIds.STUDIO_BUNDLE -> { studio = true; photo = true }
                }
            }
        }
        _entitlement.update { EntitlementState(photo, studio) }
    }

    /**
     * Replace entitlement state entirely from an authoritative purchase list
     * (Play's [queryPurchasesAsync] result). A refunded purchase will be absent
     * here, so this revokes access.
     */
    private fun replaceEntitlement(purchases: List<Purchase>) {
        var photo = false
        var studio = false
        for (p in purchases) {
            if (p.purchaseState != Purchase.PurchaseState.PURCHASED) continue
            for (id in p.products) {
                when (id) {
                    ProductIds.PHOTO_EXPORT -> photo = true
                    ProductIds.STUDIO_BUNDLE -> { studio = true; photo = true }
                }
            }
        }
        _entitlement.value = EntitlementState(photo, studio)
    }

    override fun close() {
        scope.cancel()
        if (client.isReady) client.endConnection()
    }

    private companion object {
        const val TAG = "BillingClient"
    }
}
