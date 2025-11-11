package com.meq.objectsize.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.meq.objectsize.domain.entity.BoundingBox
import com.meq.objectsize.domain.entity.DetectionResult
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for FindBestReferenceUseCase
 *
 * Tests cover:
 * - Reference object selection logic
 * - Preferred object priority
 * - Confidence threshold filtering
 * - Fallback behavior
 */
class FindBestReferenceUseCaseTest {

    private lateinit var useCase: FindBestReferenceUseCase

    @Before
    fun setup() {
        useCase = FindBestReferenceUseCase()
    }

    @Test
    fun `invoke returns preferred reference object`() {
        // Given: Multiple detections including a cell phone
        val detections = listOf(
            createDetection("person", 0.1f, 0.1f, 0.2f, 0.3f, 0.85f),
            createDetection("cell phone", 0.3f, 0.1f, 0.4f, 0.2f, 0.75f),
            createDetection("cup", 0.5f, 0.1f, 0.6f, 0.15f, 0.90f)
        )

        // When: Find best reference
        val result = useCase(detections)

        // Then: Returns cell phone (first in preferred list, good confidence)
        assertThat(result).isNotNull()
        assertThat(result?.label).isEqualTo("cell phone")
    }

    @Test
    fun `invoke returns null when no valid objects`() {
        // Given: Detections with low confidence
        val detections = listOf(
            createDetection("cell phone", 0.1f, 0.1f, 0.2f, 0.2f, 0.3f)
        )

        // When: Find best reference
        val result = useCase(detections)

        // Then: Returns null (confidence too low)
        assertThat(result).isNull()
    }

    @Test
    fun `invoke returns highest confidence when no preferred object found`() {
        // Given: No preferred objects, but other valid detections
        val detections = listOf(
            createDetection("person", 0.1f, 0.1f, 0.2f, 0.3f, 0.70f),
            createDetection("chair", 0.3f, 0.1f, 0.4f, 0.2f, 0.85f),
            createDetection("laptop", 0.5f, 0.1f, 0.6f, 0.15f, 0.65f)
        )

        // When: Find best reference
        val result = useCase(detections)

        // Then: Returns chair (highest confidence above threshold)
        assertThat(result).isNotNull()
        assertThat(result?.label).isEqualTo("chair")
    }

    @Test
    fun `invoke prioritizes preferred objects over higher confidence non-preferred`() {
        // Given: Preferred object with lower confidence than non-preferred
        val detections = listOf(
            createDetection("person", 0.1f, 0.1f, 0.2f, 0.3f, 0.95f), // High confidence
            createDetection("bottle", 0.3f, 0.1f, 0.4f, 0.2f, 0.70f)  // Lower confidence but preferred
        )

        // When: Find best reference
        val result = useCase(detections)

        // Then: Returns bottle (preferred despite lower confidence)
        assertThat(result).isNotNull()
        assertThat(result?.label).isEqualTo("bottle")
    }

    @Test
    fun `invoke returns null for empty detection list`() {
        // Given: Empty detections
        val detections = emptyList<DetectionResult>()

        // When: Find best reference
        val result = useCase(detections)

        // Then: Returns null
        assertThat(result).isNull()
    }

    @Test
    fun `KNOWN_OBJECT_SIZES contains expected reference objects`() {
        // Verify all known objects are present
        assertThat(FindBestReferenceUseCase.KNOWN_OBJECT_SIZES).containsKey("cell phone")
        assertThat(FindBestReferenceUseCase.KNOWN_OBJECT_SIZES).containsKey("book")
        assertThat(FindBestReferenceUseCase.KNOWN_OBJECT_SIZES).containsKey("bottle")
        assertThat(FindBestReferenceUseCase.KNOWN_OBJECT_SIZES).containsKey("cup")
        assertThat(FindBestReferenceUseCase.KNOWN_OBJECT_SIZES).containsKey("keyboard")

        // Verify dimensions are positive
        FindBestReferenceUseCase.KNOWN_OBJECT_SIZES.values.forEach { (width, height) ->
            assertThat(width).isGreaterThan(0f)
            assertThat(height).isGreaterThan(0f)
        }
    }

    @Test
    fun `invoke handles case-insensitive label matching`() {
        // Given: Detection with uppercase label
        val detections = listOf(
            createDetection("CELL PHONE", 0.1f, 0.1f, 0.2f, 0.2f, 0.80f)
        )

        // When: Find best reference
        val result = useCase(detections)

        // Then: Returns the detection (case-insensitive match)
        assertThat(result).isNotNull()
        assertThat(result?.label).isEqualTo("CELL PHONE")
    }

    private fun createDetection(
        label: String,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        confidence: Float = 0.9f
    ): DetectionResult {
        return DetectionResult(
            label = label,
            confidence = confidence,
            boundingBox = BoundingBox(left, top, right, bottom)
        )
    }
}
