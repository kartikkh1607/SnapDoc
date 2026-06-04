import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

// Mirrors :app's local.properties handling: the signature-check public key
// stays out of source control. When missing, PurchaseVerifier defaults to
// permissive mode and logs a warning at runtime.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val billingLicenseKey: String = localProps.getProperty("billing.licenseKey", "")

android {
    namespace = "com.kartik.snapdoc.data.billing"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        buildConfigField("String", "BILLING_LICENSE_KEY", "\"$billingLicenseKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            consumerProguardFiles("consumer-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(project(":core:common"))
    implementation(project(":data:prefs"))
    implementation(libs.billing.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
