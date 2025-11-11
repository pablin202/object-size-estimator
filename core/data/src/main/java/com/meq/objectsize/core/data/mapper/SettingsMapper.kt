package com.meq.objectsize.core.data.mapper

import androidx.datastore.preferences.core.Preferences
import com.meq.objectsize.core.datastore.SettingsDataStore
import com.meq.objectsize.domain.entity.AppSettings
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper between DataStore Preferences and Domain AppSettings entity
 *
 * This is part of the data layer and handles the transformation
 * from infrastructure (DataStore) to domain (AppSettings).
 */
@Singleton
class SettingsMapper @Inject constructor() {

    /**
     * Map DataStore Preferences to AppSettings entity
     *
     * Uses default values from AppSettings.DEFAULT if preference is not set
     */
    fun mapToAppSettings(preferences: Preferences): AppSettings {
        val defaults = AppSettings.DEFAULT

        return AppSettings(
            // Detection Settings
            confidenceThreshold = preferences[SettingsDataStore.Keys.CONFIDENCE_THRESHOLD]
                ?: defaults.confidenceThreshold,
            maxObjects = preferences[SettingsDataStore.Keys.MAX_OBJECTS]
                ?: defaults.maxObjects,
            samePlaneThreshold = preferences[SettingsDataStore.Keys.SAME_PLANE_THRESHOLD]
                ?: defaults.samePlaneThreshold,

            // Camera Settings
            targetResolutionWidth = preferences[SettingsDataStore.Keys.TARGET_RESOLUTION_WIDTH]
                ?: defaults.targetResolutionWidth,
            targetResolutionHeight = preferences[SettingsDataStore.Keys.TARGET_RESOLUTION_HEIGHT]
                ?: defaults.targetResolutionHeight,
            frameCaptureIntervalMs = preferences[SettingsDataStore.Keys.FRAME_CAPTURE_INTERVAL_MS]
                ?: defaults.frameCaptureIntervalMs,

            // ML Settings
            enableGpuDelegate = preferences[SettingsDataStore.Keys.ENABLE_GPU_DELEGATE]
                ?: defaults.enableGpuDelegate,
            numThreads = preferences[SettingsDataStore.Keys.NUM_THREADS]
                ?: defaults.numThreads,

            // Performance Settings
            showPerformanceOverlay = preferences[SettingsDataStore.Keys.SHOW_PERFORMANCE_OVERLAY]
                ?: defaults.showPerformanceOverlay,
            performanceRefreshRate = preferences[SettingsDataStore.Keys.PERFORMANCE_REFRESH_RATE]
                ?: defaults.performanceRefreshRate,

            // Reference Object Sizes
            cellPhoneWidth = preferences[SettingsDataStore.Keys.CELL_PHONE_WIDTH]
                ?: defaults.cellPhoneWidth,
            cellPhoneHeight = preferences[SettingsDataStore.Keys.CELL_PHONE_HEIGHT]
                ?: defaults.cellPhoneHeight,
            bookWidth = preferences[SettingsDataStore.Keys.BOOK_WIDTH]
                ?: defaults.bookWidth,
            bookHeight = preferences[SettingsDataStore.Keys.BOOK_HEIGHT]
                ?: defaults.bookHeight,
            bottleWidth = preferences[SettingsDataStore.Keys.BOTTLE_WIDTH]
                ?: defaults.bottleWidth,
            bottleHeight = preferences[SettingsDataStore.Keys.BOTTLE_HEIGHT]
                ?: defaults.bottleHeight,
            cupWidth = preferences[SettingsDataStore.Keys.CUP_WIDTH]
                ?: defaults.cupWidth,
            cupHeight = preferences[SettingsDataStore.Keys.CUP_HEIGHT]
                ?: defaults.cupHeight,
            keyboardWidth = preferences[SettingsDataStore.Keys.KEYBOARD_WIDTH]
                ?: defaults.keyboardWidth,
            keyboardHeight = preferences[SettingsDataStore.Keys.KEYBOARD_HEIGHT]
                ?: defaults.keyboardHeight
        )
    }
}
