package com.meq.objectsize.domain.entity

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
