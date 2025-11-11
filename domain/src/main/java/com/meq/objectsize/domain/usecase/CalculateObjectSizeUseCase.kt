package com.meq.objectsize.domain.usecase

import com.meq.objectsize.domain.entity.BoundingBox
import com.meq.objectsize.domain.entity.SizeEstimate
import kotlin.math.abs
import javax.inject.Inject

/**
 * Use case for calculating real-world object sizes using reference object comparison
 *
 * Core algorithm:
 * 1. Use reference object's known size to calculate pixels-per-cm ratio
 * 2. Apply this ratio to target object's bounding box
 * 3. Verify objects are in same plane for accuracy
 *
 * Assumptions:
 * - Camera is roughly perpendicular to surface (~90° angle)
 * - Objects are on the same horizontal plane (e.g., table)
 * - Reference object size is accurate
 */
class CalculateObjectSizeUseCase @Inject constructor() {

    /**
     * Calculate target object's real-world dimensions
     */
    operator fun invoke(
        referenceBox: BoundingBox,
        targetBox: BoundingBox,
        referenceRealWidth: Float,
        referenceRealHeight: Float
    ): SizeEstimate? {
        // Validate inputs
        if (!referenceBox.isValid() || !targetBox.isValid()) {
            return null
        }

        if (referenceRealWidth <= 0 || referenceRealHeight <= 0) {
            return null
        }

        // Check if objects are roughly in same plane
        if (!areObjectsInSamePlane(referenceBox, targetBox)) {
            return null
        }

        // Calculate pixels per centimeter for both dimensions
        val pixelsPerCmWidth = referenceBox.width() / referenceRealWidth
        val pixelsPerCmHeight = referenceBox.height() / referenceRealHeight

        // Calculate target dimensions
        val targetWidth = targetBox.width() / pixelsPerCmWidth
        val targetHeight = targetBox.height() / pixelsPerCmHeight

        // Calculate confidence based on reference box proportions
        val referenceAspectRatio = referenceBox.width() / referenceBox.height()
        val expectedAspectRatio = referenceRealWidth / referenceRealHeight
        val aspectRatioError = abs(referenceAspectRatio - expectedAspectRatio) / expectedAspectRatio
        val confidence = (1f - aspectRatioError).coerceIn(0f, 1f)

        return SizeEstimate(
            widthCm = targetWidth,
            heightCm = targetHeight,
            confidence = confidence
        )
    }

    /**
     * Check if two objects are in approximately the same plane
     */
    private fun areObjectsInSamePlane(
        box1: BoundingBox,
        box2: BoundingBox,
        threshold: Float = SAME_PLANE_THRESHOLD
    ): Boolean {
        val center1Y = box1.centerY()
        val center2Y = box2.centerY()
        val verticalDifference = abs(center1Y - center2Y)
        return verticalDifference < threshold
    }

    companion object {
        private const val SAME_PLANE_THRESHOLD = 0.2f
    }
}
