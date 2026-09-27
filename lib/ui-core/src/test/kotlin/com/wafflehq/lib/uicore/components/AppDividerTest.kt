package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
class AppDividerTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `renders horizontal and vertical dividers in every tone`() {
        rule.setContent {
            MaterialTheme {
                Column {
                    AppDividerTone.entries.forEach { tone ->
                        AppHorizontalDivider(tone = tone, modifier = Modifier.testTag("h-$tone"))
                        Row(Modifier.height(20.dp)) { AppVerticalDivider(tone = tone, modifier = Modifier.testTag("v-$tone")) }
                    }
                }
            }
        }

        AppDividerTone.entries.forEach { tone ->
            rule.onNodeWithTag("h-$tone").assertIsDisplayed()
            rule.onNodeWithTag("v-$tone").assertIsDisplayed()
        }
    }

    @Test
    fun `color overloads render with an explicit color and thickness`() {
        rule.setContent {
            MaterialTheme {
                Column {
                    AppHorizontalDivider(color = Color.Magenta, thickness = 2.dp, modifier = Modifier.testTag("h"))
                    Row(Modifier.height(20.dp)) { AppVerticalDivider(color = Color.Cyan, thickness = 2.dp, modifier = Modifier.testTag("v")) }
                }
            }
        }

        rule.onNodeWithTag("h").assertIsDisplayed()
        rule.onNodeWithTag("v").assertIsDisplayed()
    }

    @Test
    fun `tone colors come from the theme`() {
        var standard: Color = Color.Unspecified
        var subtle: Color = Color.Unspecified
        var outlineVariant: Color = Color.Unspecified
        var outline: Color = Color.Unspecified
        rule.setContent {
            MaterialTheme {
                standard = AppDividerDefaults.color(AppDividerTone.Standard)
                subtle = AppDividerDefaults.color(AppDividerTone.Subtle)
                outlineVariant = MaterialTheme.colorScheme.outlineVariant
                outline = MaterialTheme.colorScheme.outline
            }
        }

        assertEquals(outlineVariant, standard)
        assertEquals(outline.copy(alpha = AppDividerDefaults.SUBTLE_ALPHA), subtle)
    }

    @Test
    fun `subtle alpha matches the card border and thickness is one dp`() {
        assertEquals(AppCardDefaults.BORDER_ALPHA, AppDividerDefaults.SUBTLE_ALPHA, 0.0001f)
        assertEquals(1f, AppDividerDefaults.thickness.value, 0.0001f)
    }
}
