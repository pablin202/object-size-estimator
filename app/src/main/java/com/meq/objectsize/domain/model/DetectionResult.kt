package com.meq.objectsize.domain.model

/**
 * Represents a detected object from the ML model
 *
 * @property label Object class name (e.g., "person", "cell phone")
 * @property confidence Detection confidence score (0.0 to 1.0)
 * @property boundingBox Normalized coordinates of the object (0.0 to 1.0)
 */
data class DetectionResult(
    val label: String,
    val confidence: Float,
    val boundingBox: BoundingBox
) {
    /**
     * Check if this detection meets minimum quality threshold
     */
    fun isValid(minConfidence: Float = 0.5f): Boolean {
        return confidence >= minConfidence && boundingBox.isValid()
    }
}

/**
 * Bounding box with normalized coordinates (0.0 to 1.0)
 * These coordinates are relative to image dimensions
 *
 * @property left Normalized left coordinate
 * @property top Normalized top coordinate
 * @property right Normalized right coordinate
 * @property bottom Normalized bottom coordinate
 */
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    /**
     * Calculate box width in normalized coordinates
     */
    fun width(): Float = right - left

    /**
     * Calculate box height in normalized coordinates
     */
    fun height(): Float = bottom - top

    /**
     * Calculate center Y coordinate (for plane detection)
     */
    fun centerY(): Float = (top + bottom) / 2f

    /**
     * Validate that coordinates are properly ordered and in range
     */
    fun isValid(): Boolean {
        return left >= 0f && top >= 0f &&
                right <= 1f && bottom <= 1f &&
                right > left && bottom > top
    }

    /**
     * Convert normalized coordinates to pixel coordinates
     *
     * @param imageWidth Width of the image in pixels
     * @param imageHeight Height of the image in pixels
     * @return BoundingBox with pixel coordinates
     */
    fun toPixelCoordinates(imageWidth: Int, imageHeight: Int): BoundingBox {
        return BoundingBox(
            left = left * imageWidth,
            top = top * imageHeight,
            right = right * imageWidth,
            bottom = bottom * imageHeight
        )
    }
}
