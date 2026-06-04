plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.kartik.snapdoc.domain.watermark"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    buildTypes { release { isMinifyEnabled = false; consumerProguardFiles("consumer-rules.pro") } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) } }
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
