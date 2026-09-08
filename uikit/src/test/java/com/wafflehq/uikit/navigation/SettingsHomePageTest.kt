package com.wafflehq.uikit.navigation

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.uikit.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsHomePageTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun rendersAllEntriesInOrderAndInvokesClick() {
        var clicked = ""
        rule.setContent {
            AppTheme {
                SettingsHomePage(
                    title = "Settings",
                    entries = listOf(
                        SettingsHomeEntry(title = "Modules", subtitle = "All modules", highlighted = true) { clicked = "Modules" },
                        SettingsHomeEntry(title = "Display", subtitle = "Theme") { clicked = "Display" },
                        SettingsHomeEntry(title = "Colors") { clicked = "Colors" },
                    ),
                    onBack = {},
                    backDescription = "Back",
                )
            }
        }
        rule.onNodeWithText("Settings").assertIsDisplayed()
        rule.onNodeWithTag(SettingsHomePageTestTags.LIST).assertIsDisplayed()
        rule.onNodeWithText("All modules").assertIsDisplayed()
        rule.onNodeWithText("Theme").assertIsDisplayed()
        rule.onNodeWithTag(SettingsHomePageTestTags.entry("Colors")).performClick()
        assertEquals("Colors", clicked)
        rule.onNodeWithTag(SettingsHomePageTestTags.entry("Modules")).performClick()
        assertEquals("Modules", clicked)
    }

    @Test
    fun backButtonInvokesCallback() {
        var back = 0
        rule.setContent {
            AppTheme {
                SettingsHomePage(title = "Settings", entries = emptyList(), onBack = { back++ }, backDescription = "Back")
            }
        }
        rule.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, back)
    }

    @Test
    fun entryDefaultsAreNotHighlightedAndHaveNoSubtitle() {
        val entry = SettingsHomeEntry(title = "x") {}
        assertEquals(null, entry.subtitle)
        assertEquals(false, entry.highlighted)
    }
}
