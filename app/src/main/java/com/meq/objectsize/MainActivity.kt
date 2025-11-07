package com.meq.objectsize

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.meq.objectsize.ui.CameraScreen
import com.meq.objectsize.ui.theme.ObjectSizeEstimatorTheme
import com.meq.objectsize.utils.PermissionsHelper
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity that hosts the camera screen
 *
 * Handles:
 * - Permission requests
 * - Compose setup
 * - Theme application
 *
 * @AndroidEntryPoint enables Hilt injection
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, UI will handle camera start
        } else {
            // Permission denied
            Toast.makeText(
                this,
                "Camera permission is required for this app",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check and request camera permission
        if (!PermissionsHelper.hasCameraPermission(this)) {
            requestPermissionLauncher.launch(PermissionsHelper.CAMERA_PERMISSION)
        }

        // Set Compose content
        setContent {
            ObjectSizeEstimatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraScreen()
                }
            }
        }
    }
}