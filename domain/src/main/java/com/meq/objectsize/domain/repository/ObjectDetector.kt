package com.meq.objectsize.domain.repository

import android.graphics.Bitmap
import com.meq.objectsize.domain.entity.DetectionResult
import com.meq.objectsize.domain.entity.PerformanceMetrics
import kotlinx.coroutines.flow.SharedFlow

/**
 * Interface for object detection using Flow-based reactive streams
 *
 * Benefits:
 * - Lifecycle-aware (auto-cancellation)
 * - Backpressure handling
 * - Memory leak safe
 * - Composable with operators
 *
 * Using Bitmap as input since it's Android-specific and easier to work with
 * from CameraX image analysis
 */
interface ObjectDetector {

    /**
     * Hot flow of performance metrics emitted after each inference
     *
     * SharedFlow because:
     * - Multiple collectors can observe
     * - Events can be missed if no active collectors (hot)
     * - No need to retain last value
     */
    val metricsFlow: SharedFlow<PerformanceMetrics>

    /**
     * Detect objects in a bitmap image
     *
     * @param bitmap Input image
     * @return List of detected objects with bounding boxes and confidence
     */
    suspend fun detect(bitmap: Bitmap): List<DetectionResult>

    /**
     * Check if detector is ready to use
     */
    fun isReady(): Boolean

    /**
     * Release resources (model, buffers, etc.)
     * Also cancels any ongoing Flow emissions
     */
    fun close()

    companion object {
        const val DEFAULT_CONFIDENCE_THRESHOLD = 0.5f
        const val MAX_DETECTIONS = 10
    }
}
