package com.wafflehq.lib.settings.colors

import kotlinx.serialization.Serializable

@Serializable
data class ExportedColorTheme(
    val name: String,
    val overridesLight: Map<String, String> = emptyMap(),
    val overridesDark: Map<String, String> = emptyMap(),
    val baseRamps: Map<String, Int> = emptyMap()
)

@Serializable
data class ColorThemeExport(
    val schemaVersion: Int = 3,
    val themes: List<ExportedColorTheme> = emptyList(),
    val overridesLight: Map<String, String> = emptyMap(),
    val overridesDark: Map<String, String> = emptyMap(),
    val overrides: Map<String, String> = emptyMap()
)
