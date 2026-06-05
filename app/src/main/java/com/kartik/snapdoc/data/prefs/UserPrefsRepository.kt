package com.kartik.snapdoc.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface UserPrefsRepository {
    val onboardingSeen: Flow<Boolean>
    val profile: Flow<UserProfile>
    val saveToGallery: Flow<Boolean>
    suspend fun setOnboardingSeen(seen: Boolean)
    suspend fun setDisplayName(name: String)
    suspend fun setEmail(email: String)
    suspend fun setSaveToGallery(value: Boolean)
}

data class UserProfile(
    val displayName: String,
    val email: String,
) {
    val initials: String
        get() = displayName
            .split(' ')
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }
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

    override val profile: Flow<UserProfile> = store.data.map {
        UserProfile(
            displayName = it[Keys.DISPLAY_NAME] ?: "Riya Sharma",
            email = it[Keys.EMAIL] ?: "riya.sharma@gmail.com",
        )
    }

    override val saveToGallery: Flow<Boolean> =
        store.data.map { it[Keys.SAVE_TO_GALLERY] ?: true }

    override suspend fun setDisplayName(name: String) {
        store.edit { it[Keys.DISPLAY_NAME] = name }
    }

    override suspend fun setEmail(email: String) {
        store.edit { it[Keys.EMAIL] = email }
    }

    override suspend fun setSaveToGallery(value: Boolean) {
        store.edit { it[Keys.SAVE_TO_GALLERY] = value }
    }

    override suspend fun setOnboardingSeen(seen: Boolean) {
        store.edit { it[Keys.ONBOARDING_SEEN] = seen }
    }

    private object Keys {
        val ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EMAIL = stringPreferencesKey("email")
        val SAVE_TO_GALLERY = booleanPreferencesKey("save_to_gallery")
    }
}
