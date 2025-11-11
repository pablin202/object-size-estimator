package com.meq.objectsize.feature.camera.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meq.objectsize.domain.entity.PerformanceMetrics

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