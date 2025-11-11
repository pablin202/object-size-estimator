package com.meq.objectsize.domain.usecase

import com.meq.objectsize.domain.entity.DetectionResult
import javax.inject.Inject

/**
 * Use case for finding the best reference object from detections
 *
 * Priority:
 * 1. Preferred reference objects (cell phone, book, etc.)
 * 2. Highest confidence detection above threshold
 */
class FindBestReferenceUseCase @Inject constructor() {

    /**
     * Find the best reference object from detections
     *
     * @param detections List of detected objects
     * @param preferredLabels List of preferred reference object labels
     * @return Best reference object or null if none found
     */
    operator fun invoke(
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
         * Format: label to Pair(width, height)
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
