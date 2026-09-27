package com.wafflehq.lib.settings.colors.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.settings.colors.ColorRamp
import com.wafflehq.lib.settings.colors.ColorRampTable
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
class PaletteSwatchGridTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        baseRampOverrides: Map<ColorRamp, Color> = emptyMap(),
        onSwatchSelected: (ColorRamp, Int) -> Unit
    ) {
        rule.setContent {
            Box(modifier = Modifier.size(360.dp)) {
                PaletteSwatchGrid(
                    onSwatchSelected = onSwatchSelected,
                    baseRampOverrides = baseRampOverrides
                )
            }
        }
    }

    @Test
    fun `tapping a swatch reports exactly its ramp and step`() {
        var picked: Pair<ColorRamp, Int>? = null
        setContent { ramp, step -> picked = ramp to step }

        rule.onNodeWithTag(PaletteSwatchGridTestTag.swatch(ColorRamp.SAPPHIRE, ColorRampTable.BASE_STEP))
            .performClick()

        assertEquals(ColorRamp.SAPPHIRE to ColorRampTable.BASE_STEP, picked)
    }

    @Test
    fun `a base ramp override keeps the reported ramp and step identical`() {
        var picked: Pair<ColorRamp, Int>? = null
        setContent(baseRampOverrides = mapOf(ColorRamp.SAPPHIRE to Color(0xFF224466))) { ramp, step ->
            picked = ramp to step
        }

        rule.onNodeWithTag(PaletteSwatchGridTestTag.swatch(ColorRamp.SAPPHIRE, 20)).performClick()

        assertEquals(ColorRamp.SAPPHIRE to 20, picked)
    }
}
