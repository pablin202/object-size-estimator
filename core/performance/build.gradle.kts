plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.meq.objectsize.core.performance"
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
    // Domain (implements repository interfaces)
    implementation(project(":domain"))

    // Core modules
    implementation(project(":core:common"))

    // AndroidX Core
    implementation(libs.androidx.core.ktx)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)

    // Android Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.truth)
}
