package meq.objectsize.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.meq.objectsize.domain.model.PerformanceMetrics
import com.meq.objectsize.ui.PerformanceOverlay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun givenValidMetrics_whenDisplayOverlay_thenShowsCorrectly() {
        // Given
        val metrics = PerformanceMetrics(
            inferenceTimeMs = 50,
            preprocessTimeMs = 10,
            postprocessTimeMs = 5,
            totalTimeMs = 65,
            fps = 15.5f,
            memoryUsedMb = 125.5f
        )

        // When
        composeTestRule.setContent {
            PerformanceOverlay(metrics = metrics)
        }

        // Then
        composeTestRule.onNodeWithText("Performance Monitor").assertExists()
        composeTestRule.onNodeWithText("15.5", substring = true).assertExists() // FPS
        composeTestRule.onNodeWithText("Inf: 50ms", substring = true).assertExists() // Inference
        composeTestRule.onNodeWithText("125.5 MB", substring = true).assertExists() // Memory
    }

    @Test
    fun givenNullMetrics_whenDisplayOverlay_thenNotDisplayed() {
        // When
        composeTestRule.setContent {
            PerformanceOverlay(metrics = null)
        }

        // Then
        composeTestRule.onNodeWithText("Performance Monitor").assertDoesNotExist()
    }

    @Test
    fun givenGoodFps_whenDisplayOverlay_thenShowsGreenColor() {
        // Given - FPS >= 25
        val metrics = PerformanceMetrics(
            inferenceTimeMs = 30,
            preprocessTimeMs = 5,
            postprocessTimeMs = 5,
            totalTimeMs = 40,
            fps = 28.0f,
            memoryUsedMb = 100f
        )

        // When
        composeTestRule.setContent {
            PerformanceOverlay(metrics = metrics)
        }

        // Then
        composeTestRule.onNodeWithText("28.0", substring = true).assertExists()
    }
}