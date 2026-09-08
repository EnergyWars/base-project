package com.wafflehq.uikit.color

import androidx.compose.ui.graphics.Color
import com.wafflehq.uikit.theme.ColorRamp

private val toneLightnessPercent = listOf(
    "10" to 6f,
    "20" to 13f,
    "30" to 21f,
    "40" to 30f,
    "50" to 40f,
    "60" to 50f,
    "70" to 62f,
    "80" to 75f,
    "90" to 88f,
)

/**
 * Derives a full, always-coherent 9-tone [ColorRamp] from a single accent color by keeping its
 * hue/saturation and remapping lightness onto a fixed, contrast-safe curve. This is what powers
 * the palette editor's color picker: pick one accent, every tone (and therefore every showcase
 * example built from [com.wafflehq.uikit.theme.AppTheme.colorRamps]) stays consistent.
 */
fun rampFromAccent(name: String, accent: Color): ColorRamp {
    val hsl = accent.toHsl()
    val tones = toneLightnessPercent.map { (label, lightness) ->
        label to Hsl(hsl.h, hsl.s, lightness).toColor()
    }
    return ColorRamp(name, tones)
}
