package com.meq.objectsize.domain.entity

/**
 * Platform-agnostic image representation
 *
 * This class allows the domain layer to remain independent of Android's Bitmap class,
 * enabling pure Kotlin/JVM implementation and easier testing.
 */
data class ImageData(
    val width: Int,
    val height: Int,
    val pixels: ByteArray,
    val format: ImageFormat = ImageFormat.ARGB_8888
) {
    init {
        require(width > 0) { "Width must be positive" }
        require(height > 0) { "Height must be positive" }
        require(pixels.isNotEmpty()) { "Pixel data cannot be empty" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageData

        if (width != other.width) return false
        if (height != other.height) return false
        if (!pixels.contentEquals(other.pixels)) return false
        if (format != other.format) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + pixels.contentHashCode()
        result = 31 * result + format.hashCode()
        return result
    }
}

/**
 * Supported image pixel formats
 */
enum class ImageFormat {
    /** 32-bit ARGB format (8 bits per channel) */
    ARGB_8888,

    /** 24-bit RGB format (8 bits per channel, no alpha) */
    RGB_888
}
