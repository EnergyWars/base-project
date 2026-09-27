package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.wafflehq.lib.uicore.color.Hsl
import com.wafflehq.lib.uicore.color.toColor
import com.wafflehq.lib.uicore.color.toHsl
import kotlin.math.abs

object ColorRampTable {

    const val BASE_STEP = 40

    private const val ACHROMATIC_SATURATION = 6f
    private const val ACHROMATIC_TINT = 0.5f
    private const val ACHROMATIC_MIN_LIGHTNESS = 2f
    private const val ACHROMATIC_MAX_LIGHTNESS = 98f

    private val defaultTable: Map<ColorRamp, Map<Int, Color>> = mapOf(
        ColorRamp.SAPPHIRE to mapOf(
            10 to Sapphire10, 20 to Sapphire20, 30 to Sapphire30, 40 to Sapphire40, 50 to Sapphire50,
            60 to Sapphire60, 70 to Sapphire70, 80 to Sapphire80, 90 to Sapphire90
        ),
        ColorRamp.AQUAMARINE to mapOf(
            10 to Aquamarine10, 20 to Aquamarine20, 30 to Aquamarine30, 40 to Aquamarine40, 50 to Aquamarine50,
            60 to Aquamarine60, 70 to Aquamarine70, 80 to Aquamarine80, 90 to Aquamarine90
        ),
        ColorRamp.AMETHYST to mapOf(
            10 to Amethyst10, 20 to Amethyst20, 30 to Amethyst30, 40 to Amethyst40, 50 to Amethyst50,
            60 to Amethyst60, 70 to Amethyst70, 80 to Amethyst80, 90 to Amethyst90
        ),
        ColorRamp.EMERALD to mapOf(
            10 to Emerald10, 20 to Emerald20, 30 to Emerald30, 40 to Emerald40, 50 to Emerald50,
            60 to Emerald60, 70 to Emerald70, 80 to Emerald80, 90 to Emerald90
        ),
        ColorRamp.CITRINE to mapOf(
            10 to Citrine10, 20 to Citrine20, 30 to Citrine30, 40 to Citrine40, 50 to Citrine50,
            60 to Citrine60, 70 to Citrine70, 80 to Citrine80, 90 to Citrine90
        ),
        ColorRamp.GARNET to mapOf(
            10 to Garnet10, 20 to Garnet20, 30 to Garnet30, 40 to Garnet40, 50 to Garnet50,
            60 to Garnet60, 70 to Garnet70, 80 to Garnet80, 90 to Garnet90
        ),
        ColorRamp.GRAPHITE to mapOf(
            10 to Graphite10, 20 to Graphite20, 30 to Graphite30, 40 to Graphite40, 50 to Graphite50,
            60 to Graphite60, 70 to Graphite70, 80 to Graphite80, 90 to Graphite90
        )
    )

    private val reverseLookup: Map<Int, Pair<ColorRamp, Int>> =
        defaultTable.entries
            .flatMap { (ramp, steps) -> steps.entries.map { (step, color) -> color.toArgb() to (ramp to step) } }
            .toMap()

    fun swatch(ramp: ColorRamp, step: Int, baseOverrides: Map<ColorRamp, Color> = emptyMap()): Color {
        val seed = baseOverrides[ramp] ?: return defaultTable.getValue(ramp).getValue(step)
        return generatedRamp(seed).getValue(step)
    }

    fun allSwatches(baseOverrides: Map<ColorRamp, Color> = emptyMap()): List<Triple<ColorRamp, Int, Color>> =
        ColorRamp.entries.flatMap { ramp -> ColorRampSteps.map { step -> Triple(ramp, step, swatch(ramp, step, baseOverrides)) } }

    fun baseSeed(ramp: ColorRamp): Color = defaultTable.getValue(ramp).getValue(BASE_STEP)

    fun rampReferenceOf(color: Color): Pair<ColorRamp, Int>? = reverseLookup[color.copy(alpha = 1f).toArgb()]

    fun follow(color: Color, baseOverrides: Map<ColorRamp, Color>, anchorRamp: ColorRamp? = null): Color {
        if (baseOverrides.isEmpty() || color.alpha == 0f) return color
        val opaque = color.copy(alpha = 1f)
        reverseLookup[opaque.toArgb()]?.let { (ramp, step) ->
            return if (ramp in baseOverrides) swatch(ramp, step, baseOverrides).copy(alpha = color.alpha) else color
        }
        val hsl = opaque.toHsl()
        val achromatic = hsl.s < ACHROMATIC_SATURATION
        val ramp = anchorRamp ?: if (achromatic) ColorRamp.GRAPHITE else nearestRamp(hsl.h)
        val seedOverride = baseOverrides[ramp] ?: return color
        val seed = seedOverride.toHsl()
        val original = baseSeed(ramp).toHsl()
        val derived = if (achromatic) {
            Hsl(seed.h, seed.s * ACHROMATIC_TINT, hsl.l.coerceIn(ACHROMATIC_MIN_LIGHTNESS, ACHROMATIC_MAX_LIGHTNESS))
        } else {
            val scale = if (original.s > 0f) seed.s / original.s else 1f
            Hsl(seed.h + (hsl.h - original.h), (hsl.s * scale).coerceIn(0f, 100f), hsl.l)
        }
        return derived.toColor(color.alpha)
    }

    private fun nearestRamp(hue: Float): ColorRamp =
        ColorRamp.entries
            .filter { it != ColorRamp.GRAPHITE }
            .minBy { hueDistance(hue, baseSeed(it).toHsl().h) }

    private fun hueDistance(a: Float, b: Float): Float {
        val diff = abs(a - b) % 360f
        return if (diff > 180f) 360f - diff else diff
    }

    fun generatedRamp(seed: Color): Map<Int, Color> {
        val hsl = seed.toHsl()
        return ColorRampSteps.associateWith { step -> Hsl(hsl.h, hsl.s, step.toFloat()).toColor() }
    }
}
