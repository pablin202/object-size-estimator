package com.meq.objectsize.core.ml

import android.graphics.Bitmap
import com.meq.objectsize.domain.entity.ImageData
import com.meq.objectsize.domain.entity.ImageFormat
import java.nio.ByteBuffer

/**
 * Extension functions to convert between Android Bitmap and platform-agnostic ImageData
 *
 * These functions bridge the gap between the Android-specific Bitmap class
 * and the pure Kotlin domain layer.
 */

/**
 * Convert Android Bitmap to platform-agnostic ImageData
 */
fun Bitmap.toImageData(): ImageData {
    val width = this.width
    val height = this.height
    val pixelCount = width * height

    // Extract pixels as IntArray (ARGB_8888 format)
    val pixels = IntArray(pixelCount)
    this.getPixels(pixels, 0, width, 0, 0, width, height)

    // Convert to ByteArray (4 bytes per pixel: A, R, G, B)
    val byteArray = ByteArray(pixelCount * 4)
    var byteIndex = 0

    for (pixel in pixels) {
        byteArray[byteIndex++] = ((pixel shr 24) and 0xFF).toByte()  // A
        byteArray[byteIndex++] = ((pixel shr 16) and 0xFF).toByte()  // R
        byteArray[byteIndex++] = ((pixel shr 8) and 0xFF).toByte()   // G
        byteArray[byteIndex++] = (pixel and 0xFF).toByte()            // B
    }

    return ImageData(
        width = width,
        height = height,
        pixels = byteArray,
        format = ImageFormat.ARGB_8888
    )
}

/**
 * Convert platform-agnostic ImageData to Android Bitmap
 */
fun ImageData.toBitmap(): Bitmap {
    require(format == ImageFormat.ARGB_8888) {
        "Only ARGB_8888 format is supported for conversion to Bitmap"
    }

    val pixelCount = width * height
    val intPixels = IntArray(pixelCount)

    var byteIndex = 0
    for (i in 0 until pixelCount) {
        val a = pixels[byteIndex++].toInt() and 0xFF
        val r = pixels[byteIndex++].toInt() and 0xFF
        val g = pixels[byteIndex++].toInt() and 0xFF
        val b = pixels[byteIndex++].toInt() and 0xFF

        intPixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    return Bitmap.createBitmap(intPixels, width, height, Bitmap.Config.ARGB_8888)
}
