package com.meq.objectsize.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Helper for camera permission handling
 */
object PermissionsHelper {

    const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    const val CAMERA_PERMISSION_REQUEST_CODE = 1001

    /**
     * Check if camera permission is granted
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            CAMERA_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get array of required permissions for requestPermissions()
     */
    fun getRequiredPermissions(): Array<String> {
        return arrayOf(CAMERA_PERMISSION)
    }
}