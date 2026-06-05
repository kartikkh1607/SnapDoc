package com.kartik.snapdoc

import android.app.Application
import com.kartik.snapdoc.data.ads.AdsRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SnapDocApplication : Application() {
    @Inject lateinit var ads: AdsRepository

    override fun onCreate() {
        super.onCreate()
        ads.initialize(this)
    }
}
