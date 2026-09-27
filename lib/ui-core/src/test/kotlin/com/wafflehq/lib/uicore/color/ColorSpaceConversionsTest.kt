package com.wafflehq.lib.uicore.color

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ColorSpaceConversionsTest {

    private fun assertRgbClose(expected: Rgb, actual: Rgb, tolerance: Int = 1) {
        assertTrue("r expected ${expected.r} got ${actual.r}", abs(expected.r - actual.r) <= tolerance)
        assertTrue("g expected ${expected.g} got ${actual.g}", abs(expected.g - actual.g) <= tolerance)
        assertTrue("b expected ${expected.b} got ${actual.b}", abs(expected.b - actual.b) <= tolerance)
    }

    @Test
    fun `black round-trips through hsl`() {
        val hsl = rgbToHsl(0, 0, 0)
        assertEquals(0f, hsl.s)
        assertEquals(0f, hsl.l)
        assertRgbClose(Rgb(0, 0, 0), hslToRgb(hsl.h, hsl.s, hsl.l))
    }

    @Test
    fun `white round-trips through hsl`() {
        val hsl = rgbToHsl(255, 255, 255)
        assertEquals(0f, hsl.s)
        assertEquals(100f, hsl.l)
        assertRgbClose(Rgb(255, 255, 255), hslToRgb(hsl.h, hsl.s, hsl.l))
    }

    @Test
    fun `pure red converts to hue zero fully saturated`() {
        val hsl = rgbToHsl(255, 0, 0)
        assertEquals(0f, hsl.h, 0.01f)
        assertEquals(100f, hsl.s, 0.01f)
        assertEquals(50f, hsl.l, 0.01f)
    }

    @Test
    fun `pure green converts to hue 120`() {
        val hsl = rgbToHsl(0, 255, 0)
        assertEquals(120f, hsl.h, 0.01f)
    }

    @Test
    fun `pure blue converts to hue 240`() {
        val hsl = rgbToHsl(0, 0, 255)
        assertEquals(240f, hsl.h, 0.01f)
    }

    @Test
    fun `arbitrary colors round-trip rgb to hsl to rgb within rounding tolerance`() {
        val samples = listOf(
            Rgb(18, 52, 86), Rgb(255, 128, 0), Rgb(10, 200, 150),
            Rgb(1, 1, 1), Rgb(254, 254, 253), Rgb(120, 60, 200)
        )
        for (rgb in samples) {
            val hsl = rgbToHsl(rgb.r, rgb.g, rgb.b)
            assertRgbClose(rgb, hslToRgb(hsl.h, hsl.s, hsl.l))
        }
    }

    @Test
    fun `Color toHsl and Hsl toColor round-trip preserves rgb`() {
        val color = Color(red = 0.2f, green = 0.6f, blue = 0.8f, alpha = 1f)
        val hsl = color.toHsl()
        val restored = hsl.toColor()
        assertRgbClose(color.toRgb(), restored.toRgb())
    }

    @Test
    fun `toHexArgb formats with full alpha`() {
        val color = Color(0xFF112233)
        assertEquals("#FF112233", color.toHexArgb())
    }

    @Test
    fun `toHexArgb formats with partial alpha`() {
        val color = Color(0x80112233)
        assertEquals("#80112233", color.toHexArgb())
    }

    @Test
    fun `hexToColorOrNull parses 8-digit hex with alpha`() {
        val color = hexToColorOrNull("#80112233")
        assertEquals(0x80112233.toInt(), color?.toArgb())
    }

    @Test
    fun `hexToColorOrNull parses 6-digit hex defaulting to full alpha`() {
        val color = hexToColorOrNull("112233")
        assertEquals(0xFF112233.toInt(), color?.toArgb())
    }

    @Test
    fun `hexToColorOrNull returns null for invalid length`() {
        assertNull(hexToColorOrNull("#123"))
        assertNull(hexToColorOrNull("#1234567890"))
    }

    @Test
    fun `hexToColorOrNull returns null for non hex characters`() {
        assertNull(hexToColorOrNull("#GGHHII"))
    }

    @Test
    fun `hexToColorOrNull returns null for a leading minus sign`() {
        assertNull(hexToColorOrNull("-1a2b3"))
    }

    @Test
    fun `hex round-trip preserves argb`() {
        val original = Color(0x33ABCDEF)
        val restored = hexToColorOrNull(original.toHexArgb())
        assertEquals(original.toArgb(), restored?.toArgb())
    }
}
