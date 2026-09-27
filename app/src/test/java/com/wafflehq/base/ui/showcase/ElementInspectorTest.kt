package com.wafflehq.base.ui.showcase

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
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
    private fun IdTarget(id: String = "6a.1", onClick: () -> Unit = {}) {
        Box(
            modifier = Modifier
                .testTag("target")
                .size(40.dp)
                .background(Color.Red)
                .inspectId(id, onClick),
        )
    }

    @Composable
    private fun TapTarget(id: String) {
        Box(
            modifier = Modifier
                .testTag("target")
                .size(40.dp)
                .background(Color.Red)
                .inspectTap(id),
        )
    }

    @Test
    fun `disabled host never shows the id dialog on double tap`() {
        rule.setContent {
            MaterialTheme {
                ElementInspectorHost(enabled = false) { IdTarget() }
            }
        }

        rule.onNodeWithTag("target").performTouchInput { doubleClick() }

        rule.onNodeWithText("6a.1").assertDoesNotExist()
    }

    @Test
    fun `default host is disabled`() {
        rule.setContent {
            MaterialTheme {
                ElementInspectorHost { IdTarget() }
            }
        }

        rule.onNodeWithTag("target").performTouchInput { doubleClick() }

        rule.onNodeWithText("6a.1").assertDoesNotExist()
    }

    @Test
    fun `enabled host shows the id dialog on double tap`() {
        rule.setContent {
            MaterialTheme {
                ElementInspectorHost(enabled = true) { IdTarget() }
            }
        }

        rule.onNodeWithTag("target").performTouchInput { doubleClick() }

        rule.onNodeWithText("6a.1").assertExists()
        rule.onNodeWithText("Element id").assertExists()
    }

    @Test
    fun `enabled host does not show the dialog on single tap`() {
        rule.setContent {
            MaterialTheme {
                ElementInspectorHost(enabled = true) { IdTarget() }
            }
        }

        rule.onNodeWithTag("target").performClick()

        rule.onNodeWithText("6a.1").assertDoesNotExist()
    }

    @Test
    fun `inspectId keeps the regular click handler`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                ElementInspectorHost(enabled = true) { IdTarget(onClick = { clicks++ }) }
            }
        }

        rule.onNodeWithTag("target").performClick()
        rule.waitForIdle()

        assertEquals(1, clicks)
    }

    @Test
    fun `inspectTap shows the id dialog on double tap when enabled`() {
        rule.setContent {
            MaterialTheme {
                ElementInspectorHost(enabled = true) { TapTarget("3a") }
            }
        }

        rule.onNodeWithTag("target").performTouchInput { doubleClick() }

        rule.onNodeWithText("3a").assertExists()
    }

    @Test
    fun `inspectTap is silent when disabled`() {
        rule.setContent {
            MaterialTheme {
                ElementInspectorHost(enabled = false) { TapTarget("3a") }
            }
        }

        rule.onNodeWithTag("target").performTouchInput { doubleClick() }

        rule.onNodeWithText("3a").assertDoesNotExist()
    }

    @Test
    fun `inspect section reports its own id on double tap`() {
        rule.setContent {
            MaterialTheme {
                ElementInspectorHost(enabled = true) {
                    InspectSection("S7") {
                        Box(Modifier.testTag("target").size(40.dp)) { Text("inner") }
                    }
                }
            }
        }

        rule.onNodeWithTag("target").performTouchInput { doubleClick() }

        rule.onNodeWithText("S7").assertExists()
    }

    @Test
    fun `close button dismisses the dialog`() {
        rule.setContent {
            MaterialTheme {
                ElementInspectorHost(enabled = true) { IdTarget() }
            }
        }
        rule.onNodeWithTag("target").performTouchInput { doubleClick() }
        rule.onNodeWithText("6a.1").assertExists()

        rule.onNodeWithText("Close").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("6a.1").assertDoesNotExist()
    }

    @Test
    fun `copy button keeps the dialog open`() {
        rule.setContent {
            MaterialTheme {
                ElementInspectorHost(enabled = true) { IdTarget() }
            }
        }
        rule.onNodeWithTag("target").performTouchInput { doubleClick() }

        rule.onNodeWithText("Copy").performClick()
        rule.waitForIdle()

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
