package com.wafflehq.uikit.color

import com.wafflehq.uikit.theme.AppRole
import com.wafflehq.uikit.theme.ColorRamp
import com.wafflehq.uikit.theme.WafflePalette
import org.json.JSONObject

private fun ColorRamp.toJson(): JSONObject = JSONObject().apply {
    tones.forEach { (tone, color) -> put(tone, color.toHexArgb()) }
}

private fun JSONObject.toRamp(name: String): ColorRamp? {
    val tones = listOf("10", "20", "30", "40", "50", "60", "70", "80", "90").map { tone ->
        val hex = optString(tone, "") .ifBlank { return null }
        tone to (hexToColorOrNull(hex) ?: return null)
    }
    return ColorRamp(name, tones)
}

/** Serializes the seven role ramps of a [WafflePalette] to a small, human-readable JSON document. */
fun WafflePalette.toJson(): String {
    val root = JSONObject()
    root.put("primary", primary.toJson())
    root.put("secondary", secondary.toJson())
    root.put("tertiary", tertiary.toJson())
    root.put("success", success.toJson())
    root.put("warning", warning.toJson())
    root.put("error", error.toJson())
    root.put("neutral", neutral.toJson())
    return root.toString()
}

/**
 * Parses a palette JSON document produced by [WafflePalette.toJson]. Any role missing or
 * malformed in [json] falls back to the matching role from [fallback], so a partial or corrupted
 * file never crashes the importer — it just leaves some ramps unchanged.
 */
fun wafflePaletteFromJson(json: String, fallback: WafflePalette = WafflePalette.Default): WafflePalette {
    val root = runCatching { JSONObject(json) }.getOrNull() ?: return fallback
    var palette = fallback
    for (role in AppRole.entries) {
        val key = role.name.lowercase()
        val ramp = root.optJSONObject(key)?.toRamp(palette.forRole(role).name) ?: continue
        palette = palette.withRole(role, ramp)
    }
    return palette
}
