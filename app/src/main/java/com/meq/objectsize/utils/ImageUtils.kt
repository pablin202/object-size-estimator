package com.meq.objectsize.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * Utilities for image processing and format conversion
 */
object ImageUtils {

    /**
     * Convert CameraX Image (YUV_420_888) to Bitmap
     *
     * CameraX provides images in YUV format, we need RGB Bitmap for ML model
     *
     * @param image CameraX Image object
     * @return RGB Bitmap
     */
    fun imageToBitmap(image: Image): Bitmap {
        require(image.format == ImageFormat.YUV_420_888) {
            "Image must be in YUV_420_888 format"
        }

        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Y plane goes first
        yBuffer.get(nv21, 0, ySize)

        // V and U planes (interleaved for NV21)
        val uvPixelStride = image.planes[2].pixelStride
        if (uvPixelStride == 1) {
            // Planes are already interleaved
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)
        } else {
            // Planes need interleaving
            val vPlane = ByteArray(vSize)
            val uPlane = ByteArray(uSize)
            vBuffer.get(vPlane)
            uBuffer.get(uPlane)

            var idNv21 = ySize
            for (i in 0 until vSize step uvPixelStride) {
                nv21[idNv21++] = vPlane[i]
                nv21[idNv21++] = uPlane[i]
            }
        }

        // Convert NV21 to Bitmap
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    /**
     * Rotate bitmap by degrees
     *
     * @param bitmap Source bitmap
     * @param degrees Rotation angle (0, 90, 180, 270)
     * @return Rotated bitmap
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap

        val matrix = Matrix().apply {
            postRotate(degrees)
        }

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    /**
     * Calculate rotation needed to align camera with display
     *
     * @param cameraOrientation Camera sensor orientation (0, 90, 180, 270)
     * @param deviceRotation Device rotation (Surface.ROTATION_*)
     * @param isFrontFacing Whether using front camera
     * @return Rotation degrees needed
     */
    fun getImageRotation(
        cameraOrientation: Int,
        deviceRotation: Int,
        isFrontFacing: Boolean = false
    ): Int {
        val deviceDegrees = when (deviceRotation) {
            android.view.Surface.ROTATION_0 -> 0
            android.view.Surface.ROTATION_90 -> 90
            android.view.Surface.ROTATION_180 -> 180
            android.view.Surface.ROTATION_270 -> 270
            else -> 0
        }

        val rotation = if (isFrontFacing) {
            (cameraOrientation + deviceDegrees) % 360
        } else {
            (cameraOrientation - deviceDegrees + 360) % 360
        }

        return rotation
    }
}