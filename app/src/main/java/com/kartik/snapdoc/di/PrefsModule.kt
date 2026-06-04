package com.kartik.snapdoc.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Top-level delegate guarantees a single DataStore instance per file even if
// Hilt's @Singleton scoping is ever bypassed — `PreferenceDataStoreFactory.create`
// would throw IllegalStateException on a second instance for the same file.
private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_prefs",
)

@Module
@InstallIn(SingletonComponent::class)
object PrefsModule {

    @Provides
    @Singleton
    fun provideUserPrefsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.userPrefsDataStore
}
