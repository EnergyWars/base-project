package com.wafflehq.uikit.showcase

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.wafflehq.uikit.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ElementInspectorTest {

    @get:Rule
    val rule = createComposeRule()

    @Composable
    private fun InspectableTarget() {
        Box(
            modifier = Modifier
                .testTag("target")
                .size(40.dp)
                .background(Color.Red)
                .inspectId("6a.1"),
        )
    }

    @Test
    fun `disabled host never shows the id dialog on double tap`() {
        rule.setContent {
            AppTheme {
                ElementInspectorHost(enabled = false) {
                    InspectableTarget()
                }
            }
        }

        rule.onNodeWithTag("target").performTouchInput { doubleClick() }

        rule.onNodeWithText("6a.1").assertDoesNotExist()
    }

    @Test
    fun `enabled host shows the id dialog on double tap`() {
        rule.setContent {
            AppTheme {
                ElementInspectorHost(enabled = true) {
                    InspectableTarget()
                }
            }
        }

        rule.onNodeWithTag("target").performTouchInput { doubleClick() }

        rule.onNodeWithText("6a.1").assertExists()
    }

    @Test
    fun `local element inspector defaults to a no-op handler that does not throw`() {
        var callCount = 0
        rule.setContent {
            LocalElementInspector.current("some.id")
            callCount++
        }
        assertEquals(1, callCount)
    }
}
