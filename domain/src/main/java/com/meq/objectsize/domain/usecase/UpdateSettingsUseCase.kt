package com.meq.objectsize.domain.usecase

import com.meq.objectsize.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case to update app settings
 *
 * Provides methods for all setting update operations
 */
class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    // Detection Settings
    suspend fun updateConfidenceThreshold(value: Float) =
        settingsRepository.updateConfidenceThreshold(value)

    suspend fun updateMaxObjects(value: Int) =
        settingsRepository.updateMaxObjects(value)

    suspend fun updateSamePlaneThreshold(value: Float) =
        settingsRepository.updateSamePlaneThreshold(value)

    // Camera Settings
    suspend fun updateTargetResolution(width: Int, height: Int) =
        settingsRepository.updateTargetResolution(width, height)

    suspend fun updateFrameCaptureInterval(intervalMs: Long) =
        settingsRepository.updateFrameCaptureInterval(intervalMs)

    // ML Settings
    suspend fun updateEnableGpuDelegate(enabled: Boolean) =
        settingsRepository.updateEnableGpuDelegate(enabled)

    suspend fun updateNumThreads(threads: Int) =
        settingsRepository.updateNumThreads(threads)

    // Performance Settings
    suspend fun updateShowPerformanceOverlay(show: Boolean) =
        settingsRepository.updateShowPerformanceOverlay(show)

    suspend fun updatePerformanceRefreshRate(rateMs: Long) =
        settingsRepository.updatePerformanceRefreshRate(rateMs)

    // Reference Object Sizes
    suspend fun updateCellPhoneSize(width: Float, height: Float) =
        settingsRepository.updateCellPhoneSize(width, height)

    suspend fun updateBookSize(width: Float, height: Float) =
        settingsRepository.updateBookSize(width, height)

    suspend fun updateBottleSize(width: Float, height: Float) =
        settingsRepository.updateBottleSize(width, height)

    suspend fun updateCupSize(width: Float, height: Float) =
        settingsRepository.updateCupSize(width, height)

    suspend fun updateKeyboardSize(width: Float, height: Float) =
        settingsRepository.updateKeyboardSize(width, height)

    // Reset
    suspend fun resetToDefaults() =
        settingsRepository.resetToDefaults()
}
