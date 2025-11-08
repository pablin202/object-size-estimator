package com.meq.objectsize

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    // Permission state - triggers recomposition when changed
    private var hasPermission by mutableStateOf(false)
    private var permissionDenied by mutableStateOf(false)

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            hasPermission = true
        } else {
            permissionDenied = true
            Toast.makeText(
                this,
                "Camera permission is required for this app",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check initial permission state
        hasPermission = PermissionsHelper.hasCameraPermission(this)

        // Set Compose content
        setContent {
            ObjectSizeEstimatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        hasPermission -> {
                            // Permission granted - show camera
                            CameraScreen()
                        }
                        permissionDenied -> {
                            // Permission denied - show error screen
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Camera permission is required",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = {
                                        permissionDenied = false
                                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }) {
                                        Text("Grant Permission")
                                    }
                                }
                            }
                        }
                        else -> {
                            // Waiting for permission - show loading
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Requesting camera permission...")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Request permission after UI is set up
        if (!hasPermission && !permissionDenied) {
            requestPermissionLauncher.launch(PermissionsHelper.CAMERA_PERMISSION)
        }
    }
}