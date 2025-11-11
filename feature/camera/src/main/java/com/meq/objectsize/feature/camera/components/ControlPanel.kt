package com.meq.objectsize.feature.camera.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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