package com.meq.objectsize.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.meq.objectsize.domain.entity.BoundingBox
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CalculateObjectSizeUseCase
 *
 * Tests cover:
 * - Size estimation logic with BoundingBox inputs
 * - Same plane validation
 * - Edge cases (invalid input, out of plane)
 */
class CalculateObjectSizeUseCaseTest {

    private lateinit var useCase: CalculateObjectSizeUseCase

    @Before
    fun setup() {
        useCase = CalculateObjectSizeUseCase()
    }

    @Test
    fun `invoke returns null for invalid reference box`() {
        // Given: Invalid reference box (negative coordinates)
        val invalidRef = BoundingBox(-1f, 0f, 0.5f, 0.5f)
        val validTarget = BoundingBox(0.1f, 0.1f, 0.2f, 0.3f)

        // When: Calculate size
        val result = useCase(invalidRef, validTarget, 7f, 15f)

        // Then: Returns null
        assertThat(result).isNull()
    }

    @Test
    fun `invoke returns null for invalid reference dimensions`() {
        // Given: Valid boxes but zero reference dimensions
        val refBox = BoundingBox(0.1f, 0.1f, 0.2f, 0.3f)
        val targetBox = BoundingBox(0.3f, 0.1f, 0.4f, 0.3f)

        // When: Calculate with zero width
        val result = useCase(refBox, targetBox, 0f, 15f)

        // Then: Returns null
        assertThat(result).isNull()
    }

    @Test
    fun `invoke returns proportional dimensions`() {
        // Given: Reference box 0.1 wide (representing 7cm phone)
        val refBox = BoundingBox(0.1f, 0.1f, 0.2f, 0.25f) // 0.1 x 0.15
        val targetBox = BoundingBox(0.3f, 0.1f, 0.5f, 0.4f) // 0.2 x 0.3 (2x bigger)

        // When: Calculate size
        val result = useCase(refBox, targetBox, 7f, 15f)

        // Then: Target should be approximately 2x the reference dimensions
        assertThat(result).isNotNull()
        assertThat(result!!.widthCm).isWithin(1f).of(14f)  // 2x 7cm
        assertThat(result.heightCm).isWithin(1f).of(30f)   // 2x 15cm
    }

    @Test
    fun `invoke returns null for objects not in same plane`() {
        // Given: Boxes with large vertical separation
        val refBox = BoundingBox(0.1f, 0.1f, 0.2f, 0.2f)
        val targetBox = BoundingBox(0.3f, 0.8f, 0.4f, 0.9f) // Much lower

        // When: Calculate size
        val result = useCase(refBox, targetBox, 7f, 15f)

        // Then: Returns null (different planes)
        assertThat(result).isNull()
    }

    @Test
    fun `invoke calculates confidence based on aspect ratio accuracy`() {
        // Given: Perfect aspect ratio match
        val refBox = BoundingBox(0.1f, 0.1f, 0.2f, 0.3f) // 1:2 aspect ratio
        val targetBox = BoundingBox(0.3f, 0.1f, 0.5f, 0.5f) // 1:2 aspect ratio (same)

        // When: Calculate with 7cm x 14cm reference (1:2 ratio)
        val result = useCase(refBox, targetBox, 7f, 14f)

        // Then: Confidence should be high (close to 1.0)
        assertThat(result).isNotNull()
        assertThat(result!!.confidence).isGreaterThan(0.9f)
    }

    @Test
    fun `invoke handles small boxes correctly`() {
        // Given: Small bounding boxes
        val refBox = BoundingBox(0.01f, 0.01f, 0.05f, 0.06f) // Very small
        val targetBox = BoundingBox(0.1f, 0.1f, 0.18f, 0.2f) // Also small

        // When: Calculate size
        val result = useCase(refBox, targetBox, 7f, 15f)

        // Then: Returns valid result (not null)
        assertThat(result).isNotNull()
        assertThat(result!!.widthCm).isGreaterThan(0f)
        assertThat(result.heightCm).isGreaterThan(0f)
    }
}
