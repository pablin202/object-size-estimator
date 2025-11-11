package com.meq.objectsize.feature.camera.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.meq.objectsize.domain.entity.DetectionResult

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