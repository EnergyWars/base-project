package com.wafflehq.lib.settings.colors

import com.wafflehq.lib.uicore.io.readBytesLimited
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

private const val MAX_COLOR_THEME_IMPORT_BYTES = 10L * 1024 * 1024
internal const val MAX_IMPORTED_THEMES = 100
internal const val MAX_THEME_NAME_LENGTH = 60

data class ColorThemeExportEntry(
    val name: String,
    val data: ColorThemeData
) {
    val tokenCount: Int get() = data.overridesLight.size + data.overridesDark.size
}

class ColorThemeExportRepository(
    private val store: ColorOverrideStore,
    private val registry: ColorTokenRegistry
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun exportToStream(out: OutputStream, themeIds: Collection<String>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val export = ColorThemeExport(
                themes = store.storedThemes()
                    .filter { it.id in themeIds }
                    .map { ExportedColorTheme(it.name, it.data.overridesLight, it.data.overridesDark, it.data.baseRamps) }
            )
            out.write(json.encodeToString(export).toByteArray())
        }
    }

    suspend fun parseImport(input: InputStream, fallbackName: String): Result<List<ColorThemeExportEntry>> = withContext(Dispatchers.IO) {
        runCatching {
            val export = json.decodeFromString<ColorThemeExport>(input.readBytesLimited(MAX_COLOR_THEME_IMPORT_BYTES).decodeToString())
            val exported = export.themes.ifEmpty { legacyTheme(export, fallbackName) }
            exported.take(MAX_IMPORTED_THEMES).mapIndexedNotNull { index, theme ->
                val data = ColorThemeData(
                    overridesLight = validOverrides(theme.overridesLight),
                    overridesDark = validOverrides(theme.overridesDark),
                    baseRamps = theme.baseRamps.filterKeys { name -> ColorRamp.entries.any { it.name == name } }
                )
                if (data.isEmpty) null else ColorThemeExportEntry(themeName(theme.name, fallbackName, index), data)
            }
        }
    }

    private fun themeName(raw: String, fallbackName: String, index: Int): String =
        raw.trim().take(MAX_THEME_NAME_LENGTH).trim().ifEmpty { "$fallbackName ${index + 1}" }

    private fun legacyTheme(export: ColorThemeExport, fallbackName: String): List<ExportedColorTheme> {
        val light = export.overridesLight.ifEmpty { export.overrides }
        val dark = export.overridesDark.ifEmpty { export.overrides }
        return if (light.isEmpty() && dark.isEmpty()) emptyList() else listOf(ExportedColorTheme(fallbackName, light, dark))
    }

    private fun validOverrides(raw: Map<String, String>): Map<String, String> =
        raw.filter { (rawId, rawValue) -> registry.byId[ColorTokenId(rawId)] != null && ColorValueCodec.decode(rawValue) != null }

    suspend fun applyImport(entries: List<ColorThemeExportEntry>): List<ColorThemeInfo> = store.importThemes(entries)
}
