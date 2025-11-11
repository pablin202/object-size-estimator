plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.meq.objectsize"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.meq.objectsize"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
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
    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = false
        }
    }

    lint {
        // Baseline file for existing issues
        baseline = file("lint-baseline.xml")

        // Fail build on errors
        abortOnError = true

        // Check for fatal issues during release builds
        checkReleaseBuilds = true

        // Enable all checks
        checkAllWarnings = true

        // Disable specific checks if needed
        disable += setOf(
            "OldTargetApi",
            "GradleDependency"
        )

        // Treat specific warnings as errors
        error += setOf(
            "StopShip",
            "NewApi",
            "InlinedApi"
        )

        // Generate reports
        htmlReport = true
        xmlReport = true
        textReport = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

hilt {
    enableAggregatingTask = false
}

dependencies {
    // Feature modules
    implementation(project(":feature:camera"))
    implementation(project(":feature:settings"))

    // Domain (needed for DI providers)
    implementation(project(":domain"))

    // Core modules (needed for DI providers)
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))
    implementation(project(":core:ml"))
    implementation(project(":core:camera"))
    implementation(project(":core:performance"))

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // LeakCanary - Memory leak detection (DEBUG only)
    debugImplementation(libs.leakcanary.android)
    // Plumber - Detects Android framework leaks (Activity, Fragment, ViewModel, etc.)
    debugImplementation(libs.plumber.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)

    // Android Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.truth)

    // Compose Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Coroutines testing
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
}