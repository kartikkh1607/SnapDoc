package com.kartik.snapdoc.data.monitoring

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Surface for reporting non-fatal errors and breadcrumbs.
 *
 * The default impl forwards to Firebase Crashlytics when it has been initialized
 * (i.e. when `google-services.json` is present in `app/` and the
 * `com.google.gms.google-services` plugin is applied). Without that, calls fall
 * through to logcat so the build still works and dev runs aren't silent.
 */
interface CrashReporter {
    fun log(message: String)
    fun setKey(key: String, value: String)
    fun recordException(throwable: Throwable, message: String? = null)
}

@Singleton
class FirebaseCrashReporter @Inject constructor() : CrashReporter {

    // Resolved lazily so a missing google-services.json (Firebase not init'd)
    // doesn't blow up on app startup — instead we silently fall back to logcat.
    private val crashlytics: FirebaseCrashlytics? by lazy {
        runCatching { FirebaseCrashlytics.getInstance() }.getOrNull()
    }

    override fun log(message: String) {
        crashlytics?.log(message) ?: Log.d(TAG, message)
    }

    override fun setKey(key: String, value: String) {
        crashlytics?.setCustomKey(key, value)
    }

    override fun recordException(throwable: Throwable, message: String?) {
        val cl = crashlytics
        if (cl != null) {
            if (message != null) cl.log(message)
            cl.recordException(throwable)
        } else {
            Log.w(TAG, message ?: throwable.message ?: "non-fatal", throwable)
        }
    }

    private companion object {
        const val TAG = "CrashReporter"
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CrashReporterModule {
    @Binds
    @Singleton
    abstract fun bindCrashReporter(impl: FirebaseCrashReporter): CrashReporter
}
