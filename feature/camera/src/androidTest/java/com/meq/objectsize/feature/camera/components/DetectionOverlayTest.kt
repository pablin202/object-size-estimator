package com.meq.objectsize.feature.camera.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.meq.objectsize.domain.entity.BoundingBox
import com.meq.objectsize.domain.entity.DetectionResult
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetectionOverlayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun givenMultipleDetections_whenDisplayOverlay_thenShowsDetections() {
        // Given
        val detections = listOf(
            DetectionResult(
                label = "cat",
                confidence = 0.95f,
                boundingBox = BoundingBox(0.1f, 0.1f, 0.5f, 0.5f)
            ),
            DetectionResult(
                label = "dog",
                confidence = 0.87f,
                boundingBox = BoundingBox(0.6f, 0.6f, 0.9f, 0.9f)
            )
        )

        // When
        composeTestRule.setContent {
            DetectionOverlay(
                detections = detections,
                referenceObject = null,
                measurements = emptyMap()
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Detection overlay").assertExists()
    }

    @Test
    fun givenNoDetections_whenDisplayOverlay_thenShowsEmpty() {
        // When
        composeTestRule.setContent {
            DetectionOverlay(
                detections = emptyList(),
                referenceObject = null,
                measurements = emptyMap()
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Detection overlay").assertExists()
    }
}
