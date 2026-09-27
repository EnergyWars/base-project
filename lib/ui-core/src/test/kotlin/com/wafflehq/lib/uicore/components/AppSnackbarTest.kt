package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class AppSnackbarTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `shows the message posted to the host state`() {
        rule.setContent {
            MaterialTheme {
                val state = remember { SnackbarHostState() }
                LaunchedEffect(Unit) { state.showSnackbar("Saved") }
                AppSnackbarHost(hostState = state)
            }
        }

        rule.onNodeWithText("Saved").assertIsDisplayed()
    }

    @Test
    fun `shows the action label and reports the action click`() {
        var result: androidx.compose.material3.SnackbarResult? = null
        rule.setContent {
            MaterialTheme {
                val state = remember { SnackbarHostState() }
                LaunchedEffect(Unit) { result = state.showSnackbar("Deleted", actionLabel = "Undo") }
                AppSnackbarHost(hostState = state)
            }
        }

        rule.onNodeWithText("Undo").performClick()
        rule.waitForIdle()

        assertEquals(androidx.compose.material3.SnackbarResult.ActionPerformed, result)
    }
}
