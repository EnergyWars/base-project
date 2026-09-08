package com.wafflehq.uikit.color

import androidx.compose.ui.graphics.Color
import com.wafflehq.uikit.theme.AppRole
import com.wafflehq.uikit.theme.WafflePalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PaletteJsonTest {

    @Test
    fun `round trip through json preserves every ramp`() {
        val palette = WafflePalette.Default
        val restored = wafflePaletteFromJson(palette.toJson())
        AppRole.entries.forEach { role ->
            assertEquals(palette.forRole(role), restored.forRole(role))
        }
    }

    @Test
    fun `round trip preserves a customized ramp`() {
        val customized = WafflePalette.Default.withRole(AppRole.Primary, rampFromAccent("Sapphire", Color(0xFF224466)))
        val restored = wafflePaletteFromJson(customized.toJson())
        assertEquals(customized.primary, restored.primary)
    }

    @Test
    fun `malformed json falls back to the given default`() {
        val fallback = WafflePalette.Default
        val restored = wafflePaletteFromJson("not json", fallback)
        assertEquals(fallback, restored)
    }

    @Test
    fun `missing role in json keeps the fallback ramp for that role`() {
        val fallback = WafflePalette.Default
        val restored = wafflePaletteFromJson("{}", fallback)
        assertEquals(fallback.primary, restored.primary)
        assertEquals(fallback.neutral, restored.neutral)
    }

    @Test
    fun `malformed ramp entry within json is ignored`() {
        val fallback = WafflePalette.Default
        val json = """{"primary": {"10": "not-a-hex"}}"""
        val restored = wafflePaletteFromJson(json, fallback)
        assertEquals(fallback.primary, restored.primary)
    }

    @Test
    fun `changing accent then serializing yields a different document`() {
        val original = WafflePalette.Default
        val changed = original.withRole(AppRole.Error, rampFromAccent("Garnet", Color(0xFF00FF00)))
        assertNotEquals(original.toJson(), changed.toJson())
    }
}
