package com.meq.objectsize.utils

import android.util.Log
import leakcanary.AppWatcher

/**
 * Custom LeakCanary watchers for critical components
 *
 * These watchers help detect leaks in our Flow-based architecture:
 * - CameraManager (Singleton, should never leak)
 * - ImageAnalyzer (Should be cleaned on CameraManager.stopCamera)
 * - TFLiteObjectDetector (Singleton, should never leak)
 *
 * Usage:
 * - Call watch() when object should be GC'd
 * - LeakCanary will verify it's collected
 * - Triggers heap dump if retained
 *
 * Note: LeakCanary is only included in DEBUG builds (debugImplementation)
 * In RELEASE builds, these functions are no-op (safe to call)
 */
object LeakCanaryWatchers {

    private const val TAG = "LeakCanaryWatchers"

    /**
     * Watch ImageAnalyzer for leaks
     *
     * Call this when CameraManager.stopCamera() is called
     * ImageAnalyzer should be GC'd after camera stops
     */
    fun watchImageAnalyzer(analyzer: Any, reason: String = "Camera stopped") {
        try {
            Log.d(TAG, "Watching ImageAnalyzer for leaks: $reason")
            AppWatcher.objectWatcher.expectWeaklyReachable(
                watchedObject = analyzer,
                description = "ImageAnalyzer should be GC'd after $reason"
            )
        } catch (e: Throwable) {
            // LeakCanary not available - safe to ignore
        }
    }

    /**
     * Watch CoroutineScope for cancellation
     *
     * Verifies that scopes are properly cancelled and collected
     */
    fun watchCoroutineScope(scope: Any, name: String) {
        try {
            Log.d(TAG, "Watching CoroutineScope '$name' for leaks")
            AppWatcher.objectWatcher.expectWeaklyReachable(
                watchedObject = scope,
                description = "CoroutineScope '$name' should be cancelled and GC'd"
            )
        } catch (e: Throwable) {
            // LeakCanary not available - safe to ignore
        }
    }

    /**
     * Watch any custom object for leaks
     *
     * Generic watcher for testing custom components
     */
    fun watchObject(obj: Any, description: String) {
        try {
            Log.d(TAG, "Watching object: $description")
            AppWatcher.objectWatcher.expectWeaklyReachable(
                watchedObject = obj,
                description = description
            )
        } catch (e: Throwable) {
            // LeakCanary not available - safe to ignore
        }
    }
}
