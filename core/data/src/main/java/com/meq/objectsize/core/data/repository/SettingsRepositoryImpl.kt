package com.meq.objectsize.core.data.repository

import com.meq.objectsize.core.data.mapper.SettingsMapper
import com.meq.objectsize.core.datastore.SettingsDataStore
import com.meq.objectsize.domain.entity.AppSettings
import com.meq.objectsize.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository
 *
 * This class is part of the infrastructure/data layer.
 * It uses SettingsDataStore (infrastructure) and maps to AppSettings (domain).
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val mapper: SettingsMapper
) : SettingsRepository {

    override val settings: Flow<AppSettings> =
        settingsDataStore.preferences.map { preferences ->
            mapper.mapToAppSettings(preferences)
        }

    // Detection Settings
    override suspend fun updateConfidenceThreshold(value: Float) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.CONFIDENCE_THRESHOLD, value)
    }

    override suspend fun updateMaxObjects(value: Int) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.MAX_OBJECTS, value)
    }

    override suspend fun updateSamePlaneThreshold(value: Float) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.SAME_PLANE_THRESHOLD, value)
    }

    // Camera Settings
    override suspend fun updateTargetResolution(width: Int, height: Int) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.TARGET_RESOLUTION_WIDTH, width)
        settingsDataStore.updatePreference(SettingsDataStore.Keys.TARGET_RESOLUTION_HEIGHT, height)
    }

    override suspend fun updateFrameCaptureInterval(intervalMs: Long) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.FRAME_CAPTURE_INTERVAL_MS, intervalMs)
    }

    // ML Settings
    override suspend fun updateEnableGpuDelegate(enabled: Boolean) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.ENABLE_GPU_DELEGATE, enabled)
    }

    override suspend fun updateNumThreads(threads: Int) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.NUM_THREADS, threads)
    }

    // Performance Settings
    override suspend fun updateShowPerformanceOverlay(show: Boolean) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.SHOW_PERFORMANCE_OVERLAY, show)
    }

    override suspend fun updatePerformanceRefreshRate(rateMs: Long) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.PERFORMANCE_REFRESH_RATE, rateMs)
    }

    // Reference Object Sizes
    override suspend fun updateCellPhoneSize(width: Float, height: Float) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.CELL_PHONE_WIDTH, width)
        settingsDataStore.updatePreference(SettingsDataStore.Keys.CELL_PHONE_HEIGHT, height)
    }

    override suspend fun updateBookSize(width: Float, height: Float) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.BOOK_WIDTH, width)
        settingsDataStore.updatePreference(SettingsDataStore.Keys.BOOK_HEIGHT, height)
    }

    override suspend fun updateBottleSize(width: Float, height: Float) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.BOTTLE_WIDTH, width)
        settingsDataStore.updatePreference(SettingsDataStore.Keys.BOTTLE_HEIGHT, height)
    }

    override suspend fun updateCupSize(width: Float, height: Float) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.CUP_WIDTH, width)
        settingsDataStore.updatePreference(SettingsDataStore.Keys.CUP_HEIGHT, height)
    }

    override suspend fun updateKeyboardSize(width: Float, height: Float) {
        settingsDataStore.updatePreference(SettingsDataStore.Keys.KEYBOARD_WIDTH, width)
        settingsDataStore.updatePreference(SettingsDataStore.Keys.KEYBOARD_HEIGHT, height)
    }

    // Reset
    override suspend fun resetToDefaults() {
        settingsDataStore.clearAll()
    }
}
