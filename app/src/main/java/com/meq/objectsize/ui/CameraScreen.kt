package com.meq.objectsize.ui

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.meq.objectsize.domain.model.DetectionResult
import com.meq.objectsize.domain.model.PerformanceMetrics
import com.meq.objectsize.utils.PermissionsHelper
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

    // Start camera when permission granted
    LaunchedEffect(Unit) {
        if (PermissionsHelper.hasCameraPermission(context)) {
            scope.launch {
                viewModel.cameraManager.startCamera(lifecycleOwner, previewView)
            }
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

/**
 * Canvas overlay that draws bounding boxes and labels
 */
@Composable
fun DetectionOverlay(
    detections: List<DetectionResult>,
    referenceObject: DetectionResult?,
    measurements: Map<String, com.meq.objectsize.domain.SizeEstimate>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        detections.forEach { detection ->
            // Convert normalized coordinates to screen pixels
            val box = detection.boundingBox
            val left = box.left * canvasWidth
            val top = box.top * canvasHeight
            val right = box.right * canvasWidth
            val bottom = box.bottom * canvasHeight

            // Color: Green for reference, Cyan for others
            val color = if (detection == referenceObject) {
                Color.Green
            } else {
                Color.Cyan
            }

            // Draw bounding box rectangle
            drawRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = 4f)
            )

            // Build label text with size if available
            val labelText = buildString {
                if (detection == referenceObject) {
                    append("${detection.label} (REF)")
                } else {
                    append(detection.label)
                    // Add size measurement if calculated
                    val key = "${detection.label}_${detection.boundingBox.left.toInt()}"
                    measurements[key]?.let { estimate ->
                        append("\n${estimate.toDisplayString()}")
                    }
                }
                append(" ${(detection.confidence * 100).toInt()}%")
            }

            // Measure and draw text
            val textLayoutResult = textMeasurer.measure(
                text = labelText,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 14.sp,
                    background = Color.Black.copy(alpha = 0.8f)
                )
            )

            // Position text above bounding box (or at top if not enough space)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = left,
                    y = (top - textLayoutResult.size.height - 4).coerceAtLeast(0f)
                )
            )
        }
    }
}

/**
 * Info card at top showing detection statistics
 */
@Composable
fun InfoPanel(
    detectionCount: Int,
    referenceObject: DetectionResult?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Detections: $detectionCount",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )

            if (referenceObject != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reference: ${referenceObject.label}",
                    color = Color.Green,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (detectionCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No reference object detected",
                    color = Color.Yellow,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Control buttons at bottom
 */
@Composable
fun ControlPanel(
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onClear: () -> Unit,
    onCaptureSnapshot: () -> Unit,
    onShareReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDebug = remember(context) {
        context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row - main controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pause/Resume button
            Button(
                onClick = onTogglePause,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPaused) Color.Green else Color.Red
                )
            ) {
                Text(if (isPaused) "Resume" else "Pause")
            }

            // Clear detections button
            Button(
                onClick = onClear,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray
                )
            ) {
                Text("Clear")
            }
        }

        // Bottom row - profiler controls (only in DEBUG)
        if (isDebug) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Snapshot button
                Button(
                    onClick = onCaptureSnapshot,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3) // Blue
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("📸 Snapshot", fontSize = 12.sp)
                }

                // Share report button
                Button(
                    onClick = onShareReport,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50) // Green
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("📊 Report", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PerformanceOverlay(
    metrics: PerformanceMetrics?,
    modifier: Modifier = Modifier
) {
    if (metrics == null) return

    Card(
        modifier = modifier.alpha(0.85f),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Performance Monitor",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // FPS
            MetricRow(
                label = "FPS",
                value = String.format("%.1f", metrics.fps),
                color = when {
                    metrics.fps >= 25f -> Color.Green
                    metrics.fps >= 15f -> Color.Yellow
                    else -> Color.Red
                }
            )

            // Inference time
            MetricRow(
                label = "Inference",
                value = "${metrics.inferenceTimeMs}ms",
                color = when {
                    metrics.inferenceTimeMs <= 50 -> Color.Green
                    metrics.inferenceTimeMs <= 100 -> Color.Yellow
                    else -> Color.Red
                }
            )

            // Total time
            MetricRow(
                label = "Total",
                value = "${metrics.totalTimeMs}ms",
                color = Color.White
            )

            // Memory
            MetricRow(
                label = "Memory",
                value = String.format("%.1f MB", metrics.memoryUsedMb),
                color = when {
                    metrics.memoryUsedMb <= 100 -> Color.Green
                    metrics.memoryUsedMb <= 200 -> Color.Yellow
                    else -> Color.Red
                }
            )

            // Breakdown
            LinearProgressIndicator(
                progress = { metrics.preprocessTimeMs.toFloat() / metrics.totalTimeMs },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(top = 8.dp),
                color = Color.Cyan,
                trackColor = Color.DarkGray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pre: ${metrics.preprocessTimeMs}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Cyan,
                    fontSize = 8.sp
                )
                Text(
                    text = "Inf: ${metrics.inferenceTimeMs}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Green,
                    fontSize = 8.sp
                )
                Text(
                    text = "Post: ${metrics.postprocessTimeMs}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Yellow,
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}