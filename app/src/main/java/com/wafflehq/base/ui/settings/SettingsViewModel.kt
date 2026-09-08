package com.wafflehq.base.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wafflehq.base.data.features.FeatureFilesRepository
import com.wafflehq.base.data.settings.SettingsRepository
import com.wafflehq.uikit.color.WafflePaletteState
import com.wafflehq.uikit.color.toJson
import com.wafflehq.uikit.color.wafflePaletteFromJson
import com.wafflehq.uikit.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val featureFilesRepository: FeatureFilesRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = repository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM
        )

    val paletteState: WafflePaletteState = WafflePaletteState(
        onPaletteChanged = { palette -> viewModelScope.launch { repository.setPaletteJson(palette.toJson()) } },
    )

    private val _featureFilesCount = MutableStateFlow(0)
    val featureFilesCount: StateFlow<Int> = _featureFilesCount.asStateFlow()

    init {
        viewModelScope.launch {
            _featureFilesCount.value = featureFilesRepository.list().size
        }
        viewModelScope.launch {
            repository.paletteJson.collect { json ->
                if (json != null) paletteState.syncFromExternal(wafflePaletteFromJson(json))
            }
        }
    }

    fun onThemeModeSelected(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }
}
