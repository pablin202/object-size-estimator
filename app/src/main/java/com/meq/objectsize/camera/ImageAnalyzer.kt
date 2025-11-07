package com.meq.objectsize.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.meq.objectsize.domain.model.DetectionResult
import com.meq.objectsize.domain.model.PerformanceMetrics
import com.meq.objectsize.ml.ObjectDetector
import com.meq.objectsize.utils.ImageUtils
import com.meq.objectsize.utils.LeakCanaryWatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Analyzes camera frames and runs object detection using reactive Flow streams
 *
 * This analyzer:
 * 1. Converts camera frame (YUV) to Bitmap (RGB)
 * 2. Runs ML model inference
 * 3. Emits detection results via SharedFlow (reactive, memory-safe)
 *
 * Flow Architecture Benefits:
 * - Automatic lifecycle management
 * - Backpressure handling
 * - No memory leaks (auto-cancellation)
 * - Multiple collectors support
 *
 * @param objectDetector ML model for object detection
 */
class ImageAnalyzer @Inject constructor(
    private val objectDetector: ObjectDetector
) : ImageAnalysis.Analyzer {

    // CoroutineScope tied to analyzer lifecycle
    // SupervisorJob: isolated failures
    private val analyzerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // SharedFlow for detection results
    // replay = 1: Keep last value for new collectors (hot + cached)
    // extraBufferCapacity = 16: Buffer for smooth backpressure
    private val _detectionsFlow = MutableSharedFlow<List<DetectionResult>>(
        replay = 1,
        extraBufferCapacity = 16
    )
    val detectionsFlow: SharedFlow<List<DetectionResult>> = _detectionsFlow.asSharedFlow()

    // Forward detector's metrics flow (no need to re-emit)
    val metricsFlow: SharedFlow<PerformanceMetrics> = objectDetector.metricsFlow

    // FPS throttling
    private var lastAnalyzedTimestamp = 0L
    private val minTimeBetweenFramesMs = 100L // ~10 FPS for ML inference

    companion object {
        private const val TAG = "ImageAnalyzer"
    }

    /**
     * Analyze a single image frame
     *
     * Called by CameraX for each frame from camera
     */
    override fun analyze(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()

        // Throttle frame processing for performance
        if (currentTimestamp - lastAnalyzedTimestamp < minTimeBetweenFramesMs) {
            imageProxy.close()
            return
        }

        lastAnalyzedTimestamp = currentTimestamp

        // Process frame asynchronously in analyzerScope
        analyzerScope.launch {
            try {
                // Convert ImageProxy to Bitmap
                val bitmap = imageProxyToBitmap(imageProxy)

                // Run object detection
                val detections = objectDetector.detect(bitmap)

                // Emit results via Flow (non-blocking)
                // tryEmit: doesn't suspend, returns false if buffer full
                val emitted = _detectionsFlow.tryEmit(detections)
                if (!emitted) {
                    Log.w(TAG, "Detections buffer full, dropping frame (backpressure)")
                }

                // Cleanup
                bitmap.recycle()

            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing image", e)
            } finally {
                imageProxy.close()
            }
        }
    }

    /**
     * Convert CameraX ImageProxy to Bitmap
     *
     * @param imageProxy CameraX image
     * @return RGB Bitmap
     */
    @OptIn(ExperimentalGetImage::class)
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        // Convert to bitmap using utility
        val bitmap = ImageUtils.imageToBitmap(imageProxy.image!!)

        // Handle rotation if needed
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        return if (rotationDegrees != 0) {
            ImageUtils.rotateBitmap(bitmap, rotationDegrees.toFloat())
        } else {
            bitmap
        }
    }

    /**
     * Release resources and cancel all ongoing operations
     *
     * IMPORTANT: This cancels the analyzerScope which:
     * - Stops all image processing coroutines
     * - Prevents memory leaks
     * - Cancels Flow emissions
     */
    fun close() {
        Log.d(TAG, "Closing ImageAnalyzer and cancelling scope")

        // Watch scope for leaks before cancelling
        LeakCanaryWatchers.watchCoroutineScope(analyzerScope, "ImageAnalyzer.analyzerScope")
        analyzerScope.cancel()

        objectDetector.close()
    }
}