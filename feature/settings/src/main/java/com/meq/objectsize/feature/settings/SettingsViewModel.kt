package com.meq.objectsize.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meq.objectsize.domain.entity.AppSettings
import com.meq.objectsize.domain.usecase.GetSettingsUseCase
import com.meq.objectsize.domain.usecase.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 *
 * Uses domain use cases to access and modify settings.
 * Follows Clean Architecture - depends only on domain layer.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettings: GetSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase
) : ViewModel() {

    /**
     * Current app settings as StateFlow
     * UI observes this for reactive updates
     */
    val settings: StateFlow<AppSettings> = getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings.DEFAULT
        )

    // Detection Settings

    fun updateConfidenceThreshold(value: Float) {
        viewModelScope.launch {
            updateSettings.updateConfidenceThreshold(value)
        }
    }

    fun updateMaxObjects(value: Int) {
        viewModelScope.launch {
            updateSettings.updateMaxObjects(value)
        }
    }

    fun updateSamePlaneThreshold(value: Float) {
        viewModelScope.launch {
            updateSettings.updateSamePlaneThreshold(value)
        }
    }

    // Camera Settings

    fun updateTargetResolution(width: Int, height: Int) {
        viewModelScope.launch {
            updateSettings.updateTargetResolution(width, height)
        }
    }

    fun updateFrameCaptureInterval(intervalMs: Long) {
        viewModelScope.launch {
            updateSettings.updateFrameCaptureInterval(intervalMs)
        }
    }

    // ML Settings

    fun updateEnableGpuDelegate(enabled: Boolean) {
        viewModelScope.launch {
            updateSettings.updateEnableGpuDelegate(enabled)
        }
    }

    fun updateNumThreads(threads: Int) {
        viewModelScope.launch {
            updateSettings.updateNumThreads(threads)
        }
    }

    // Performance Settings

    fun updateShowPerformanceOverlay(show: Boolean) {
        viewModelScope.launch {
            updateSettings.updateShowPerformanceOverlay(show)
        }
    }

    fun updatePerformanceRefreshRate(rateMs: Long) {
        viewModelScope.launch {
            updateSettings.updatePerformanceRefreshRate(rateMs)
        }
    }

    // Reference Object Sizes

    fun updateCellPhoneSize(width: Float, height: Float) {
        viewModelScope.launch {
            updateSettings.updateCellPhoneSize(width, height)
        }
    }

    fun updateBookSize(width: Float, height: Float) {
        viewModelScope.launch {
            updateSettings.updateBookSize(width, height)
        }
    }

    fun updateBottleSize(width: Float, height: Float) {
        viewModelScope.launch {
            updateSettings.updateBottleSize(width, height)
        }
    }

    fun updateCupSize(width: Float, height: Float) {
        viewModelScope.launch {
            updateSettings.updateCupSize(width, height)
        }
    }

    fun updateKeyboardSize(width: Float, height: Float) {
        viewModelScope.launch {
            updateSettings.updateKeyboardSize(width, height)
        }
    }

    // Reset

    fun resetToDefaults() {
        viewModelScope.launch {
            updateSettings.resetToDefaults()
        }
    }
}
