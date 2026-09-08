package com.wafflehq.uikit.color

import androidx.compose.ui.graphics.Color
import com.wafflehq.uikit.theme.AppRole
import com.wafflehq.uikit.theme.WafflePalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class WafflePaletteStateTest {

    @Test
    fun `starts with the given initial palette`() {
        val state = WafflePaletteState(WafflePalette.Default)
        assertEquals(WafflePalette.Default, state.palette)
    }

    @Test
    fun `setRoleAccent regenerates only the targeted role`() {
        val state = WafflePaletteState(WafflePalette.Default)
        state.setRoleAccent(AppRole.Primary, Color(0xFF224466))
        assertNotEquals(WafflePalette.Default.primary, state.palette.primary)
        assertEquals(WafflePalette.Default.secondary, state.palette.secondary)
    }

    @Test
    fun `resetRole restores the default ramp for that role only`() {
        val state = WafflePaletteState(WafflePalette.Default)
        state.setRoleAccent(AppRole.Primary, Color(0xFF224466))
        state.resetRole(AppRole.Primary)
        assertEquals(WafflePalette.Default.primary, state.palette.primary)
    }

    @Test
    fun `resetAll restores the full default palette`() {
        val state = WafflePaletteState(WafflePalette.Default)
        state.setRoleAccent(AppRole.Primary, Color(0xFF224466))
        state.setRoleAccent(AppRole.Error, Color(0xFF00FF00))
        state.resetAll()
        assertEquals(WafflePalette.Default, state.palette)
    }

    @Test
    fun `replace swaps the whole palette`() {
        val state = WafflePaletteState(WafflePalette.Default)
        val other = WafflePalette.Default.withRole(AppRole.Neutral, rampFromAccent("Graphite", Color(0xFF808080)))
        state.replace(other)
        assertEquals(other, state.palette)
    }

    @Test
    fun `every mutation notifies the onPaletteChanged callback`() {
        var notified: WafflePalette? = null
        val state = WafflePaletteState(WafflePalette.Default, onPaletteChanged = { notified = it })
        state.setRoleAccent(AppRole.Primary, Color(0xFF224466))
        assertEquals(state.palette, notified)
    }

    @Test
    fun `syncFromExternal applies a palette without notifying onPaletteChanged`() {
        var notifications = 0
        val state = WafflePaletteState(WafflePalette.Default, onPaletteChanged = { notifications++ })
        val other = WafflePalette.Default.withRole(AppRole.Neutral, rampFromAccent("Graphite", Color(0xFF808080)))
        state.syncFromExternal(other)
        assertEquals(other, state.palette)
        assertEquals(0, notifications)
    }

    @Test
    fun `syncFromExternal is a no-op when the palette is unchanged`() {
        val state = WafflePaletteState(WafflePalette.Default)
        state.syncFromExternal(WafflePalette.Default)
        assertEquals(WafflePalette.Default, state.palette)
    }
}
