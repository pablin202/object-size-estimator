package com.meq.objectsize.domain.entity

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
