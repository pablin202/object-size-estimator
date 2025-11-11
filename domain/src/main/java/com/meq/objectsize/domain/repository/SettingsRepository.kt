package com.meq.objectsize.domain.repository

import com.meq.objectsize.domain.entity.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for app settings
 *
 * This is part of the domain layer and defines the contract
 * for accessing and modifying app settings.
 *
 * Implementation details (DataStore, etc.) are hidden in infrastructure layers.
 */
interface SettingsRepository {

    /**
     * Flow of current app settings
     * Emits new values whenever settings change
     */
    val settings: Flow<AppSettings>

    // Detection Settings
    suspend fun updateConfidenceThreshold(value: Float)
    suspend fun updateMaxObjects(value: Int)
    suspend fun updateSamePlaneThreshold(value: Float)

    // Camera Settings
    suspend fun updateTargetResolution(width: Int, height: Int)
    suspend fun updateFrameCaptureInterval(intervalMs: Long)

    // ML Settings
    suspend fun updateEnableGpuDelegate(enabled: Boolean)
    suspend fun updateNumThreads(threads: Int)

    // Performance Settings
    suspend fun updateShowPerformanceOverlay(show: Boolean)
    suspend fun updatePerformanceRefreshRate(rateMs: Long)

    // Reference Object Sizes
    suspend fun updateCellPhoneSize(width: Float, height: Float)
    suspend fun updateBookSize(width: Float, height: Float)
    suspend fun updateBottleSize(width: Float, height: Float)
    suspend fun updateCupSize(width: Float, height: Float)
    suspend fun updateKeyboardSize(width: Float, height: Float)

    // Reset
    suspend fun resetToDefaults()
}
