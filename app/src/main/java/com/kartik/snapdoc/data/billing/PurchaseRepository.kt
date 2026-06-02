package com.kartik.snapdoc.data.billing

import android.app.Activity
import com.kartik.snapdoc.data.prefs.UserPrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(
    private val billing: BillingClientWrapper,
    private val prefs: UserPrefsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Entitlement is authoritative from Play once billing is connected.
     * The DataStore cache only fills the offline-bootstrap window before the
     * first successful Play query — once we have a live answer, refunds and
     * revocations propagate immediately.
     */
    val entitlement: StateFlow<EntitlementState> = combine(
        billing.entitlement,
        billing.connected,
        prefs.entitlement,
    ) { live, connected, cached ->
        if (connected) live else cached
    }.stateIn(scope, SharingStarted.Eagerly, EntitlementState.Locked)

    init {
        billing.start()
        // Mirror authoritative live state into the cache so offline launches are
        // accurate. If Play revokes, we write `false` back into the cache too.
        scope.launch {
            combine(billing.entitlement, billing.connected) { live, connected -> live to connected }
                .collect { (live, connected) ->
                    if (!connected) return@collect
                    prefs.setPhotoExportUnlocked(live.photoExportUnlocked)
                    prefs.setStudioBundleUnlocked(live.studioBundleUnlocked)
                }
        }
    }

    val errors: SharedFlow<BillingError> = billing.errors

    fun launchPurchase(activity: Activity, productId: String): Boolean =
        billing.launchPurchase(activity, productId)

    suspend fun restorePurchases() {
        billing.queryPurchases()
    }
}
