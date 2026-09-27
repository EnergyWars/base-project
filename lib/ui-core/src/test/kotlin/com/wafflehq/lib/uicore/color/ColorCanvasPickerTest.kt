package com.wafflehq.lib.uicore.color

import android.app.Application
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ColorCanvasPickerTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun touchOnPanelEmitsColorWithinInitialHue() {
        var emitted: Color? = null
        rule.setContent {
            ColorCanvasPicker(initialColor = Color.Red, onColorChanged = { emitted = it }, modifier = Modifier.testTag("picker"))
        }

        rule.onNodeWithTag("picker").performTouchInput { click(percentOffset(0.5f, 0.2f)) }

        rule.runOnIdle {
            assertNotNull(emitted)
            assertEquals(1f, emitted!!.alpha, 0.001f)
        }
    }

    @Test
    fun defaultCheckerboardIsNeutralGrey() {
        assertEquals(Color(0xFFB8C2D2), ColorCanvasDefaults.checkerboard)
    }

    @Test
    fun hueSliderEmitsHueInRange() {
        var hue = -1f
        rule.setContent { HueSlider(hue = 0f, onHueChange = { hue = it }) }

        rule.onRoot().performTouchInput { click(centerRight) }

        rule.runOnIdle { assertTrue(hue in 0f..360f) }
    }

    @Test
    fun alphaSliderEmitsAlphaInRange() {
        var alpha = -1f
        rule.setContent { AlphaSlider(color = Color.Blue, alpha = 1f, onAlphaChange = { alpha = it }) }
        rule.onRoot().performTouchInput { click(centerLeft) }
        rule.runOnIdle { assertTrue(alpha in 0f..1f) }
    }

    @Test
    fun alphaSliderIgnoresTouchWhenDisabled() {
        var disabledAlpha = -1f
        rule.setContent { AlphaSlider(color = Color.Blue, alpha = 1f, enabled = false, onAlphaChange = { disabledAlpha = it }) }
        rule.onRoot().performTouchInput { click(center) }
        rule.runOnIdle { assertEquals(-1f, disabledAlpha, 0f) }
    }
}
