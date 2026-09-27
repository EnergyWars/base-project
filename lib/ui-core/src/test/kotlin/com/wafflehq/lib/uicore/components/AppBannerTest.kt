package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
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
class AppBannerTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `renders text for every role and variant combination`() {
        rule.setContent {
            MaterialTheme {
                Column {
                    AppBannerRole.entries.forEach { role ->
                        AppBannerVariant.entries.forEach { variant ->
                            AppBanner(text = "$role-$variant", role = role, variant = variant)
                        }
                    }
                }
            }
        }

        AppBannerRole.entries.forEach { role ->
            AppBannerVariant.entries.forEach { variant ->
                rule.onNodeWithText("$role-$variant").assertIsDisplayed()
            }
        }
    }

    @Test
    fun `title and text are both displayed`() {
        rule.setContent {
            MaterialTheme {
                AppBanner(text = "Body", title = "Heading", icon = Icons.Default.Info, variant = AppBannerVariant.Card)
            }
        }

        rule.onNodeWithText("Heading").assertIsDisplayed()
        rule.onNodeWithText("Body").assertIsDisplayed()
    }

    @Test
    fun `banner without onClick has no click action`() {
        rule.setContent {
            MaterialTheme { AppBanner(text = "Plain", modifier = Modifier.testTag("banner")) }
        }

        rule.onNodeWithTag("banner").assertIsDisplayed().assertHasNoClickAction()
    }

    @Test
    fun `tapping the banner invokes onClick`() {
        var clicks = 0
        rule.setContent {
            MaterialTheme { AppBanner(text = "Tap me", onClick = { clicks++ }) }
        }

        rule.onNodeWithText("Tap me").assertHasClickAction()
        rule.onNodeWithText("Tap me").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `color overload renders with explicit colors and border`() {
        rule.setContent {
            MaterialTheme {
                AppBanner(
                    text = "Custom",
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    borderColor = Color.Red,
                    variant = AppBannerVariant.Card,
                )
            }
        }

        rule.onNodeWithText("Custom").assertIsDisplayed()
    }

    @Test
    fun `action slot is displayed and clickable independently of the banner`() {
        var bannerClicks = 0
        var actionClicks = 0
        rule.setContent {
            MaterialTheme {
                AppBanner(
                    text = "With action",
                    onClick = { bannerClicks++ },
                    action = {
                        Text("Do it", modifier = Modifier.testTag("action").clickable { actionClicks++ })
                    },
                )
            }
        }

        rule.onNodeWithText("With action").assertIsDisplayed()
        rule.onNodeWithTag("action").performClick()

        assertEquals(1, actionClicks)
        assertEquals(0, bannerClicks)
    }

    @Test
    fun `footer and text modifier are applied`() {
        rule.setContent {
            MaterialTheme {
                AppBanner(
                    text = "Countdown 5",
                    title = "Title",
                    textModifier = Modifier.testTag("text"),
                    footer = {
                        Row { Text("Footer", modifier = Modifier.testTag("footer")) }
                    },
                )
            }
        }

        rule.onNodeWithTag("text").assertTextEquals("Countdown 5")
        rule.onNodeWithTag("footer").assertIsDisplayed()
    }

    @Test
    fun `custom content padding is accepted`() {
        rule.setContent {
            MaterialTheme {
                AppBanner(text = "Padded", contentPadding = PaddingValues(start = 16.dp, end = 4.dp))
            }
        }

        rule.onNodeWithText("Padded").assertIsDisplayed()
    }
}
