package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.wafflehq.lib.uicore.color.toHsl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ColorTokenResolverTest {

    private val token = ColorToken(
        id = ColorTokenId("test.token"),
        categoryKey = "GLOBAL",
        labelRes = 0,
        descriptionRes = 0,
        defaultLight = Color(0xFF112233),
        defaultDark = Color(0xFF445566)
    )

    private val rampLinkedToken = ColorToken(
        id = ColorTokenId("test.rampLinkedToken"),
        categoryKey = "GLOBAL",
        labelRes = 0,
        descriptionRes = 0,
        defaultLight = Sapphire40,
        defaultDark = Sapphire80
    )

    @Test
    fun `null override falls back to defaultLight when not dark`() {
        assertEquals(token.defaultLight, ColorTokenResolver.resolve(token, null, isDark = false))
    }

    @Test
    fun `null override falls back to defaultDark when dark`() {
        assertEquals(token.defaultDark, ColorTokenResolver.resolve(token, null, isDark = true))
    }

    @Test
    fun `light and dark defaults resolve to different colors when they differ`() {
        assertNotEquals(
            ColorTokenResolver.resolve(token, null, isDark = false),
            ColorTokenResolver.resolve(token, null, isDark = true)
        )
    }

    @Test
    fun `palette override resolves via ColorRampTable regardless of theme`() {
        val override = ColorValue.Palette(ColorRamp.GARNET, 40)
        val expected = ColorRampTable.swatch(ColorRamp.GARNET, 40)
        assertEquals(expected, ColorTokenResolver.resolve(token, override, isDark = false))
        assertEquals(expected, ColorTokenResolver.resolve(token, override, isDark = true))
    }

    @Test
    fun `custom override round-trips the exact argb including alpha`() {
        val argb = Color(0x80FF00AA).toArgb()
        val override = ColorValue.Custom(argb)
        val resolved = ColorTokenResolver.resolve(token, override, isDark = false)
        assertEquals(argb, resolved.toArgb())
    }

    @Test
    fun `custom override with zero alpha is preserved`() {
        val argb = Color(0x00112233).toArgb()
        val resolved = ColorTokenResolver.resolve(token, ColorValue.Custom(argb), isDark = true)
        assertEquals(0, (resolved.toArgb() ushr 24) and 0xFF)
    }

    @Test
    fun `custom override with full alpha is preserved`() {
        val argb = Color(0xFF112233).toArgb()
        val resolved = ColorTokenResolver.resolve(token, ColorValue.Custom(argb), isDark = false)
        assertEquals(0xFF, (resolved.toArgb() ushr 24) and 0xFF)
    }

    @Test
    fun `palette override is identical across all seven ramps for a fixed step`() {
        for (ramp in ColorRamp.entries) {
            val resolved = ColorTokenResolver.resolve(token, ColorValue.Palette(ramp, 60), isDark = false)
            assertEquals(ColorRampTable.swatch(ramp, 60), resolved)
        }
    }

    @Test
    fun `a default that is no ramp swatch still follows a base override of its nearest ramp`() {
        val overrides = mapOf(ColorRamp.SAPPHIRE to Color(0xFF992244))
        val resolved = ColorTokenResolver.resolve(token, null, isDark = false, overrides)
        assertNotEquals(token.defaultLight, resolved)
        assertEquals(token.defaultLight.alpha, resolved.alpha)
    }

    @Test
    fun `a default keeps its lightness while following a base override`() {
        val overrides = mapOf(ColorRamp.SAPPHIRE to Color(0xFF992244))
        val resolved = ColorTokenResolver.resolve(token, null, isDark = false, overrides)
        assertEquals(token.defaultLight.toHsl().l, resolved.toHsl().l, 2f)
    }

    @Test
    fun `a default that is no ramp swatch is untouched by a base override of a far ramp`() {
        val overrides = mapOf(ColorRamp.GARNET to Color(0xFF224466))
        assertEquals(token.defaultLight, ColorTokenResolver.resolve(token, null, isDark = false, overrides))
    }

    @Test
    fun `an explicit anchor ramp decides which base override a default follows`() {
        val anchored = token.copy(anchorRamp = ColorRamp.GARNET)
        val overrides = mapOf(ColorRamp.GARNET to Color(0xFF224466))
        assertNotEquals(token.defaultLight, ColorTokenResolver.resolve(anchored, null, isDark = false, overrides))
    }

    @Test
    fun `white and black defaults follow the graphite base override`() {
        val white = token.copy(defaultLight = Color.White, defaultDark = Color.Black)
        val overrides = mapOf(ColorRamp.GRAPHITE to Color(0xFF884400))
        assertNotEquals(Color.White, ColorTokenResolver.resolve(white, null, isDark = false, overrides))
        assertNotEquals(Color.Black, ColorTokenResolver.resolve(white, null, isDark = true, overrides))
    }

    @Test
    fun `a transparent default stays transparent`() {
        val transparent = token.copy(defaultLight = Color.Transparent)
        val overrides = mapOf(ColorRamp.GRAPHITE to Color(0xFF884400))
        assertEquals(Color.Transparent, ColorTokenResolver.resolve(transparent, null, isDark = false, overrides))
    }

    @Test
    fun `a translucent ramp swatch keeps its alpha while following a base override`() {
        val translucent = token.copy(defaultLight = Sapphire50.copy(alpha = 0.4f))
        val overrides = mapOf(ColorRamp.SAPPHIRE to Color(0xFF224466))
        val resolved = ColorTokenResolver.resolve(translucent, null, isDark = false, overrides)
        assertEquals(ColorRampTable.swatch(ColorRamp.SAPPHIRE, 50, overrides).copy(alpha = 0.4f), resolved)
    }

    @Test
    fun `resolution without any base override returns every default unchanged`() {
        val translucent = token.copy(defaultLight = Sapphire50.copy(alpha = 0.4f))
        assertEquals(translucent.defaultLight, ColorTokenResolver.resolve(translucent, null, isDark = false))
    }

    @Test
    fun `an explicit setting wins over a base override`() {
        val overrides = mapOf(ColorRamp.SAPPHIRE to Color(0xFF992244))
        val custom = ColorValue.Custom(0xFF00AA00.toInt())
        assertEquals(Color(0xFF00AA00), ColorTokenResolver.resolve(token, custom, isDark = false, overrides))
    }

    @Test
    fun `a default that equals a ramp swatch follows a base ramp override for that ramp`() {
        val seed = Color(0xFF224466)
        val overrides = mapOf(ColorRamp.SAPPHIRE to seed)
        val expected = ColorRampTable.swatch(ColorRamp.SAPPHIRE, 40, overrides)

        assertEquals(expected, ColorTokenResolver.resolve(rampLinkedToken, null, isDark = false, overrides))
        assertNotEquals(Sapphire40, ColorTokenResolver.resolve(rampLinkedToken, null, isDark = false, overrides))
    }

    @Test
    fun `light and dark defaults of the same ramp both follow a base ramp override`() {
        val overrides = mapOf(ColorRamp.SAPPHIRE to Color(0xFF224466))

        assertEquals(
            ColorRampTable.swatch(ColorRamp.SAPPHIRE, 40, overrides),
            ColorTokenResolver.resolve(rampLinkedToken, null, isDark = false, overrides)
        )
        assertEquals(
            ColorRampTable.swatch(ColorRamp.SAPPHIRE, 80, overrides),
            ColorTokenResolver.resolve(rampLinkedToken, null, isDark = true, overrides)
        )
    }

    @Test
    fun `a base ramp override for an unrelated ramp does not change this token's default`() {
        val overrides = mapOf(ColorRamp.GARNET to Color(0xFF224466))
        assertEquals(Sapphire40, ColorTokenResolver.resolve(rampLinkedToken, null, isDark = false, overrides))
    }

    @Test
    fun `a custom override is never affected by base ramp overrides - it is manually chosen`() {
        val argb = Color(0xFF00FF00).toArgb()
        val override = ColorValue.Custom(argb)
        val overrides = mapOf(ColorRamp.SAPPHIRE to Color(0xFF224466))

        val resolved = ColorTokenResolver.resolve(rampLinkedToken, override, isDark = false, overrides)

        assertEquals(argb, resolved.toArgb())
    }

    @Test
    fun `a palette override follows base ramp overrides just like a linked default`() {
        val overrides = mapOf(ColorRamp.GARNET to Color(0xFF224466))
        val override = ColorValue.Palette(ColorRamp.GARNET, 40)

        val resolved = ColorTokenResolver.resolve(token, override, isDark = false, overrides)

        assertEquals(ColorRampTable.swatch(ColorRamp.GARNET, 40, overrides), resolved)
    }
}
