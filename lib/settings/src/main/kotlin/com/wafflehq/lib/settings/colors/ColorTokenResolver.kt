package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.Color

object ColorTokenResolver {
    fun resolve(
        token: ColorToken,
        override: ColorValue?,
        isDark: Boolean,
        baseRampOverrides: Map<ColorRamp, Color> = emptyMap()
    ): Color = when (override) {
        is ColorValue.Palette -> ColorRampTable.swatch(override.ramp, override.step, baseRampOverrides).copy(alpha = override.alpha)
        is ColorValue.Custom -> Color(override.argb)
        null -> ColorRampTable.follow(
            color = if (isDark) token.defaultDark else token.defaultLight,
            baseOverrides = baseRampOverrides,
            anchorRamp = token.anchorRamp
        )
    }

    fun resolveAll(
        registry: ColorTokenRegistry,
        overrides: Map<ColorTokenId, ColorValue>,
        isDark: Boolean,
        baseRampOverrides: Map<ColorRamp, Color> = emptyMap()
    ): Map<ColorTokenId, Color> = registry.all.associate { token ->
        token.id to resolve(token, overrides[token.id], isDark, baseRampOverrides)
    }
}
