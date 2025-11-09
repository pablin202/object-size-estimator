package meq.objectsize

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.meq.objectsize.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraIntegrationTest {

    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA
    )

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_launches_successfully() {
        // Verify that the app launches without crashes
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun camera_permission_flow() {
        // This test requires permission handling
        // You can use UiAutomator to handle the permission dialog

        composeTestRule.waitForIdle()

        // Verify that something is displayed (even if it's the permission)
        composeTestRule.onRoot().assertExists()
    }
}