plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.meq.objectsize.core.ml"
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
    // Domain (implements ObjectDetector interface)
    implementation(project(":domain"))

    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:performance"))

    // AndroidX Core
    implementation(libs.androidx.core.ktx)

    // TensorFlow Lite
    implementation(libs.bundles.tensorflow)

    // Coroutines (for Flow)
    implementation(libs.bundles.coroutines)

    // Hilt (for injection)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // LeakCanary (for memory leak detection)
    debugImplementation(libs.leakcanary.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)

    // Android Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
