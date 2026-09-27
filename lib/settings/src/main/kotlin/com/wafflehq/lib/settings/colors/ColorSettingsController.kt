package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.Color
import com.wafflehq.lib.settings.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

data class ColorThemeImportCandidate(
    val entry: ColorThemeExportEntry,
    val selected: Boolean
)

enum class ColorSettingsMessage(val labelRes: Int) {
    EXPORT_SUCCESS(R.string.appsettings_color_theme_export_success),
    EXPORT_ERROR(R.string.appsettings_color_theme_export_error),
    IMPORT_SUCCESS(R.string.appsettings_color_theme_import_success),
    IMPORT_EMPTY(R.string.appsettings_color_theme_import_empty),
    IMPORT_ERROR(R.string.appsettings_color_theme_import_error)
}

class ColorSettingsController(
    private val store: ColorOverrideStore,
    private val exportRepository: ColorThemeExportRepository,
    private val registry: ColorTokenRegistry,
    private val scope: CoroutineScope
) {

    private val lightOverrides: StateFlow<Map<ColorTokenId, ColorValue>> = store.overridesFlow(isDark = false)
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyMap())
    private val darkOverrides: StateFlow<Map<ColorTokenId, ColorValue>> = store.overridesFlow(isDark = true)
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun overrides(isDark: Boolean): StateFlow<Map<ColorTokenId, ColorValue>> =
        if (isDark) darkOverrides else lightOverrides

    val baseRampOverrides: StateFlow<Map<ColorRamp, Color>> = store.baseRampOverridesFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val simplifiedViewActive: StateFlow<Boolean> = store.simplifiedColorViewActiveFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), true)

    val themes: StateFlow<List<ColorThemeInfo>> = store.themesFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeThemeId: StateFlow<String?> = store.activeThemeIdFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    private val _importCandidates = MutableStateFlow<List<ColorThemeImportCandidate>>(emptyList())
    val importCandidates: StateFlow<List<ColorThemeImportCandidate>> = _importCandidates

    private val _snackbarMessage = MutableStateFlow<ColorSettingsMessage?>(null)
    val snackbarMessage: StateFlow<ColorSettingsMessage?> = _snackbarMessage

    private var heldEdit: (suspend () -> Unit)? = null
    private val _themeNameRequested = MutableStateFlow(false)
    val themeNameRequested: StateFlow<Boolean> = _themeNameRequested

    fun setSimplifiedViewActive(active: Boolean) {
        scope.launch { store.setSimplifiedColorViewActive(active) }
    }

    private fun edit(block: suspend () -> Unit) {
        scope.launch {
            if (store.activeThemeIdFlow().first() == null) {
                heldEdit = block
                _themeNameRequested.value = true
            } else {
                block()
            }
        }
    }

    fun createThemeForEdit(name: String) {
        val edit = heldEdit
        heldEdit = null
        _themeNameRequested.value = false
        scope.launch {
            store.createTheme(name)
            edit?.invoke()
        }
    }

    fun dismissThemeNameRequest() {
        heldEdit = null
        _themeNameRequested.value = false
    }

    fun setBaseRampOverride(ramp: ColorRamp, color: Color) {
        edit { store.setBaseRampOverride(ramp, color) }
    }

    fun resetBaseRamp(ramp: ColorRamp) {
        edit { store.setBaseRampOverride(ramp, null) }
    }

    fun setOverride(id: ColorTokenId, isDark: Boolean, value: ColorValue?) {
        edit { store.setOverride(id, isDark, value) }
    }

    fun resetCategory(categoryKey: String, isDark: Boolean) {
        val ids = registry.tokensOf(categoryKey).map { it.id }
        edit { store.clearTokens(ids, isDark) }
    }

    fun resetAll() {
        edit { store.clearAll() }
    }

    fun createTheme(name: String, copyFromActive: Boolean = false) {
        scope.launch { store.createTheme(name, copyFromActive) }
    }

    fun renameTheme(id: String, name: String) {
        scope.launch { store.renameTheme(id, name) }
    }

    fun deleteTheme(id: String) {
        scope.launch { store.deleteTheme(id) }
    }

    fun activateTheme(id: String?) {
        scope.launch { store.activateTheme(id) }
    }

    fun exportToStream(out: OutputStream, themeIds: Collection<String>) {
        scope.launch {
            val result = exportRepository.exportToStream(out, themeIds)
            _snackbarMessage.value =
                if (result.isSuccess) ColorSettingsMessage.EXPORT_SUCCESS else ColorSettingsMessage.EXPORT_ERROR
        }
    }

    fun loadImportCandidates(input: InputStream, fallbackName: String) {
        scope.launch {
            exportRepository.parseImport(input, fallbackName).fold(
                onSuccess = { entries ->
                    if (entries.isEmpty()) {
                        _snackbarMessage.value = ColorSettingsMessage.IMPORT_EMPTY
                    } else {
                        _importCandidates.value = entries.map { ColorThemeImportCandidate(it, selected = true) }
                    }
                },
                onFailure = { _snackbarMessage.value = ColorSettingsMessage.IMPORT_ERROR }
            )
        }
    }

    fun toggleImportCandidate(index: Int) {
        _importCandidates.update { list ->
            list.mapIndexed { i, candidate -> if (i == index) candidate.copy(selected = !candidate.selected) else candidate }
        }
    }

    fun setAllImportCandidatesSelected(selected: Boolean) {
        _importCandidates.update { list -> list.map { it.copy(selected = selected) } }
    }

    fun dismissImport() {
        _importCandidates.value = emptyList()
    }

    fun confirmImport() {
        scope.launch {
            val selected = _importCandidates.value.filter { it.selected }.map { it.entry }
            exportRepository.applyImport(selected)
            _importCandidates.value = emptyList()
            _snackbarMessage.value = ColorSettingsMessage.IMPORT_SUCCESS
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
