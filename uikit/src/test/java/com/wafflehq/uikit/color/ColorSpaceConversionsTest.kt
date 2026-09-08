package com.wafflehq.uikit.color

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorSpaceConversionsTest {

    @Test
    fun `toRgb extracts channels from a color`() {
        val rgb = Color(red = 1f, green = 0f, blue = 0.5f).toRgb()
        assertEquals(255, rgb.r)
        assertEquals(0, rgb.g)
        assertTrue(rgb.b == 127 || rgb.b == 128)
    }

    @Test
    fun `rgb toColor round trips channel values`() {
        val color = Rgb(255, 0, 128).toColor()
        assertEquals(1f, color.red, 0.01f)
        assertEquals(0f, color.green, 0.01f)
        assertEquals(1f, color.alpha, 0.01f)
    }

    @Test
    fun `rgbToHsl reports zero saturation for gray`() {
        val hsl = rgbToHsl(128, 128, 128)
        assertEquals(0f, hsl.s, 0.01f)
    }

    @Test
    fun `rgbToHsl and hslToRgb round trip red`() {
        val hsl = rgbToHsl(255, 0, 0)
        val rgb = hslToRgb(hsl.h, hsl.s, hsl.l)
        assertEquals(255, rgb.r)
        assertEquals(0, rgb.g)
        assertEquals(0, rgb.b)
    }

    @Test
    fun `rgbToHsl and hslToRgb round trip green and blue dominant colors`() {
        val green = rgbToHsl(0, 255, 0)
        val greenRgb = hslToRgb(green.h, green.s, green.l)
        assertEquals(0, greenRgb.r)
        assertEquals(255, greenRgb.g)

        val blue = rgbToHsl(0, 0, 255)
        val blueRgb = hslToRgb(blue.h, blue.s, blue.l)
        assertEquals(255, blueRgb.b)
    }

    @Test
    fun `hslToRgb with zero saturation returns gray`() {
        val rgb = hslToRgb(0f, 0f, 50f)
        assertEquals(rgb.r, rgb.g)
        assertEquals(rgb.g, rgb.b)
    }

    @Test
    fun `hslToRgb handles hue wraparound`() {
        val rgb = hslToRgb(-30f, 100f, 50f)
        assertEquals(hslToRgb(330f, 100f, 50f), rgb)
    }

    @Test
    fun `color toHsl and back preserves hue family`() {
        val original = Color(0xFF3D7480)
        val roundTripped = original.toHsl().toColor()
        assertEquals(original.toRgb(), roundTripped.toRgb())
    }

    @Test
    fun `toHexArgb formats with alpha channel`() {
        val hex = Color(0xFF123456).toHexArgb()
        assertEquals("#FF123456", hex)
    }

    @Test
    fun `hexToColorOrNull parses six digit hex`() {
        val color = hexToColorOrNull("#123456")
        assertEquals(Color(0xFF123456), color)
    }

    @Test
    fun `hexToColorOrNull parses eight digit hex`() {
        val color = hexToColorOrNull("80123456")
        assertEquals(Color(0x80123456), color)
    }

    @Test
    fun `hexToColorOrNull rejects invalid characters`() {
        assertNull(hexToColorOrNull("#12345Z"))
    }

    @Test
    fun `hexToColorOrNull rejects wrong length`() {
        assertNull(hexToColorOrNull("#1234"))
    }
}
