package com.wafflehq.lib.settings.colors.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ColorRampPreviewStripTest {

    @get:Rule
    val rule = createComposeRule()

    private val testTag = "ramp_preview_strip_test_tag"

    @Test
    fun `strip is displayed for a saturated seed`() {
        rule.setContent {
            ColorRampPreviewStrip(seed = Color(0xFF0E3D6E), testTag = testTag)
        }

        rule.onNodeWithTag(testTag).assertIsDisplayed()
    }

    @Test
    fun `strip is displayed for a neutral gray seed without throwing`() {
        rule.setContent {
            ColorRampPreviewStrip(seed = Color(0xFF808080), testTag = testTag)
        }

        rule.onNodeWithTag(testTag).assertIsDisplayed()
    }
}
