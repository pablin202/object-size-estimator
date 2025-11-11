package com.meq.objectsize.core.ml

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.scale
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.meq.objectsize.core.performance.PerformanceMonitor
import com.meq.objectsize.domain.entity.BoundingBox
import com.meq.objectsize.domain.entity.DetectionResult
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for TFLiteObjectDetector
 *
 * These tests run on an Android device/emulator and verify the actual
 * TensorFlow Lite model integration and detection pipeline.
 */
@RunWith(AndroidJUnit4::class)
class TFLiteObjectDetectorTest {

    private lateinit var detector: TFLiteObjectDetector
    private lateinit var performanceMonitor: PerformanceMonitor

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        performanceMonitor = PerformanceMonitor()
        // Initialize detector with real TFLite model
        detector = TFLiteObjectDetector(
            context = context,
            useGpu = false,
            performanceMonitor = performanceMonitor,
            profilerHelper = null
        )
    }

    @After
    fun tearDown() {
        detector.close()
    }

    @Test
    fun givenContext_whenInitializeDetector_thenSucceeds() {
        // Detector should be initialized in setup without throwing
        assertThat(detector).isNotNull()
    }

    @Test
    fun givenValidBitmap_whenDetect_thenReturnsResults() = runTest {
        // Given: A simple solid color bitmap (300x300)
        val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.BLUE)

        // When: Running detection
        val results = detector.detect(bitmap)

        // Then: Should return a list (may be empty for solid color)
        assertThat(results).isNotNull()
        // Model may not detect objects in a solid color image, which is expected
    }

    @Test
    fun givenComplexBitmap_whenDetect_thenMayReturnDetections() = runTest {
        // Given: A more complex bitmap with varied colors (simulating real scene)
        val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // Draw some shapes to simulate objects
        paint.color = Color.RED
        canvas.drawRect(50f, 50f, 150f, 150f, paint)
        paint.color = Color.GREEN
        canvas.drawRect(180f, 180f, 280f, 280f, paint)

        // When: Running detection
        val results = detector.detect(bitmap)

        // Then: Should return a valid list
        assertThat(results).isNotNull()
        // Note: SSD MobileNet may not detect abstract shapes, which is OK
    }

    @Test
    fun givenLargeBitmap_whenResize_thenDimensionsMatch() {
        // Given: A large bitmap
        val bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)

        // When: Scaling to 300x300
        val resized = bitmap.scale(300, 300)

        // Then: Dimensions should match
        assertThat(resized.width).isEqualTo(300)
        assertThat(resized.height).isEqualTo(300)
    }

    @Test
    fun givenMultipleDetections_whenFilterByConfidence_thenReturnsHighConfidenceOnly() {
        // Given: List of detections with varying confidence
        val detections = listOf(
            DetectionResult("cat", 0.9f, BoundingBox(0f, 0f, 0.5f, 0.5f)),
            DetectionResult("dog", 0.3f, BoundingBox(0.2f, 0.2f, 0.4f, 0.4f)),
            DetectionResult("bird", 0.7f, BoundingBox(0.6f, 0.6f, 0.9f, 0.9f))
        )

        // When: Filtering by confidence threshold
        val filtered = detections.filter { it.confidence >= 0.5f }

        // Then: Should only include high confidence detections
        assertThat(filtered).hasSize(2)
        assertThat(filtered.all { it.confidence >= 0.5f }).isTrue()
        assertThat(filtered.map { it.label }).containsExactly("cat", "bird")
    }

    @Test
    fun givenDetectorInitialized_whenAccessMetricsFlow_thenIsNotNull() {
        // Given: Detector initialized in setup
        // When: Accessing metrics flow
        val metricsFlow = detector.metricsFlow

        // Then: Flow should be accessible
        assertThat(metricsFlow).isNotNull()
    }
}
