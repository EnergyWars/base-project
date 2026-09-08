package com.wafflehq.uikit.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PaletteTest {

    private val nineTones = listOf(
        "10" to Color.Black, "20" to Color.Black, "30" to Color.Black, "40" to Color.Black, "50" to Color.Black,
        "60" to Color.Black, "70" to Color.White, "80" to Color.Black, "90" to Color.Black,
    )

    @Test
    fun `ramp get returns color for known tone`() {
        val ramp = ColorRamp("Test", nineTones)
        assertEquals(Color.Black, ramp["10"])
        assertEquals(Color.White, ramp["70"])
    }

    @Test
    fun `ramp get throws for unknown tone`() {
        val ramp = ColorRamp("Test", nineTones)
        assertThrows(IllegalStateException::class.java) { ramp["99"] }
    }

    @Test
    fun `ramp requires exactly nine tones`() {
        assertThrows(IllegalArgumentException::class.java) {
            ColorRamp("Broken", listOf("10" to Color.Black))
        }
    }

    @Test
    fun `default palette has nine tones per ramp`() {
        WafflePalette.Default.ramps.forEach { ramp ->
            assertEquals(9, ramp.tones.size)
        }
    }

    @Test
    fun `ramps lists all seven roles in role order`() {
        val palette = WafflePalette.Default
        assertEquals(
            listOf(palette.primary, palette.secondary, palette.tertiary, palette.success, palette.warning, palette.error, palette.neutral),
            palette.ramps,
        )
    }

    @Test
    fun `forRole returns matching ramp for every role`() {
        val palette = WafflePalette.Default
        assertEquals(palette.primary, palette.forRole(AppRole.Primary))
        assertEquals(palette.secondary, palette.forRole(AppRole.Secondary))
        assertEquals(palette.tertiary, palette.forRole(AppRole.Tertiary))
        assertEquals(palette.success, palette.forRole(AppRole.Success))
        assertEquals(palette.warning, palette.forRole(AppRole.Warning))
        assertEquals(palette.error, palette.forRole(AppRole.Error))
        assertEquals(palette.neutral, palette.forRole(AppRole.Neutral))
    }

    @Test
    fun `withRole replaces only the targeted role for every role`() {
        val palette = WafflePalette.Default
        val replacement = ColorRamp(
            "Custom",
            listOf("10" to Color.Black, "20" to Color.Black, "30" to Color.Black, "40" to Color.Black, "50" to Color.Black, "60" to Color.Black, "70" to Color.Black, "80" to Color.Black, "90" to Color.Black),
        )

        AppRole.entries.forEach { role ->
            val updated = palette.withRole(role, replacement)
            assertEquals(replacement, updated.forRole(role))
            AppRole.entries.filter { it != role }.forEach { other ->
                assertEquals(palette.forRole(other), updated.forRole(other))
            }
        }
    }
}
