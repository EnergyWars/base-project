package com.wafflehq.uikit.color

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RampGeneratorTest {

    @Test
    fun `rampFromAccent produces nine tones with the given name`() {
        val ramp = rampFromAccent("Custom", Color(0xFF3D7480))
        assertEquals("Custom", ramp.name)
        assertEquals(9, ramp.tones.size)
        assertEquals(listOf("10", "20", "30", "40", "50", "60", "70", "80", "90"), ramp.tones.map { it.first })
    }

    @Test
    fun `rampFromAccent tones get darker towards low labels and lighter towards high labels`() {
        val ramp = rampFromAccent("Custom", Color(0xFF3D7480))
        val lightness = ramp.tones.map { (_, color) -> color.toHsl().l }
        for (i in 0 until lightness.size - 1) {
            assertTrue("tone ${i * 10 + 10} should be darker than the next", lightness[i] < lightness[i + 1])
        }
    }

    @Test
    fun `rampFromAccent preserves hue and saturation at mid lightness`() {
        val accent = Color(0xFFE19C31)
        val ramp = rampFromAccent("Custom", accent)
        val accentHsl = accent.toHsl()
        val midTone = ramp["50"].toHsl()
        assertEquals(accentHsl.h, midTone.h, 4f)
        assertEquals(accentHsl.s, midTone.s, 4f)
    }
}
