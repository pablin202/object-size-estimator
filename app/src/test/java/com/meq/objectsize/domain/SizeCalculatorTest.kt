package com.meq.objectsize.domain

import com.google.common.truth.Truth.assertThat
import com.meq.objectsize.domain.model.BoundingBox
import com.meq.objectsize.domain.model.DetectionResult
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SizeCalculator
 *
 * Tests cover:
 * - Size estimation logic with BoundingBox inputs
 * - Same plane validation
 * - Reference object selection
 * - Edge cases (invalid input, out of plane)
 */
class SizeCalculatorTest {

    private lateinit var calculator: SizeCalculator

    @Before
    fun setup() {
        calculator = SizeCalculator()
    }

    @Test
    fun `calculateSize returns null for invalid reference box`() {
        // Given: Invalid reference box (negative coordinates)
        val invalidRef = BoundingBox(-1f, 0f, 0.5f, 0.5f)
        val validTarget = BoundingBox(0.1f, 0.1f, 0.2f, 0.3f)

        // When: Calculate size
        val result = calculator.calculateSize(invalidRef, validTarget, 7f, 15f)

        // Then: Returns null
        assertThat(result).isNull()
    }

    @Test
    fun `calculateSize returns null for invalid reference dimensions`() {
        // Given: Valid boxes but zero reference dimensions
        val refBox = BoundingBox(0.1f, 0.1f, 0.2f, 0.3f)
        val targetBox = BoundingBox(0.3f, 0.1f, 0.4f, 0.3f)

        // When: Calculate with zero width
        val result = calculator.calculateSize(refBox, targetBox, 0f, 15f)

        // Then: Returns null
        assertThat(result).isNull()
    }

    @Test
    fun `calculateSize returns proportional dimensions`() {
        // Given: Reference box 0.1 wide (representing 7cm phone)
        val refBox = BoundingBox(0.1f, 0.1f, 0.2f, 0.25f) // 0.1 x 0.15
        val targetBox = BoundingBox(0.3f, 0.1f, 0.5f, 0.4f) // 0.2 x 0.3 (2x bigger)

        // When: Calculate size
        val result = calculator.calculateSize(refBox, targetBox, 7f, 15f)

        // Then: Target should be approximately 2x the reference dimensions
        assertThat(result).isNotNull()
        assertThat(result!!.widthCm).isWithin(1f).of(14f)  // 2x 7cm
        assertThat(result.heightCm).isWithin(1f).of(30f)   // 2x 15cm
    }

    @Test
    fun `calculateSize returns null for objects not in same plane`() {
        // Given: Boxes with large vertical separation
        val refBox = BoundingBox(0.1f, 0.1f, 0.2f, 0.2f)
        val targetBox = BoundingBox(0.3f, 0.8f, 0.4f, 0.9f) // Much lower

        // When: Calculate size
        val result = calculator.calculateSize(refBox, targetBox, 7f, 15f)

        // Then: Returns null (different planes)
        assertThat(result).isNull()
    }

    @Test
    fun `areObjectsInSamePlane returns true for close objects`() {
        // Given: Two boxes with similar Y centers
        val box1 = BoundingBox(0.1f, 0.1f, 0.2f, 0.3f)
        val box2 = BoundingBox(0.3f, 0.12f, 0.4f, 0.32f)

        // When: Check if in same plane
        val result = calculator.areObjectsInSamePlane(box1, box2)

        // Then: Returns true
        assertThat(result).isTrue()
    }

    @Test
    fun `areObjectsInSamePlane returns false for distant objects`() {
        // Given: Boxes with large vertical difference
        val box1 = BoundingBox(0.1f, 0.1f, 0.2f, 0.2f)
        val box2 = BoundingBox(0.3f, 0.6f, 0.4f, 0.7f)

        // When: Check if in same plane
        val result = calculator.areObjectsInSamePlane(box1, box2)

        // Then: Returns false
        assertThat(result).isFalse()
    }

    @Test
    fun `findBestReference returns preferred reference object`() {
        // Given: Multiple detections including a cell phone
        val detections = listOf(
            createDetection("person", 0.1f, 0.1f, 0.2f, 0.3f, 0.85f),
            createDetection("cell phone", 0.3f, 0.1f, 0.4f, 0.2f, 0.75f),
            createDetection("cup", 0.5f, 0.1f, 0.6f, 0.15f, 0.90f)
        )

        // When: Find best reference
        val result = calculator.findBestReference(detections)

        // Then: Returns cell phone (first in preferred list, good confidence)
        assertThat(result).isNotNull()
        assertThat(result?.label).isEqualTo("cell phone")
    }

    @Test
    fun `findBestReference returns null when no valid objects`() {
        // Given: Detections with low confidence
        val detections = listOf(
            createDetection("cell phone", 0.1f, 0.1f, 0.2f, 0.2f, 0.3f)
        )

        // When: Find best reference
        val result = calculator.findBestReference(detections)

        // Then: Returns null (confidence too low)
        assertThat(result).isNull()
    }

    @Test
    fun `KNOWN_OBJECT_SIZES contains expected reference objects`() {
        // Verify all known objects are present
        assertThat(SizeCalculator.KNOWN_OBJECT_SIZES).containsKey("cell phone")
        assertThat(SizeCalculator.KNOWN_OBJECT_SIZES).containsKey("book")
        assertThat(SizeCalculator.KNOWN_OBJECT_SIZES).containsKey("bottle")
        assertThat(SizeCalculator.KNOWN_OBJECT_SIZES).containsKey("cup")
        assertThat(SizeCalculator.KNOWN_OBJECT_SIZES).containsKey("keyboard")

        // Verify dimensions are positive
        SizeCalculator.KNOWN_OBJECT_SIZES.values.forEach { (width, height) ->
            assertThat(width).isGreaterThan(0f)
            assertThat(height).isGreaterThan(0f)
        }
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
