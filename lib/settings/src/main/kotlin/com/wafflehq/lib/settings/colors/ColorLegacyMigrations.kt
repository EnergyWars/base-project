package com.wafflehq.lib.settings.colors

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.encodeToString

internal const val LEGACY_OVERRIDE_KEY_PREFIX = "override_"
internal const val LIGHT_OVERRIDE_KEY_PREFIX = "override_light_"
internal const val DARK_OVERRIDE_KEY_PREFIX = "override_dark_"
internal const val BASE_RAMP_KEY_PREFIX = "base_ramp_"
internal const val MIGRATED_THEME_ID = "migrated"

internal object LegacyColorOverrideMigration : DataMigration<Preferences> {
    private fun Preferences.Key<*>.isLegacy() =
        name.startsWith(LEGACY_OVERRIDE_KEY_PREFIX) &&
            !name.startsWith(LIGHT_OVERRIDE_KEY_PREFIX) &&
            !name.startsWith(DARK_OVERRIDE_KEY_PREFIX)

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        currentData.asMap().keys.any { it.isLegacy() }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val mutable = currentData.toMutablePreferences()
        currentData.asMap().forEach { (key, value) ->
            if (key.isLegacy() && value is String) {
                val id = key.name.removePrefix(LEGACY_OVERRIDE_KEY_PREFIX)
                mutable[stringPreferencesKey("$LIGHT_OVERRIDE_KEY_PREFIX$id")] = value
                mutable[stringPreferencesKey("$DARK_OVERRIDE_KEY_PREFIX$id")] = value
                mutable.remove(key)
            }
        }
        return mutable.toPreferences()
    }

    override suspend fun cleanUp() = Unit
}

internal class LegacyColorThemeMigration(private val themeName: String) : DataMigration<Preferences> {
    private fun Preferences.Key<*>.isThemeKey() =
        name.startsWith(LIGHT_OVERRIDE_KEY_PREFIX) ||
            name.startsWith(DARK_OVERRIDE_KEY_PREFIX) ||
            name.startsWith(BASE_RAMP_KEY_PREFIX)

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        currentData.asMap().keys.any { it.isThemeKey() }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val light = mutableMapOf<String, String>()
        val dark = mutableMapOf<String, String>()
        val ramps = mutableMapOf<String, Int>()
        val mutable = currentData.toMutablePreferences()
        currentData.asMap().forEach { (key, value) ->
            if (value !is String || !key.isThemeKey()) return@forEach
            when {
                key.name.startsWith(LIGHT_OVERRIDE_KEY_PREFIX) -> light[key.name.removePrefix(LIGHT_OVERRIDE_KEY_PREFIX)] = value
                key.name.startsWith(DARK_OVERRIDE_KEY_PREFIX) -> dark[key.name.removePrefix(DARK_OVERRIDE_KEY_PREFIX)] = value
                else -> value.toIntOrNull()?.let { ramps[key.name.removePrefix(BASE_RAMP_KEY_PREFIX)] = it }
            }
            mutable.remove(key)
        }
        val existing = currentData[LIBRARY_KEY]?.let(::decodeLibrary)
        val migrated = StoredColorTheme(MIGRATED_THEME_ID, themeName, ColorThemeData(light, dark, ramps))
        val library = if (existing == null || existing.themes.isEmpty()) {
            ColorThemeLibrary(activeId = migrated.id, themes = listOf(migrated))
        } else {
            existing.copy(themes = existing.themes + migrated.copy(name = uniqueThemeName(themeName, existing.themes.map { it.name })))
        }
        mutable[LIBRARY_KEY] = colorJson.encodeToString(library)
        return mutable.toPreferences()
    }

    override suspend fun cleanUp() = Unit
}
