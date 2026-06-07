package com.kartik.snapdoc.data.ads

import android.app.Activity
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The app is fully free — no ads, no paywall, no rewarded videos.
 *
 * This interface is kept as a no-op stub so the DI graph and any historical
 * call sites keep compiling, but every method does nothing. The AppLovin
 * dependency has been removed from the build. If you ever want to bring ads
 * back, restore the AppLovin artifact in libs.versions.toml and reimplement
 * these methods.
 */
interface AdsRepository {
    val sdkReady: StateFlow<Boolean>
    fun initialize(context: Context)
    fun showInterstitial(activity: Activity)
    suspend fun showRewardedForHd(activity: Activity): Boolean
}

@Module
@InstallIn(SingletonComponent::class)
object AdsRepositoryModule {
    @Provides
    @Singleton
    fun provideAdsRepository(): AdsRepository = NoOpAdsRepository()
}

@Singleton
class NoOpAdsRepository @Inject constructor() : AdsRepository {
    private val _sdkReady = MutableStateFlow(false)
    override val sdkReady: StateFlow<Boolean> = _sdkReady.asStateFlow()
    override fun initialize(context: Context) = Unit
    override fun showInterstitial(activity: Activity) = Unit
    override suspend fun showRewardedForHd(activity: Activity): Boolean = true
}
