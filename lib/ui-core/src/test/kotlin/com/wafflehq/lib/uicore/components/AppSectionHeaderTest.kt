package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppSectionHeaderTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `renders text for every style`() {
        rule.setContent {
            MaterialTheme {
                Column {
                    AppSectionHeaderStyle.entries.forEach { style ->
                        AppSectionHeader(text = "header-$style", style = style, subtitle = "sub-$style")
                    }
                }
            }
        }

        AppSectionHeaderStyle.entries.forEach { rule.onNodeWithText("header-$it").assertIsDisplayed() }
        rule.onNodeWithText("sub-Title").assertIsDisplayed()
    }

    @Test
    fun `date variant renders the full localized date`() {
        val date = LocalDate.of(2026, 9, 25)
        val expected = AppSectionHeaderDefaults.formatDate(date, Locale.getDefault())
        rule.setContent { MaterialTheme { AppSectionHeader(date = date) } }

        rule.onNodeWithText(expected).assertIsDisplayed()
    }

    @Test
    fun `formatDate uses the given locale`() {
        val date = LocalDate.of(2026, 9, 25)

        assertEquals("Friday, September 25, 2026", AppSectionHeaderDefaults.formatDate(date, Locale.US))
        assertTrue(AppSectionHeaderDefaults.formatDate(date, Locale.GERMANY).contains("September"))
    }

    @Test
    fun `pill and leading content render together`() {
        rule.setContent {
            MaterialTheme {
                AppSectionHeader(
                    text = "Night",
                    pillColor = androidx.compose.ui.graphics.Color.Gray,
                    leadingContent = { Text("moon") },
                )
            }
        }

        rule.onNodeWithText("moon").assertIsDisplayed()
        rule.onNodeWithText("Night").assertIsDisplayed()
    }
}
