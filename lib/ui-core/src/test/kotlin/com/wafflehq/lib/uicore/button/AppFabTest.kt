package com.wafflehq.lib.uicore.button

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
class AppFabTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `fab invokes onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppFab(icon = Icons.Filled.Add, contentDescription = "add", role = AppButtonRole.Primary, onClick = { clicks++ })
            }
        }
        rule.onNodeWithContentDescription("add").assertIsDisplayed().performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `extended fab renders text and invokes onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppExtendedFab(text = "Add event", icon = Icons.Filled.Add, role = AppButtonRole.Primary, onClick = { clicks++ })
            }
        }
        rule.onNodeWithText("Add event", useUnmergedTree = true).assertExists().performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `extended fab without icon still renders text and invokes onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppExtendedFab(
                    text = "Ok",
                    containerColor = androidx.compose.ui.graphics.Color.Magenta,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    onClick = { clicks++ },
                )
            }
        }
        rule.onNodeWithText("Ok", useUnmergedTree = true).assertExists().performClick()
        assertEquals(1, clicks)
    }
}
