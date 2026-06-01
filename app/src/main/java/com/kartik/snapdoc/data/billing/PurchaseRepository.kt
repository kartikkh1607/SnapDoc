package com.kartik.snapdoc.data.billing

import android.app.Activity
import com.kartik.snapdoc.data.prefs.UserPrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

    val entitlement: StateFlow<EntitlementState> = combine(
        billing.entitlement,
        prefs.entitlement,
    ) { live, cached ->
        EntitlementState(
            photoExportUnlocked = live.photoExportUnlocked || cached.photoExportUnlocked,
            studioBundleUnlocked = live.studioBundleUnlocked || cached.studioBundleUnlocked,
        )
    }.stateIn(scope, SharingStarted.Eagerly, EntitlementState.Locked)

    init {
        billing.start()
        scope.launch {
            billing.entitlement.collect { live ->
                if (live.photoExportUnlocked) prefs.setPhotoExportUnlocked(true)
                if (live.studioBundleUnlocked) prefs.setStudioBundleUnlocked(true)
            }
        }
    }

    fun launchPurchase(activity: Activity, productId: String): Boolean =
        billing.launchPurchase(activity, productId)

    suspend fun restorePurchases() {
        billing.queryPurchases()
    }
}
