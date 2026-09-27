package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class AppCardTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `renders content for every role and variant combination`() {
        rule.setContent {
            MaterialTheme {
                Column {
                    AppCardRole.entries.forEach { role ->
                        AppCardVariant.entries.forEach { variant ->
                            AppCard(role = role, variant = variant) { Text("$role-$variant") }
                        }
                    }
                }
            }
        }

        AppCardRole.entries.forEach { role ->
            AppCardVariant.entries.forEach { variant ->
                rule.onNodeWithText("$role-$variant").assertIsDisplayed()
            }
        }
    }

    @Test
    fun `card without onClick has no click action and does not crash`() {
        rule.setContent {
            MaterialTheme {
                AppCard(modifier = Modifier.testTag("plain")) { Text("Plain content") }
            }
        }

        rule.onNodeWithText("Plain content").assertIsDisplayed()
        rule.onNodeWithTag("plain").assertIsDisplayed()
    }

    @Test
    fun `clickable card invokes onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppCard(modifier = Modifier.testTag("clickable"), onClick = { clicks++ }) { Text("Tap me") }
            }
        }

        rule.onNodeWithTag("clickable").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `disabled clickable card does not invoke onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppCard(modifier = Modifier.testTag("disabled"), onClick = { clicks++ }, enabled = false) {
                    Text("Disabled content")
                }
            }
        }

        rule.onNodeWithTag("disabled").performClick()

        assertEquals(0, clicks)
    }

    @Test
    fun `color overload renders with an explicit container border and content color`() {
        rule.setContent {
            MaterialTheme {
                AppCard(
                    containerColor = Color.Magenta,
                    contentColor = Color.White,
                    borderColor = Color.Cyan,
                    borderWidth = 2.dp,
                    modifier = Modifier.testTag("custom"),
                ) { Text("Custom colors") }
            }
        }

        rule.onNodeWithTag("custom").assertIsDisplayed()
        rule.onNodeWithText("Custom colors").assertIsDisplayed()
    }

    @Test
    fun `color overload without a border color renders borderless`() {
        rule.setContent {
            MaterialTheme {
                AppCard(containerColor = Color.Magenta, modifier = Modifier.testTag("borderless")) {
                    Text("Borderless")
                }
            }
        }

        rule.onNodeWithTag("borderless").assertIsDisplayed()
    }

    @Test
    fun `color overload is clickable as well`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme {
                AppCard(
                    containerColor = Color.Magenta,
                    modifier = Modifier.testTag("custom-click"),
                    onClick = { clicks++ },
                ) { Text("Custom tap") }
            }
        }

        rule.onNodeWithTag("custom-click").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `defaults describe the hairline border of the outlined card`() {
        assertEquals(1, AppCardDefaults.borderWidth.value.toInt())
        assertEquals(0.4f, AppCardDefaults.BORDER_ALPHA, 0.0001f)
    }
}
