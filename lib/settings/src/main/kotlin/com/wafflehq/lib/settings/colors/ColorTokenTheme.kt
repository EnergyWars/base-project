package com.wafflehq.lib.settings.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = staticCompositionLocalOf { false }

val LocalColorTokens = staticCompositionLocalOf<Map<ColorTokenId, Color>> { emptyMap() }

val LocalColorTokenRegistry = staticCompositionLocalOf<ColorTokenRegistry?> { null }

@Composable
fun colorToken(id: ColorTokenId): Color {
    LocalColorTokens.current[id]?.let { return it }
    val token = LocalColorTokenRegistry.current?.byId?.get(id)
        ?: error("Unknown color token ${id.value}: provide a ColorTokenRegistry via ColorTokenTheme")
    return ColorTokenResolver.resolve(token, override = null, isDark = LocalIsDarkTheme.current)
}

@Composable
fun ColorTokenTheme(
    registry: ColorTokenRegistry,
    overrides: Map<ColorTokenId, ColorValue>,
    baseRampOverrides: Map<ColorRamp, Color>,
    isDark: Boolean,
    content: @Composable (resolved: Map<ColorTokenId, Color>) -> Unit
) {
    val resolved = remember(registry, overrides, baseRampOverrides, isDark) {
        ColorTokenResolver.resolveAll(registry, overrides, isDark, baseRampOverrides)
    }
    CompositionLocalProvider(
        LocalIsDarkTheme provides isDark,
        LocalColorTokens provides resolved,
        LocalColorTokenRegistry provides registry
    ) {
        content(resolved)
    }
}

@Composable
fun rememberMappedColorScheme(
    base: ColorScheme,
    resolved: Map<ColorTokenId, Color>,
    mapping: MaterialRoleMapping
): ColorScheme = remember(base, resolved, mapping) { base.withRoleMapping(resolved, mapping) }
