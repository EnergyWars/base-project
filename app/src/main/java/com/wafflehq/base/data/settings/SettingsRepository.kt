package com.wafflehq.base.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wafflehq.uikit.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    private val context: Context
) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val checkedFeatureFilesKey = stringSetPreferencesKey("checked_feature_files")
    private val showHiddenFeatureFilesKey = booleanPreferencesKey("show_hidden_feature_files")
    private val paletteJsonKey = stringPreferencesKey("palette_json")

    val themeMode: Flow<ThemeMode> = context.settingsDataStore.data.map { prefs ->
        ThemeMode.fromName(prefs[themeModeKey])
    }

    val paletteJson: Flow<String?> = context.settingsDataStore.data.map { prefs ->
        prefs[paletteJsonKey]
    }

    suspend fun setPaletteJson(json: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[paletteJsonKey] = json
        }
    }

    val checkedFeatureFiles: Flow<Set<String>> = context.settingsDataStore.data.map { prefs ->
        prefs[checkedFeatureFilesKey].orEmpty()
    }

    val showHiddenFeatureFiles: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[showHiddenFeatureFilesKey] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    suspend fun setFeatureFileChecked(fileName: String, checked: Boolean) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[checkedFeatureFilesKey].orEmpty().toMutableSet()
            if (checked) current.add(fileName) else current.remove(fileName)
            prefs[checkedFeatureFilesKey] = current
        }
    }

    suspend fun setShowHiddenFeatureFiles(show: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[showHiddenFeatureFilesKey] = show
        }
    }
}
