plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.meq.objectsize.core.common"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)

    // CameraX (for ImageProxy in ImageUtils)
    implementation(libs.bundles.camerax)

    // LeakCanary (for watcher utilities)
    debugImplementation(libs.leakcanary.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
}
