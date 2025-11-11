package com.meq.objectsize.feature.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.meq.objectsize.domain.entity.SizeEstimate
import com.meq.objectsize.domain.entity.DetectionResult
import kotlin.collections.forEach

/**
 * Canvas overlay that draws bounding boxes and labels
 */
@Composable
fun DetectionOverlay(
    detections: List<DetectionResult>,
    referenceObject: DetectionResult?,
    measurements: Map<String, SizeEstimate>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier.semantics {
            contentDescription = "Detection overlay"
        }
    ) {
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