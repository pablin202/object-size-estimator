package com.meq.objectsize.domain

import com.meq.objectsize.domain.model.BoundingBox
import com.meq.objectsize.domain.model.DetectionResult
import kotlin.math.abs

/**
 * Calculates real-world object sizes using reference object comparison
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
class SizeCalculator {

    /**
     * Calculate target object's real-world dimensions
     */
    fun calculateSize(
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
    fun areObjectsInSamePlane(
        box1: BoundingBox,
        box2: BoundingBox,
        threshold: Float = SAME_PLANE_THRESHOLD
    ): Boolean {
        val center1Y = box1.centerY()
        val center2Y = box2.centerY()
        val verticalDifference = abs(center1Y - center2Y)
        return verticalDifference < threshold
    }

    /**
     * Find the best reference object from detections
     */
    fun findBestReference(
        detections: List<DetectionResult>,
        preferredLabels: List<String> = DEFAULT_REFERENCE_OBJECTS
    ): DetectionResult? {
        // Try preferred objects first
        for (label in preferredLabels) {
            val match = detections
                .filter { it.label.equals(label, ignoreCase = true) }
                .maxByOrNull { it.confidence }

            if (match != null && match.isValid(MIN_REFERENCE_CONFIDENCE)) {
                return match
            }
        }

        // Fallback: highest confidence detection
        return detections
            .filter { it.isValid(MIN_REFERENCE_CONFIDENCE) }
            .maxByOrNull { it.confidence }
    }

    companion object {
        private const val SAME_PLANE_THRESHOLD = 0.2f
        private const val MIN_REFERENCE_CONFIDENCE = 0.6f

        private val DEFAULT_REFERENCE_OBJECTS = listOf(
            "cell phone",
            "book",
            "bottle",
            "cup",
            "keyboard"
        )

        /**
         * Known sizes for common reference objects (in cm)
         */
        val KNOWN_OBJECT_SIZES = mapOf(
            "cell phone" to Pair(7f, 15f),
            "book" to Pair(15f, 23f),
            "bottle" to Pair(7f, 25f),
            "cup" to Pair(8f, 10f),
            "keyboard" to Pair(44f, 13f)
        )
    }
}

/**
 * Result of size calculation
 */
data class SizeEstimate(
    val widthCm: Float,
    val heightCm: Float,
    val confidence: Float
) {
    fun toDisplayString(): String {
        return "%.1f × %.1f cm".format(widthCm, heightCm)
    }
}