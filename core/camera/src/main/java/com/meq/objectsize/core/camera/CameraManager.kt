package com.meq.objectsize.core.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.meq.objectsize.domain.entity.DetectionResult
import com.meq.objectsize.domain.entity.PerformanceMetrics
import com.meq.objectsize.core.common.LeakCanaryWatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages CameraX lifecycle and provides detection stream using Flow architecture
 *
 * This class handles:
 * - Camera initialization and binding
 * - Image analysis pipeline
 * - Detection result streaming via StateFlow (reactive, memory-safe)
 *
 * Flow Architecture:
 * - ImageAnalyzer emits via SharedFlow
 * - CameraManager collects and transforms to StateFlow
 * - ViewModel observes StateFlow (lifecycle-aware)
 * - Auto-cancellation on stopCamera() prevents leaks
 */
@Singleton
class CameraManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val imageAnalyzer: ImageAnalyzer
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var preview: Preview? = null

    // Executor for camera operations
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // CoroutineScope for Flow collection
    // SupervisorJob: isolated failures
    // Dispatchers.Main: UI updates are safe
    private val cameraScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // Detection results stream (StateFlow: holds current value)
    private val _detections = MutableStateFlow<List<DetectionResult>>(emptyList())
    val detections: StateFlow<List<DetectionResult>> = _detections.asStateFlow()

    // Performance metrics stream (StateFlow: holds current value)
    private val _performanceMetrics = MutableStateFlow<PerformanceMetrics?>(null)
    val performanceMetrics: StateFlow<PerformanceMetrics?> = _performanceMetrics.asStateFlow()

    companion object {
        private const val TAG = "CameraManager"
    }

    /**
     * Initialize camera and bind to lifecycle
     *
     * @param lifecycleOwner Activity or Fragment lifecycle
     * @param previewView SurfaceView for camera preview
     */
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        try {
            // Get camera provider
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()

            // Setup preview
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Setup image analysis
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor, imageAnalyzer)
                }

            // Collect detections from ImageAnalyzer Flow
            // launchIn(cameraScope): auto-cancelled when scope is cancelled
            imageAnalyzer.detectionsFlow
                .onEach { detections ->
                    _detections.value = detections
                }
                .launchIn(cameraScope)

            // Collect performance metrics from ImageAnalyzer Flow
            imageAnalyzer.metricsFlow
                .onEach { metrics ->
                    _performanceMetrics.value = metrics
                }
                .launchIn(cameraScope)

            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Unbind all use cases before rebinding
            cameraProvider?.unbindAll()

            // Bind use cases to lifecycle
            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )

            Log.d(TAG, "Camera started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start camera", e)
        }
    }

    /**
     * Stop camera and release resources
     *
     * IMPORTANT for memory leak prevention:
     * - Cancels cameraScope (stops all Flow collections)
     * - Closes imageAnalyzer (cancels detector operations)
     * - Unbinds camera use cases
     * - Shuts down executor
     */
    fun stopCamera() {
        Log.d(TAG, "Stopping camera and cancelling flows")

        // Cancel all Flow collections (prevents memory leaks)
        LeakCanaryWatchers.watchCoroutineScope(cameraScope, "CameraManager.cameraScope")
        cameraScope.cancel()

        // Unbind camera
        cameraProvider?.unbindAll()

        // Shutdown executor
        cameraExecutor.shutdown()

        // Close analyzer (cancels detector) and watch for leaks
        LeakCanaryWatchers.watchImageAnalyzer(imageAnalyzer, "stopCamera called")
        imageAnalyzer.close()

        Log.d(TAG, "Camera stopped successfully")
    }

    /**
     * Pause detection (e.g., when app goes to background)
     */
    fun pauseDetection() {
        imageAnalysis?.clearAnalyzer()
    }

    /**
     * Resume detection
     */
    fun resumeDetection() {
        imageAnalysis?.setAnalyzer(cameraExecutor, imageAnalyzer)
    }
}
