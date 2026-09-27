package com.wafflehq.base.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wafflehq.lib.settings.colors.ColorOverrideStore
import com.wafflehq.lib.settings.colors.ColorSettingsController
import com.wafflehq.lib.settings.colors.ColorThemeExportRepository
import com.wafflehq.lib.settings.colors.ColorTokenRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ColorSettingsViewModel @Inject constructor(
    store: ColorOverrideStore,
    exportRepository: ColorThemeExportRepository,
    registry: ColorTokenRegistry
) : ViewModel() {

    val controller = ColorSettingsController(store, exportRepository, registry, viewModelScope)
}
