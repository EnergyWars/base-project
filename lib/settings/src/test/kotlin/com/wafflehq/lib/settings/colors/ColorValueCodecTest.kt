package com.wafflehq.lib.settings.colors

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ColorValueCodecTest {

    @Test
    fun `palette value round-trips through encode and decode for every ramp`() {
        for (ramp in ColorRamp.entries) {
            val value = ColorValue.Palette(ramp, 40)
            assertEquals(value, ColorValueCodec.decode(ColorValueCodec.encode(value)))
        }
    }

    @Test
    fun `palette value round-trips for every step`() {
        for (step in listOf(10, 20, 30, 40, 50, 60, 70, 80, 90)) {
            val value = ColorValue.Palette(ColorRamp.GARNET, step)
            assertEquals(value, ColorValueCodec.decode(ColorValueCodec.encode(value)))
        }
    }

    @Test
    fun `custom value round-trips including full alpha`() {
        val value = ColorValue.Custom(0xFF112233.toInt())
        assertEquals(value, ColorValueCodec.decode(ColorValueCodec.encode(value)))
    }

    @Test
    fun `custom value round-trips including zero alpha`() {
        val value = ColorValue.Custom(0x00112233)
        assertEquals(value, ColorValueCodec.decode(ColorValueCodec.encode(value)))
    }

    @Test
    fun `custom value round-trips including mid alpha`() {
        val value = ColorValue.Custom(0x80AABBCC.toInt())
        assertEquals(value, ColorValueCodec.decode(ColorValueCodec.encode(value)))
    }

    @Test
    fun `decode returns null for empty string`() {
        assertNull(ColorValueCodec.decode(""))
    }

    @Test
    fun `decode returns null for garbage string`() {
        assertNull(ColorValueCodec.decode("not-a-valid-value"))
    }

    @Test
    fun `decode returns null for unknown ramp name`() {
        assertNull(ColorValueCodec.decode("P|UNKNOWN_RAMP|40"))
    }

    @Test
    fun `decode returns null for invalid step`() {
        assertNull(ColorValueCodec.decode("P|GARNET|45"))
    }

    @Test
    fun `decode returns null for truncated palette value`() {
        assertNull(ColorValueCodec.decode("P|GARNET"))
    }

    @Test
    fun `decode returns null for truncated custom value`() {
        assertNull(ColorValueCodec.decode("C"))
    }

    @Test
    fun `decode returns null for non numeric custom argb`() {
        assertNull(ColorValueCodec.decode("C|not-a-number"))
    }

    @Test
    fun `decode never throws for a batch of garbage inputs`() {
        val garbageInputs = listOf(
            "", " ", "|||", "X|Y|Z", "P||", "C||", "P|SAPPHIRE|", "P|SAPPHIRE|-10",
            "P|SAPPHIRE|100", "randomtext123", "C|999999999999999999999"
        )
        for (input in garbageInputs) {
            assertNull("expected null for input '$input'", ColorValueCodec.decode(input))
        }
    }
}
