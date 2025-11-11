package com.meq.objectsize.domain.entity

/**
 * Application settings entity
 *
 * Contains all configurable parameters for the app, replacing hardcoded values.
 * These settings are persisted and can be modified by the user.
 */
data class AppSettings(
    // Detection Settings
    val confidenceThreshold: Float = 0.5f,
    val maxObjects: Int = 10,
    val samePlaneThreshold: Float = 0.20f,

    // Camera Settings
    val targetResolutionWidth: Int = 1280,
    val targetResolutionHeight: Int = 720,
    val frameCaptureIntervalMs: Long = 100L, // milliseconds between frame captures (~10 FPS)

    // ML Settings
    val enableGpuDelegate: Boolean = true,
    val numThreads: Int = 4,

    // Performance Settings
    val showPerformanceOverlay: Boolean = true,
    val performanceRefreshRate: Long = 1000L, // milliseconds

    // Reference Object Sizes (in cm) - Customizable
    val cellPhoneWidth: Float = 7f,
    val cellPhoneHeight: Float = 15f,
    val bookWidth: Float = 15f,
    val bookHeight: Float = 23f,
    val bottleWidth: Float = 7f,
    val bottleHeight: Float = 25f,
    val cupWidth: Float = 8f,
    val cupHeight: Float = 10f,
    val keyboardWidth: Float = 44f,
    val keyboardHeight: Float = 13f
) {
    /**
     * Get reference object sizes as a map
     */
    fun getReferenceObjectSizes(): Map<String, Pair<Float, Float>> = mapOf(
        "cell phone" to Pair(cellPhoneWidth, cellPhoneHeight),
        "book" to Pair(bookWidth, bookHeight),
        "bottle" to Pair(bottleWidth, bottleHeight),
        "cup" to Pair(cupWidth, cupHeight),
        "keyboard" to Pair(keyboardWidth, keyboardHeight)
    )

    /**
     * Validate settings values are within acceptable ranges
     */
    fun isValid(): Boolean {
        return confidenceThreshold in 0f..1f &&
                maxObjects in 1..50 &&
                samePlaneThreshold in 0f..1f &&
                targetResolutionWidth > 0 &&
                targetResolutionHeight > 0 &&
                frameCaptureIntervalMs > 0 &&
                numThreads in 1..8 &&
                performanceRefreshRate > 0 &&
                cellPhoneWidth > 0 && cellPhoneHeight > 0 &&
                bookWidth > 0 && bookHeight > 0 &&
                bottleWidth > 0 && bottleHeight > 0 &&
                cupWidth > 0 && cupHeight > 0 &&
                keyboardWidth > 0 && keyboardHeight > 0
    }

    companion object {
        /**
         * Default settings with sensible values
         */
        val DEFAULT = AppSettings()
    }
}
