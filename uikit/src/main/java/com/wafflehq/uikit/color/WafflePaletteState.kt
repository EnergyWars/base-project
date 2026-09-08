package com.wafflehq.uikit.color

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.wafflehq.uikit.theme.AppRole
import com.wafflehq.uikit.theme.WafflePalette

/**
 * Hoistable, Compose-observable holder for a [WafflePalette] the user is editing live via the
 * color picker. Framework-free on purpose (no Hilt/Room/DataStore, matching the rest of this
 * package) — a host app owns persistence and passes it in as [onPaletteChanged].
 */
class WafflePaletteState(
    initial: WafflePalette = WafflePalette.Default,
    private val onPaletteChanged: (WafflePalette) -> Unit = {},
) {
    var palette: WafflePalette by mutableStateOf(initial)
        private set

    fun setRoleAccent(role: AppRole, accent: Color) {
        val ramp = rampFromAccent(palette.forRole(role).name, accent)
        update(palette.withRole(role, ramp))
    }

    fun resetRole(role: AppRole) {
        update(palette.withRole(role, WafflePalette.Default.forRole(role)))
    }

    fun resetAll() {
        update(WafflePalette.Default)
    }

    fun replace(newPalette: WafflePalette) {
        update(newPalette)
    }

    /**
     * Applies a palette loaded from outside (e.g. persisted storage) without re-triggering
     * [onPaletteChanged] — avoids feedback loops when a host re-syncs this state from the same
     * store it also writes [onPaletteChanged] into.
     */
    fun syncFromExternal(newPalette: WafflePalette) {
        if (newPalette != palette) palette = newPalette
    }

    private fun update(newPalette: WafflePalette) {
        palette = newPalette
        onPaletteChanged(newPalette)
    }
}
