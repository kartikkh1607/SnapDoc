package com.kartik.snapdoc.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes (Navigation Compose 2.8+).
 *
 * Route objects are serialized via kotlinx.serialization, so call sites just
 * instantiate the typed object (no string templating, no URL encoding). Args
 * are read in ViewModels via [androidx.navigation.toRoute].
 *
 * Names are kept short under the [Routes] namespace to disambiguate from
 * similarly-named Compose types (e.g. `@Preview`).
 */
object Routes {
    @Serializable object Splash

    @Serializable object Onboarding

    @Serializable object Home

    @Serializable object History

    @Serializable object Documents

    @Serializable object Settings

    @Serializable data class DocDetail(val docId: String)

    @Serializable data class Camera(val docId: String)

    @Serializable data class Review(val docId: String, val imageUri: String)

    @Serializable data class Processing(val docId: String, val imageUri: String)

    @Serializable data class Preview(val docId: String, val imageUri: String)

    @Serializable data class Export(val docId: String, val imageUri: String)

    @Serializable data class PrintSheet(val docId: String, val imageUri: String)
}
