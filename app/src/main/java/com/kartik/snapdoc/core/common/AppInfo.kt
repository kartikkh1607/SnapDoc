package com.kartik.snapdoc.core.common

/**
 * App-level metadata that feature modules need but can't read directly
 * (BuildConfig fields live in :app and aren't transitive). The :app
 * module provides an implementation; feature modules consume the interface.
 */
interface AppInfo {
    val versionName: String
    val versionCode: Int
}
