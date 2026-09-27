package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.wafflehq.lib.uicore.color.toHsl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorRampTableTest {

    @Test
    fun `swatch maps every ramp and step to the matching Color kt constant`() {
        assertEquals(Sapphire10, ColorRampTable.swatch(ColorRamp.SAPPHIRE, 10))
        assertEquals(Sapphire40, ColorRampTable.swatch(ColorRamp.SAPPHIRE, 40))
        assertEquals(Sapphire90, ColorRampTable.swatch(ColorRamp.SAPPHIRE, 90))
        assertEquals(Aquamarine40, ColorRampTable.swatch(ColorRamp.AQUAMARINE, 40))
        assertEquals(Amethyst10, ColorRampTable.swatch(ColorRamp.AMETHYST, 10))
        assertEquals(Amethyst90, ColorRampTable.swatch(ColorRamp.AMETHYST, 90))
        assertEquals(Emerald80, ColorRampTable.swatch(ColorRamp.EMERALD, 80))
        assertEquals(Citrine60, ColorRampTable.swatch(ColorRamp.CITRINE, 60))
        assertEquals(Garnet10, ColorRampTable.swatch(ColorRamp.GARNET, 10))
        assertEquals(Garnet90, ColorRampTable.swatch(ColorRamp.GARNET, 90))
        assertEquals(Graphite50, ColorRampTable.swatch(ColorRamp.GRAPHITE, 50))
    }

    @Test
    fun `allSwatches returns exactly 7 ramps times 9 steps`() {
        val swatches = ColorRampTable.allSwatches()
        assertEquals(63, swatches.size)
        assertEquals(7, swatches.map { it.first }.distinct().size)
        assertEquals(9, swatches.map { it.second }.distinct().size)
    }

    @Test
    fun `allSwatches has no duplicate ramp-step pairs`() {
        val swatches = ColorRampTable.allSwatches()
        val pairs = swatches.map { it.first to it.second }
        assertEquals(pairs.size, pairs.distinct().size)
    }

    @Test
    fun `every ramp exposes every step without throwing`() {
        for (ramp in ColorRamp.entries) {
            for (step in ColorRampSteps) {
                ColorRampTable.swatch(ramp, step)
            }
        }
    }

    @Test
    fun `swatch without a base override for that ramp returns the original constant`() {
        val overrides = mapOf(ColorRamp.GARNET to Color(0xFF112233))
        assertEquals(Sapphire40, ColorRampTable.swatch(ColorRamp.SAPPHIRE, 40, overrides))
    }

    @Test
    fun `swatch with a base override for that ramp ignores the original constant`() {
        val seed = Color(0xFF224466)
        val overrides = mapOf(ColorRamp.SAPPHIRE to seed)
        assertNotEquals(Sapphire40, ColorRampTable.swatch(ColorRamp.SAPPHIRE, 40, overrides))
    }

    @Test
    fun `swatch with a base override keeps the seed's hue and saturation across every step`() {
        val seed = Color(0xFF224466)
        val seedHsl = seed.toHsl()
        val overrides = mapOf(ColorRamp.SAPPHIRE to seed)
        for (step in ColorRampSteps) {
            val hsl = ColorRampTable.swatch(ColorRamp.SAPPHIRE, step, overrides).toHsl()
            assertEquals(seedHsl.h, hsl.h, 2f)
            assertEquals(seedHsl.s, hsl.s, 2f)
        }
    }

    @Test
    fun `swatch with a base override produces monotonically increasing lightness by step`() {
        val overrides = mapOf(ColorRamp.SAPPHIRE to Color(0xFF224466))
        val lightness = ColorRampSteps.map { step -> ColorRampTable.swatch(ColorRamp.SAPPHIRE, step, overrides).toHsl().l }
        assertEquals(lightness, lightness.sorted())
    }

    @Test
    fun `allSwatches with a base override reflects it for every step of that ramp only`() {
        val seed = Color(0xFF224466)
        val overrides = mapOf(ColorRamp.SAPPHIRE to seed)
        val swatches = ColorRampTable.allSwatches(overrides).associate { (ramp, step, color) -> (ramp to step) to color }

        for (step in ColorRampSteps) {
            assertEquals(ColorRampTable.swatch(ColorRamp.SAPPHIRE, step, overrides), swatches.getValue(ColorRamp.SAPPHIRE to step))
        }
        assertEquals(Garnet10, swatches.getValue(ColorRamp.GARNET to 10))
    }

    @Test
    fun `baseSeed returns the original step-40 tone for every ramp`() {
        assertEquals(Sapphire40, ColorRampTable.baseSeed(ColorRamp.SAPPHIRE))
        for (ramp in ColorRamp.entries) {
            assertEquals(ColorRampTable.swatch(ramp, ColorRampTable.BASE_STEP), ColorRampTable.baseSeed(ramp))
        }
    }

    @Test
    fun `rampReferenceOf finds the exact ramp and step for an original swatch`() {
        assertEquals(ColorRamp.SAPPHIRE to 10, ColorRampTable.rampReferenceOf(Sapphire10))
        assertEquals(ColorRamp.SAPPHIRE to 90, ColorRampTable.rampReferenceOf(Sapphire90))
        assertEquals(ColorRamp.GARNET to 90, ColorRampTable.rampReferenceOf(Garnet90))
    }

    @Test
    fun `rampReferenceOf returns null for a color that matches no ramp swatch`() {
        assertNull(ColorRampTable.rampReferenceOf(Color(0xFF123456)))
    }

    @Test
    fun `generatedRamp produces exactly the nine ramp steps`() {
        val ramp = ColorRampTable.generatedRamp(Color(0xFF224466))
        assertEquals(ColorRampSteps.toSet(), ramp.keys)
    }

    @Test
    fun `generatedRamp is deterministic for the same seed`() {
        val seed = Color(0xFF224466)
        assertEquals(ColorRampTable.generatedRamp(seed), ColorRampTable.generatedRamp(seed))
    }

    @Test
    fun `generatedRamp for a neutral gray seed stays desaturated at every step`() {
        val ramp = ColorRampTable.generatedRamp(Color(0xFF808080))
        for (step in ColorRampSteps) {
            assertTrue(ramp.getValue(step).toHsl().s < 1f)
        }
    }

    @Test
    fun `follow returns the color unchanged without base overrides`() {
        val color = Color(0xFF123456)
        assertEquals(color, ColorRampTable.follow(color, emptyMap()))
    }

    @Test
    fun `follow moves an exact swatch to the same step of the regenerated ramp`() {
        val overrides = mapOf(ColorRamp.EMERALD to Color(0xFF884400))
        assertEquals(
            ColorRampTable.swatch(ColorRamp.EMERALD, 70, overrides),
            ColorRampTable.follow(Emerald70, overrides)
        )
    }

    @Test
    fun `follow adopts the hue of the overridden ramp for an arbitrary chromatic color`() {
        val overrides = mapOf(ColorRamp.SAPPHIRE to Color(0xFFAA2222))
        val followed = ColorRampTable.follow(Color(0xFF162336), overrides)
        assertTrue(followed.toHsl().h < 30f || followed.toHsl().h > 330f)
    }

    @Test
    fun `follow ignores an override of a ramp the color does not belong to`() {
        val color = Color(0xFF162336)
        assertEquals(color, ColorRampTable.follow(color, mapOf(ColorRamp.CITRINE to Color(0xFF884400))))
    }

    @Test
    fun `follow keeps alpha of a translucent color`() {
        val overrides = mapOf(ColorRamp.SAPPHIRE to Color(0xFFAA2222))
        assertEquals(0x92, (ColorRampTable.follow(Color(0x92162336), overrides).toArgb() ushr 24) and 0xFF)
    }

    @Test
    fun `follow tints pure white through the graphite override`() {
        val followed = ColorRampTable.follow(Color.White, mapOf(ColorRamp.GRAPHITE to Color(0xFF884400)))
        assertTrue(followed != Color.White)
        assertTrue(followed.toHsl().l >= 97f)
    }
}
