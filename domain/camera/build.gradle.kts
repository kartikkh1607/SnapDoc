plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.kartik.snapdoc.domain.camera"
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
    api(project(":data:specs"))
    implementation(libs.androidx.camera.core)
    implementation(libs.mlkit.face.detection)
}
