package com.wafflehq.lib.settings.colors

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wafflehq.lib.settings.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

internal val colorJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

internal val LIBRARY_KEY = stringPreferencesKey("theme_library")
private val PENDING_KEY = stringPreferencesKey("safety_pending")
private val SIMPLIFIED_VIEW_ACTIVE_KEY = booleanPreferencesKey("simplified_color_view_active")

internal fun decodeLibrary(raw: String?): ColorThemeLibrary? =
    raw?.let { runCatching { colorJson.decodeFromString<ColorThemeLibrary>(it) }.getOrNull() }

private fun decodePending(raw: String?): ColorSafetyPending? =
    raw?.let { runCatching { colorJson.decodeFromString<ColorSafetyPending>(it) }.getOrNull() }

internal val Context.colorDataStore by preferencesDataStore(
    name = "color_theme_prefs",
    produceMigrations = { context ->
        listOf(
            LegacyColorOverrideMigration,
            LegacyColorThemeMigration(context.getString(R.string.appsettings_color_theme_migrated_name))
        )
    }
)

class ColorOverrideStore(
    private val dataStore: DataStore<Preferences>,
    private val clock: () -> Long = System::currentTimeMillis,
    private val confirmWindowMillis: Long = ColorSafety.CONFIRM_WINDOW_MILLIS,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() }
) {

    constructor(context: Context) : this(context.colorDataStore)

    private fun libraryFlow(): Flow<ColorThemeLibrary> = dataStore.data
        .map { decodeLibrary(it[LIBRARY_KEY]) ?: ColorThemeLibrary() }
        .distinctUntilChanged()

    private fun activeDataFlow(): Flow<ColorThemeData> =
        libraryFlow().map { it.active?.data ?: ColorThemeData() }.distinctUntilChanged()

    fun themesFlow(): Flow<List<ColorThemeInfo>> =
        libraryFlow().map { library -> library.themes.map { it.info } }.distinctUntilChanged()

    fun activeThemeIdFlow(): Flow<String?> = libraryFlow().map { it.active?.id }.distinctUntilChanged()

    suspend fun themeData(id: String): ColorThemeData? = libraryFlow().first().theme(id)?.data

    suspend fun storedThemes(): List<StoredColorTheme> = libraryFlow().first().themes

    fun overridesFlow(isDark: Boolean): Flow<Map<ColorTokenId, ColorValue>> =
        activeDataFlow().map { it.overrides(isDark) }.distinctUntilChanged()

    fun overrideFlow(id: ColorTokenId, isDark: Boolean): Flow<ColorValue?> =
        overridesFlow(isDark).map { it[id] }.distinctUntilChanged()

    fun baseRampOverridesFlow(): Flow<Map<ColorRamp, Color>> =
        activeDataFlow().map { it.baseRampOverrides() }.distinctUntilChanged()

    fun pendingConfirmationFlow(): Flow<ColorSafetyPending?> =
        dataStore.data.map { decodePending(it[PENDING_KEY]) }.distinctUntilChanged()

    private suspend fun editLibrary(withSafety: Boolean, transform: (ColorThemeLibrary) -> ColorThemeLibrary) {
        dataStore.edit { prefs ->
            val before = decodeLibrary(prefs[LIBRARY_KEY]) ?: ColorThemeLibrary()
            val after = transform(before)
            if (after == before) return@edit
            if (withSafety) {
                val snapshot = decodePending(prefs[PENDING_KEY])?.snapshot ?: before
                prefs[PENDING_KEY] = colorJson.encodeToString(ColorSafetyPending(snapshot, clock() + confirmWindowMillis))
            }
            prefs[LIBRARY_KEY] = colorJson.encodeToString(after)
        }
    }

    suspend fun setOverride(id: ColorTokenId, isDark: Boolean, value: ColorValue?) {
        editLibrary(withSafety = true) { it.updateActive { data -> data.withOverride(id, isDark, value) } }
    }

    suspend fun clearTokens(ids: Collection<ColorTokenId>, isDark: Boolean) {
        editLibrary(withSafety = true) { it.updateActive { data -> data.withoutTokens(ids, isDark) } }
    }

    suspend fun clearAll() {
        editLibrary(withSafety = true) { it.updateActive { ColorThemeData() } }
    }

    suspend fun setBaseRampOverride(ramp: ColorRamp, color: Color?) {
        editLibrary(withSafety = true) { it.updateActive { data -> data.withBaseRamp(ramp, color) } }
    }

    suspend fun createTheme(name: String, copyFromActive: Boolean = false): ColorThemeInfo {
        var created: StoredColorTheme? = null
        editLibrary(withSafety = true) { library ->
            val theme = StoredColorTheme(
                id = idGenerator(),
                name = uniqueThemeName(name, library.themes.map { it.name }),
                data = if (copyFromActive) library.active?.data ?: ColorThemeData() else ColorThemeData()
            )
            created = theme
            library.copy(activeId = theme.id, themes = library.themes + theme)
        }
        return created!!.info
    }

    suspend fun renameTheme(id: String, name: String) {
        editLibrary(withSafety = false) { library ->
            val others = library.themes.filter { it.id != id }.map { it.name }
            library.copy(themes = library.themes.map { if (it.id == id) it.copy(name = uniqueThemeName(name, others)) else it })
        }
    }

    suspend fun deleteTheme(id: String) {
        editLibrary(withSafety = false) { library ->
            library.copy(
                activeId = library.activeId.takeIf { it != id },
                themes = library.themes.filter { it.id != id }
            )
        }
    }

    suspend fun activateTheme(id: String?) {
        editLibrary(withSafety = id != null) { library ->
            if (id != null && library.theme(id) == null) library else library.copy(activeId = id)
        }
    }

    suspend fun importThemes(themes: List<ColorThemeExportEntry>): List<ColorThemeInfo> {
        val added = mutableListOf<StoredColorTheme>()
        editLibrary(withSafety = false) { library ->
            val names = library.themes.map { it.name }.toMutableList()
            themes.forEach { entry ->
                val unique = uniqueThemeName(entry.name, names)
                names += unique
                added += StoredColorTheme(idGenerator(), unique, entry.data)
            }
            library.copy(themes = library.themes + added)
        }
        return added.map { it.info }
    }

    suspend fun confirmPending() {
        dataStore.edit { it.remove(PENDING_KEY) }
    }

    suspend fun revertPending() {
        dataStore.edit { prefs ->
            val pending = decodePending(prefs[PENDING_KEY]) ?: return@edit
            prefs[LIBRARY_KEY] = colorJson.encodeToString(pending.snapshot)
            prefs.remove(PENDING_KEY)
        }
    }

    suspend fun revertIfExpired(): Boolean {
        var reverted = false
        dataStore.edit { prefs ->
            val pending = decodePending(prefs[PENDING_KEY]) ?: return@edit
            if (clock() < pending.deadlineMillis) return@edit
            prefs[LIBRARY_KEY] = colorJson.encodeToString(pending.snapshot)
            prefs.remove(PENDING_KEY)
            reverted = true
        }
        return reverted
    }

    suspend fun resetToStandardTheme() {
        dataStore.edit { prefs ->
            val before = decodeLibrary(prefs[LIBRARY_KEY]) ?: ColorThemeLibrary()
            prefs[LIBRARY_KEY] = colorJson.encodeToString(before.copy(activeId = null))
            prefs.remove(PENDING_KEY)
        }
    }

    fun simplifiedColorViewActiveFlow(): Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[SIMPLIFIED_VIEW_ACTIVE_KEY] ?: true }

    suspend fun setSimplifiedColorViewActive(active: Boolean) {
        dataStore.edit { prefs -> prefs[SIMPLIFIED_VIEW_ACTIVE_KEY] = active }
    }
}
