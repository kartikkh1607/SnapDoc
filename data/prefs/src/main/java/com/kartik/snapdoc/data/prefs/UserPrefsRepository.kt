package com.kartik.snapdoc.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kartik.snapdoc.data.billing.EntitlementState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface UserPrefsRepository {
    val onboardingSeen: Flow<Boolean>
    val entitlement: Flow<EntitlementState>
    suspend fun setOnboardingSeen(seen: Boolean)
    suspend fun setPhotoExportUnlocked(value: Boolean)
    suspend fun setStudioBundleUnlocked(value: Boolean)
    suspend fun installId(): String
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserPrefsRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUserPrefsRepository(impl: DefaultUserPrefsRepository): UserPrefsRepository
}

@Singleton
class DefaultUserPrefsRepository @Inject constructor(
    private val store: DataStore<Preferences>,
) : UserPrefsRepository {
    override val onboardingSeen: Flow<Boolean> =
        store.data.map { it[Keys.ONBOARDING_SEEN] ?: false }

    override val entitlement: Flow<EntitlementState> = store.data.map {
        EntitlementState(
            photoExportUnlocked = it[Keys.PHOTO_EXPORT] ?: false,
            studioBundleUnlocked = it[Keys.STUDIO_BUNDLE] ?: false,
        )
    }

    override suspend fun setOnboardingSeen(seen: Boolean) {
        store.edit { it[Keys.ONBOARDING_SEEN] = seen }
    }

    override suspend fun setPhotoExportUnlocked(value: Boolean) {
        store.edit { it[Keys.PHOTO_EXPORT] = value }
    }

    override suspend fun setStudioBundleUnlocked(value: Boolean) {
        store.edit {
            it[Keys.STUDIO_BUNDLE] = value
            if (value) it[Keys.PHOTO_EXPORT] = true
        }
    }

    /**
     * Returns a stable per-install opaque ID, generating one on first call.
     * Passed as `obfuscatedAccountId` on BillingFlowParams so Google can
     * correlate purchase + device server-side and so we can later tie purchases
     * to a specific install when investigating abuse.
     */
    override suspend fun installId(): String {
        val current = store.data.first()[Keys.INSTALL_ID]
        if (current != null) return current
        val fresh = UUID.randomUUID().toString()
        store.edit { it[Keys.INSTALL_ID] = fresh }
        return fresh
    }

    private object Keys {
        val ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
        val PHOTO_EXPORT = booleanPreferencesKey("photo_export_unlocked")
        val STUDIO_BUNDLE = booleanPreferencesKey("studio_bundle_unlocked")
        val INSTALL_ID = stringPreferencesKey("install_id")
    }
}
