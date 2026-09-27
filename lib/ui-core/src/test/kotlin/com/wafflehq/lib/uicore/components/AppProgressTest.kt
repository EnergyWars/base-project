package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertRangeInfoEquals
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
class AppProgressTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `linear progress exposes its fraction`() {
        rule.setContent {
            MaterialTheme { AppLinearProgress(progress = { 0.5f }, modifier = Modifier.fillMaxWidth().testTag("linear")) }
        }

        rule.onNodeWithTag("linear").assertRangeInfoEquals(ProgressBarRangeInfo(0.5f, 0f..1f, 0))
    }

    @Test
    fun `flat linear progress renders with explicit colors`() {
        rule.setContent {
            MaterialTheme {
                AppLinearProgress(
                    progress = { 0.25f },
                    modifier = Modifier.fillMaxWidth().height(3.dp).testTag("flat"),
                    color = Color.Magenta,
                    trackColor = Color.Transparent,
                    flat = true,
                )
            }
        }

        rule.onNodeWithTag("flat").assertRangeInfoEquals(ProgressBarRangeInfo(0.25f, 0f..1f, 0))
    }

    @Test
    fun `circular progress renders indeterminate and determinate`() {
        rule.setContent {
            MaterialTheme {
                Column {
                    AppCircularProgress(modifier = Modifier.testTag("spinner"), strokeWidth = 2.dp)
                    AppCircularProgress(progress = { 0.75f }, modifier = Modifier.testTag("ring"), color = Color.Magenta)
                }
            }
        }

        rule.onNodeWithTag("spinner").assertIsDisplayed()
        rule.onNodeWithTag("ring").assertRangeInfoEquals(ProgressBarRangeInfo(0.75f, 0f..1f, 0))
    }

    @Test
    fun `default stroke width is the material default`() {
        assertEquals(4f, AppProgressDefaults.circularStrokeWidth.value, 0.0001f)
    }
}
