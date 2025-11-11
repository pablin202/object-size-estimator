package com.meq.objectsize.core.performance

import com.google.common.truth.Truth.assertThat
import com.meq.objectsize.domain.entity.PerformanceMetrics
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PerformanceMonitor
 *
 * Tests cover:
 * - Metrics aggregation (average inference time, FPS)
 * - Rolling window behavior (max 30 samples)
 * - Memory tracking
 */
class PerformanceMonitorTest {

    private lateinit var monitor: PerformanceMonitor

    @Before
    fun setup() {
        monitor = PerformanceMonitor()
    }

    @Test
    fun `recordInference stores metrics`() {
        // Given: Empty monitor
        assertThat(monitor.getAverageInferenceTime()).isEqualTo(0f)

        // When: Record a metric
        val metrics = createMetrics(inferenceMs = 50)
        monitor.recordInference(metrics)

        // Then: Average reflects the single value
        assertThat(monitor.getAverageInferenceTime()).isEqualTo(50f)
    }

    @Test
    fun `getAverageInferenceTime calculates correct average`() {
        // Given: Multiple inference times
        monitor.recordInference(createMetrics(inferenceMs = 40))
        monitor.recordInference(createMetrics(inferenceMs = 60))
        monitor.recordInference(createMetrics(inferenceMs = 50))

        // When: Get average
        val average = monitor.getAverageInferenceTime()

        // Then: Average is 50ms
        assertThat(average).isEqualTo(50f)
    }

    @Test
    fun `rolling window maintains max 30 samples`() {
        // Given: 35 inference times
        repeat(35) { i ->
            monitor.recordInference(createMetrics(inferenceMs = (10 + i).toLong()))
        }

        // When: Get average (should only consider last 30)
        val average = monitor.getAverageInferenceTime()

        // Then: Average is based on last 30 values (15-44)
        // Average of 15..44 = 29.5
        assertThat(average).isWithin(1f).of(29.5f)
    }

    @Test
    fun `getAverageFps returns zero when no samples`() {
        // Given: No samples
        // When: Get FPS
        val fps = monitor.getAverageFps()

        // Then: Returns 0
        assertThat(fps).isEqualTo(0f)
    }

    @Test
    fun `getAverageFps calculates correct FPS from inference times`() {
        // Given: 3 inferences at 50ms each
        repeat(3) {
            monitor.recordInference(createMetrics(inferenceMs = 50))
        }

        // When: Get average FPS
        val fps = monitor.getAverageFps()

        // Then: 50ms per frame = 20 FPS
        assertThat(fps).isEqualTo(20f)
    }

    @Test
    fun `calculateFps returns valid FPS based on time delta`() {
        // Given: Fresh monitor
        // When: Calculate FPS (uses System.currentTimeMillis internally)
        val fps = monitor.calculateFps()

        // Then: FPS is non-negative
        assertThat(fps >= 0f).isTrue()
    }

    @Test
    fun `getCurrentMemoryMb returns positive value`() {
        // When: Get current memory
        val memoryMb = monitor.getCurrentMemoryMb()

        // Then: Memory is positive
        assertThat(memoryMb).isGreaterThan(0f)
    }

    @Test
    fun `getMaxMemoryMb returns positive value`() {
        // When: Get max memory
        val maxMemory = monitor.getMaxMemoryMb()

        // Then: Max memory is positive
        assertThat(maxMemory).isGreaterThan(0f)
    }

    @Test
    fun `getCurrentMemoryMb is less than or equal to max memory`() {
        // When: Get both memory values
        val current = monitor.getCurrentMemoryMb()
        val max = monitor.getMaxMemoryMb()

        // Then: Current is less than or equal to max
        assertThat(current <= max).isTrue()
    }

    @Test
    fun `multiple recordings maintain accurate statistics`() {
        // Given: Various inference times simulating real usage
        val times = listOf(45L, 50L, 55L, 48L, 52L, 60L, 40L, 55L)

        // When: Record all
        times.forEach { time ->
            monitor.recordInference(createMetrics(inferenceMs = time))
        }

        // Then: Average is correct
        val expectedAvg = times.average().toFloat()
        assertThat(monitor.getAverageInferenceTime()).isWithin(0.1f).of(expectedAvg)

        // And: FPS is calculated correctly from average
        val expectedFps = if (expectedAvg > 0) 1000f / expectedAvg else 0f
        assertThat(monitor.getAverageFps()).isWithin(0.5f).of(expectedFps)
    }

    private fun createMetrics(
        preprocessMs: Long = 10,
        inferenceMs: Long = 50,
        postprocessMs: Long = 5
    ): PerformanceMetrics {
        return PerformanceMetrics(
            preprocessTimeMs = preprocessMs,
            inferenceTimeMs = inferenceMs,
            postprocessTimeMs = postprocessMs,
            totalTimeMs = preprocessMs + inferenceMs + postprocessMs,
            fps = 1000f / (preprocessMs + inferenceMs + postprocessMs),
            memoryUsedMb = 10f
        )
    }
}
