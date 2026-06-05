package com.kartik.snapdoc.ui.ads

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.applovin.mediation.ads.MaxAdView
import com.kartik.snapdoc.data.ads.AdsConfig

/**
 * AppLovin MAX banner. Renders nothing if the SDK key hasn't been configured
 * yet so previews and fresh clones don't blow up.
 */
@Composable
fun MaxBannerAd(modifier: Modifier = Modifier) {
    if (!AdsConfig.isConfigured) return
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { ctx ->
            MaxAdView(AdsConfig.UNIT_BANNER, ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                loadAd()
            }
        },
    )
}
