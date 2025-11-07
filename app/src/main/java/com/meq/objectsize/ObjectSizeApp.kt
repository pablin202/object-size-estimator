package com.meq.objectsize

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import leakcanary.LeakCanary
import shark.ReferenceMatcher

/**
 * Application class for Hilt initialization and LeakCanary configuration
 *
 * LeakCanary Setup:
 * - Automatically detects memory leaks in DEBUG builds
 * - Plumber automatically detects Android framework leaks (installed via dependency)
 * - Custom watchers for critical components (CameraManager, Detector, etc.)
 * - Configured to watch our Flow-based architecture
 *
 * Dependencies:
 * - leakcanary-android: Main leak detection engine
 * - plumber-android: Auto-detects Activity/Fragment/ViewModel/Service leaks
 *   (installs automatically, no manual configuration needed)
 *
 * Leak Detection Coverage:
 * ┌─────────────────────────────────────────────────┐
 * │ Android Framework (Plumber - automatic)         │
 * │  ├─ Activities                                  │
 * │  ├─ Fragments                                   │
 * │  ├─ ViewModels                                  │
 * │  ├─ Services                                    │
 * │  └─ BroadcastReceivers                          │
 * │                                                 │
 * │ Custom Architecture (LeakCanaryWatchers)       │
 * │  ├─ CoroutineScopes                            │
 * │  ├─ ImageAnalyzer                              │
 * │  └─ TFLiteObjectDetector                       │
 * └─────────────────────────────────────────────────┘
 */
@HiltAndroidApp
class ObjectSizeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        configureLeakCanary()
        // Plumber installs automatically - no manual setup needed
    }

    /**
     * Configure LeakCanary for optimal detection
     *
     * Custom Configuration:
     * - Watch CoroutineScopes in our architecture
     * - Ignore false positives from TensorFlow Lite
     * - Enhanced reporting for Flow-based components
     */
    private fun configureLeakCanary() {
        // LeakCanary automatically works in DEBUG builds
        // Configuration is optional - defaults are good for most cases

        LeakCanary.config = LeakCanary.config.copy(
            // Watch destroyed activities/fragments after 5s (default)
            retainedVisibleThreshold = 5,

            // Dump heap for every leak (helpful during development)
            dumpHeap = true,

            // Custom reference matchers if needed
            referenceMatchers = LeakCanary.config.referenceMatchers + customReferenceMatchers()
        )
    }

    /**
     * Custom reference matchers to ignore false positives
     *
     * Add patterns here if you see false positives from:
     * - TensorFlow Lite internal caches
     * - CameraX internal references
     * - Hilt internal references
     *
     * Example:
     * ignoredInstanceField("org.tensorflow.lite.Interpreter", "nativeHandle")
     */
    private fun customReferenceMatchers(): List<ReferenceMatcher> {
        return emptyList()
        // Add custom matchers if needed:
        // return listOf(
        //     ignoredInstanceField("ClassName", "fieldName")
        // )
    }
}