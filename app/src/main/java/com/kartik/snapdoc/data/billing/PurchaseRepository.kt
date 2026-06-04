package com.kartik.snapdoc.data.billing

import android.app.Activity
import com.kartik.snapdoc.data.prefs.UserPrefsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

interface PurchaseRepository {
    val entitlement: StateFlow<EntitlementState>
    val errors: SharedFlow<BillingError>
    suspend fun launchPurchase(activity: Activity, productId: String): Boolean
    suspend fun restorePurchases()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PurchaseRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPurchaseRepository(impl: DefaultPurchaseRepository): PurchaseRepository
}

@Singleton
class DefaultPurchaseRepository @Inject constructor(
    private val billing: BillingClientWrapper,
    private val prefs: UserPrefsRepository,
) : PurchaseRepository, Closeable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val started = AtomicBoolean(false)

    /**
     * Entitlement is authoritative from Play once billing is connected.
     * The DataStore cache only fills the offline-bootstrap window before the
     * first successful Play query — once we have a live answer, refunds and
     * revocations propagate immediately.
     *
     * Billing connects lazily on first subscriber so cold launches that never
     * navigate to a paid surface (settings, export) don't pay the Play Services
     * round-trip during startup.
     */
    override val entitlement: StateFlow<EntitlementState> = combine(
        billing.entitlement,
        billing.connected,
        prefs.entitlement,
    ) { live, connected, cached ->
        if (connected) live else cached
    }
        .onStart { ensureStarted() }
        .stateIn(scope, SharingStarted.Lazily, EntitlementState.Locked)

    override val errors: SharedFlow<BillingError> = billing.errors

    private fun ensureStarted() {
        if (!started.compareAndSet(false, true)) return
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

    override suspend fun launchPurchase(activity: Activity, productId: String): Boolean {
        ensureStarted()
        return billing.launchPurchase(activity, productId, obfuscatedAccountId = prefs.installId())
    }

    override suspend fun restorePurchases() {
        ensureStarted()
        billing.queryPurchases()
    }

    override fun close() {
        scope.cancel()
        billing.close()
    }
}
