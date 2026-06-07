import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    // AGP 9.0+ has built-in Kotlin support; the kotlin-android plugin is no
    // longer required (and is rejected by the AGP plugin if present).
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

// Firebase Crashlytics is opt-in: drop google-services.json into app/ to enable.
// Without that file the google-services plugin would fail the build, so we apply
// it (and the Crashlytics plugin) only when the config is present. The runtime
// CrashReporter implementation falls back to a no-op when Firebase isn't init'd.
val hasGoogleServices = file("google-services.json").exists()
if (hasGoogleServices) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
}

// Release signing is configured via an untracked keystore.properties file at
// the project root. See keystore.properties.example for the schema. When the
// file is absent (CI without secrets, fresh clones, or debug-only builds)
// the release build falls back to the debug signing config so the build
// still succeeds — but the resulting APK/AAB is NOT shippable.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val hasReleaseKeystore = keystoreProps.getProperty("storeFile")?.let {
    rootProject.file(it).exists()
} == true

android {
    namespace = "com.kartik.snapdoc"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kartik.snapdoc"
        minSdk = 24
        targetSdk = 36
        // Versioning policy: versionName follows semver (MAJOR.MINOR.PATCH);
        // versionCode is a monotonic integer that MUST be bumped on every
        // Play Store upload, even if the version name does not change.
        // First public release will be 1.0.0 / versionCode 1.
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (hasReleaseKeystore) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // AGP 9.0+ exposes Kotlin DSL under android { kotlin { ... } } instead of
    // the deprecated kotlinOptions block.
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    lint {
        // Fail release builds on lint errors so a regression can never ship.
        abortOnError = true
        checkReleaseBuilds = true
        // Don't scan downstream module/library code we don't own.
        checkDependencies = false
        // Hindi (values-hi) currently has minor gaps that are tracked separately;
        // also silence Material Icons internal lint noise that doesn't surface
        // user-visible issues.
        disable += setOf("MissingTranslation", "ExtraTranslation")
        // Treat unhandled exceptions and a11y issues as errors instead of warnings.
        warning += setOf("Unused")
        error += setOf(
            "ContentDescription",
            "ClickableViewAccessibility",
            "VisibleForTests",
        )
        // Baseline so existing findings are tracked separately from new ones.
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Core / lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ML Kit
    implementation(libs.mlkit.face.detection)
    implementation(libs.mlkit.segmentation.selfie)

    // Coil
    implementation(libs.coil.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Serialization + Coroutines
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Splash
    implementation(libs.androidx.core.splashscreen)

    // Work
    implementation(libs.androidx.work.runtime.ktx)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
