package com.kartik.snapdoc.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper over AppLovin MAX. The app is fully free; revenue comes from
 * banners on Home/History, an interstitial after each save, and an opt-in
 * rewarded ad that unlocks HD export for the current photo.
 *
 * SDK key is configured via [AdsConfig.SDK_KEY] / manifest meta-data; until
 * it's filled in, every call no-ops gracefully so the app stays usable.
 */
interface AdsRepository {
    val sdkReady: StateFlow<Boolean>
    fun initialize(context: Context)
    fun showInterstitial(activity: Activity)
    suspend fun showRewardedForHd(activity: Activity): Boolean
}

object AdsConfig {
    // TODO: replace with the SDK key from the AppLovin dashboard before shipping.
    const val SDK_KEY = "REPLACE_WITH_APPLOVIN_SDK_KEY"

    const val UNIT_BANNER = "REPLACE_WITH_BANNER_AD_UNIT_ID"
    const val UNIT_INTERSTITIAL = "REPLACE_WITH_INTERSTITIAL_AD_UNIT_ID"
    const val UNIT_REWARDED = "REPLACE_WITH_REWARDED_AD_UNIT_ID"

    val isConfigured: Boolean get() = !SDK_KEY.startsWith("REPLACE_")
}

@Module
@InstallIn(SingletonComponent::class)
object AdsRepositoryModule {
    @Provides
    @Singleton
    fun provideAdsRepository(): AdsRepository = DefaultAdsRepository()
}

@Singleton
class DefaultAdsRepository @Inject constructor() : AdsRepository {
    private val _sdkReady = MutableStateFlow(false)
    override val sdkReady: StateFlow<Boolean> = _sdkReady.asStateFlow()

    private var interstitial: MaxInterstitialAd? = null
    private var rewarded: MaxRewardedAd? = null

    override fun initialize(context: Context) {
        if (!AdsConfig.isConfigured) {
            Log.w(TAG, "AppLovin SDK key not configured; ads disabled.")
            return
        }
        val config = AppLovinSdkInitializationConfiguration.builder(AdsConfig.SDK_KEY, context)
            .setMediationProvider("max")
            .build()
        AppLovinSdk.getInstance(context).initialize(config) {
            _sdkReady.value = true
            preloadInterstitial(context)
            preloadRewarded(context)
        }
    }

    private fun preloadInterstitial(context: Context) {
        if (interstitial != null) return
        interstitial = MaxInterstitialAd(AdsConfig.UNIT_INTERSTITIAL, context).apply {
            setListener(object : MaxAdListener {
                override fun onAdLoaded(ad: MaxAd) {}
                override fun onAdDisplayed(ad: MaxAd) {}
                override fun onAdHidden(ad: MaxAd) { loadAd() }
                override fun onAdClicked(ad: MaxAd) {}
                override fun onAdLoadFailed(unit: String, error: MaxError) {}
                override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) { loadAd() }
            })
            loadAd()
        }
    }

    override fun showInterstitial(activity: Activity) {
        val ad = interstitial ?: return
        if (ad.isReady) ad.showAd(activity)
    }

    private fun preloadRewarded(context: Context) {
        if (rewarded != null) return
        rewarded = MaxRewardedAd.getInstance(AdsConfig.UNIT_REWARDED, context).apply { loadAd() }
    }

    override suspend fun showRewardedForHd(activity: Activity): Boolean {
        if (!AdsConfig.isConfigured) return false
        val ad = rewarded ?: MaxRewardedAd.getInstance(AdsConfig.UNIT_REWARDED, activity)
            .also { rewarded = it; it.loadAd() }
        if (!ad.isReady) return false
        val completion = CompletableDeferred<Boolean>()
        ad.setListener(object : MaxRewardedAdListener {
            override fun onAdLoaded(ad: MaxAd) {}
            override fun onAdDisplayed(ad: MaxAd) {}
            override fun onAdHidden(ad: MaxAd) {
                if (!completion.isCompleted) completion.complete(false)
                rewarded?.loadAd()
            }
            override fun onAdClicked(ad: MaxAd) {}
            override fun onAdLoadFailed(unit: String, error: MaxError) {}
            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                if (!completion.isCompleted) completion.complete(false)
            }
            override fun onUserRewarded(ad: MaxAd, reward: MaxReward) {
                if (!completion.isCompleted) completion.complete(true)
            }
        })
        ad.showAd(activity)
        return completion.await()
    }

    private companion object { const val TAG = "AdsRepository" }
}
