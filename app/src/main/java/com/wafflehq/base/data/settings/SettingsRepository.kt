package com.wafflehq.base.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wafflehq.base.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.settingsDataStore)

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.fromName(prefs[THEME_MODE_KEY])
    }

    val checkedFeatureFiles: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[CHECKED_FEATURE_FILES_KEY].orEmpty()
    }

    val showHiddenFeatureFiles: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SHOW_HIDDEN_FEATURE_FILES_KEY] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[THEME_MODE_KEY] = mode.name }
    }

    suspend fun setFeatureFileChecked(fileName: String, checked: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[CHECKED_FEATURE_FILES_KEY].orEmpty().toMutableSet()
            if (checked) current.add(fileName) else current.remove(fileName)
            prefs[CHECKED_FEATURE_FILES_KEY] = current
        }
    }

    suspend fun setShowHiddenFeatureFiles(show: Boolean) {
        dataStore.edit { prefs -> prefs[SHOW_HIDDEN_FEATURE_FILES_KEY] = show }
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val CHECKED_FEATURE_FILES_KEY = stringSetPreferencesKey("checked_feature_files")
        val SHOW_HIDDEN_FEATURE_FILES_KEY = booleanPreferencesKey("show_hidden_feature_files")
    }
}
