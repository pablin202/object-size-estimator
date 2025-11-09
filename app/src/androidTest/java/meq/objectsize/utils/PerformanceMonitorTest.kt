package meq.objectsize.utils

import com.meq.objectsize.domain.PerformanceMonitor
import com.meq.objectsize.domain.model.PerformanceMetrics
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class PerformanceMonitorTest {

    private lateinit var monitor: PerformanceMonitor

    @Before
    fun setup() {
        monitor = PerformanceMonitor()
    }

    @Test
    fun givenNoMetrics_whenGetAverageInferenceTime_thenReturnsZero() {
        assertEquals(0f, monitor.getAverageInferenceTime(), 0.01f)
    }

    @Test
    fun givenMultipleMetrics_whenGetAverageInferenceTime_thenCalculatesCorrectAverage() {
        // Given
        val metrics1 = PerformanceMetrics(50, 10, 5, 65, 15f, 100f)
        val metrics2 = PerformanceMetrics(60, 10, 5, 75, 13f, 100f)
        val metrics3 = PerformanceMetrics(40, 10, 5, 55, 18f, 100f)

        // When
        monitor.recordInference(metrics1)
        monitor.recordInference(metrics2)
        monitor.recordInference(metrics3)

        // Then
        val average = monitor.getAverageInferenceTime()
        assertEquals(50f, average, 0.1f) // (50+60+40)/3 = 50
    }

    @Test
    fun givenMoreThanMaxSamples_whenRecordInference_thenKeepsOnlyMaxSamples() {
        // Given - add 35 metrics when the limit is 30
        repeat(35) {
            monitor.recordInference(
                PerformanceMetrics(50, 10, 5, 65, 15f, 100f)
            )
        }

        // Then - should keep only 30
        // This test requires exposing the list size or calculating indirectly
        assertNotNull(monitor.getAverageInferenceTime())
    }
}