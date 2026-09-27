package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.first

suspend fun ColorOverrideStore.resolveArgbs(
    registry: ColorTokenRegistry,
    ids: Collection<ColorTokenId>,
    isDark: Boolean
): Map<ColorTokenId, Int> {
    val overrides = overridesFlow(isDark).first()
    val baseRampOverrides = baseRampOverridesFlow().first()
    return ids.associateWith { id ->
        ColorTokenResolver.resolve(registry.byId.getValue(id), overrides[id], isDark, baseRampOverrides).toArgb()
    }
}

suspend fun ColorOverrideStore.resolveArgb(registry: ColorTokenRegistry, id: ColorTokenId, isDark: Boolean): Int =
    resolveArgbs(registry, listOf(id), isDark).getValue(id)
