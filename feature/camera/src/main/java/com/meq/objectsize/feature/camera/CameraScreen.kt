package com.meq.objectsize.feature.camera

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.meq.objectsize.feature.camera.components.ControlPanel
import com.meq.objectsize.feature.camera.components.DetectionOverlay
import com.meq.objectsize.feature.camera.components.InfoPanel
import com.meq.objectsize.feature.camera.components.PerformanceOverlay
import kotlinx.coroutines.launch

/**
 * Main camera screen with Compose
 *
 * Features:
 * - Full-screen camera preview
 * - Real-time object detection overlay
 * - Size measurements display
 * - Control buttons
 */
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val metrics by viewModel.performanceMetrics.collectAsState()

    // PreviewView for CameraX
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Start camera when screen is displayed
    // Note: MainActivity ensures this screen is only shown when permission is granted
    LaunchedEffect(Unit) {
        scope.launch {
            viewModel.cameraManager.startCamera(lifecycleOwner, previewView)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Detection overlay
        DetectionOverlay(
            detections = uiState.detections,
            referenceObject = uiState.referenceObject,
            measurements = uiState.measurements,
            modifier = Modifier.fillMaxSize()
        )

        // Top row with info panels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Info panel (left)
            InfoPanel(
                detectionCount = uiState.detections.size,
                referenceObject = uiState.referenceObject,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Performance overlay (right)
            PerformanceOverlay(
                metrics = metrics,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        // Control buttons at bottom
        ControlPanel(
            isPaused = uiState.isPaused,
            onTogglePause = { viewModel.togglePause() },
            onClear = { viewModel.clearDetections() },
            onCaptureSnapshot = { viewModel.captureSnapshot() },
            onShareReport = { viewModel.shareReport(context) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}








