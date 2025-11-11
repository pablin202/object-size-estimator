package com.meq.objectsize.feature.camera

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meq.objectsize.core.camera.CameraManager
import com.meq.objectsize.core.performance.ProfilerHelper
import com.meq.objectsize.domain.usecase.CalculateObjectSizeUseCase
import com.meq.objectsize.domain.usecase.FindBestReferenceUseCase
import com.meq.objectsize.domain.entity.SizeEstimate
import com.meq.objectsize.domain.entity.DetectionResult
import com.meq.objectsize.domain.entity.PerformanceMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for camera screen
 *
 * Uses Hilt for dependency injection
 * Pure ViewModel for better testability
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    val cameraManager: CameraManager,
    private val calculateObjectSize: CalculateObjectSizeUseCase,
    private val findBestReference: FindBestReferenceUseCase,
    private val profilerHelper: ProfilerHelper
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    // Performance metrics from camera manager
    val performanceMetrics: StateFlow<PerformanceMetrics?> = cameraManager.performanceMetrics

    init {
        // Collect detections from camera
        viewModelScope.launch {
            cameraManager.detections.collect { detections ->
                processDetections(detections)
            }
        }
    }

    /**
     * Process new detections and calculate sizes
     */
    private fun processDetections(detections: List<DetectionResult>) {
        if (detections.isEmpty()) {
            _uiState.update { it.copy(
                detections = emptyList(),
                measurements = emptyMap(),
                referenceObject = null
            )}
            return
        }

        // Find reference object
        val reference = findBestReference(detections)

        // Calculate sizes if we have a reference
        val measurements = if (reference != null) {
            calculateSizes(detections, reference)
        } else {
            emptyMap()
        }

        _uiState.update { it.copy(
            detections = detections,
            referenceObject = reference,
            measurements = measurements
        )}
    }

    /**
     * Calculate sizes for all detected objects
     */
    private fun calculateSizes(
        detections: List<DetectionResult>,
        reference: DetectionResult
    ): Map<String, SizeEstimate> {
        // Get known size for reference object
        val (refWidth, refHeight) = FindBestReferenceUseCase.KNOWN_OBJECT_SIZES[reference.label]
            ?: return emptyMap()

        val measurements = mutableMapOf<String, SizeEstimate>()

        // Calculate size for each detection (except reference)
        detections.filter { it != reference }.forEach { detection ->
            val estimate = calculateObjectSize(
                referenceBox = reference.boundingBox,
                targetBox = detection.boundingBox,
                referenceRealWidth = refWidth,
                referenceRealHeight = refHeight
            )

            if (estimate != null) {
                // Use unique key: label + position
                val key = "${detection.label}_${detection.boundingBox.left.toInt()}"
                measurements[key] = estimate
            }
        }

        return measurements
    }

    /**
     * Toggle detection pause/resume
     */
    fun togglePause() {
        val newState = !_uiState.value.isPaused
        _uiState.update { it.copy(isPaused = newState) }

        if (newState) {
            cameraManager.pauseDetection()
        } else {
            cameraManager.resumeDetection()
        }
    }

    /**
     * Clear current detections
     */
    fun clearDetections() {
        _uiState.update { it.copy(
            detections = emptyList(),
            measurements = emptyMap(),
            referenceObject = null
        )}
    }

    /**
     * Capture performance snapshot manually
     */
    fun captureSnapshot() {
        viewModelScope.launch {
            profilerHelper.captureSnapshot(
                detectionCount = 0,
                objectsDetected = _uiState.value.detections.size
            )
        }
    }

    /**
     * Generate and share full profiler report
     */
    fun shareReport(context: Context) {
        viewModelScope.launch {
            profilerHelper.shareReport(context)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cameraManager.stopCamera()
    }
}

/**
 * UI state for camera screen
 */
data class CameraUiState(
    val detections: List<DetectionResult> = emptyList(),
    val referenceObject: DetectionResult? = null,
    val measurements: Map<String, SizeEstimate> = emptyMap(),
    val isPaused: Boolean = false
)
