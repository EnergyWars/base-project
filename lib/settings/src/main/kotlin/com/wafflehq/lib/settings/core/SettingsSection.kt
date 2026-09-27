package com.wafflehq.lib.settings.core

import androidx.annotation.StringRes
import com.wafflehq.lib.settings.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class SettingsSection(
    val id: String,
    val order: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val highlighted: Boolean = false,
    val isVisible: Flow<Boolean> = flowOf(true),
    val onOpen: () -> Unit
)

object LibrarySettingsSections {

    const val COLORS = "colors"
    const val ENCRYPTION = "encryption"
    const val LEGAL = "legal"

    fun colors(
        order: Int,
        onOpen: () -> Unit,
        @StringRes titleRes: Int = R.string.appsettings_section_colors,
        @StringRes descriptionRes: Int = R.string.appsettings_section_colors_desc,
        isVisible: Flow<Boolean> = flowOf(true)
    ): SettingsSection = SettingsSection(COLORS, order, titleRes, descriptionRes, false, isVisible, onOpen)

    fun encryption(
        order: Int,
        onOpen: () -> Unit,
        @StringRes titleRes: Int = R.string.appsettings_section_encryption,
        @StringRes descriptionRes: Int = R.string.appsettings_section_encryption_desc,
        isVisible: Flow<Boolean> = flowOf(true)
    ): SettingsSection = SettingsSection(ENCRYPTION, order, titleRes, descriptionRes, false, isVisible, onOpen)

    fun legal(
        order: Int,
        onOpen: () -> Unit,
        @StringRes titleRes: Int = R.string.appsettings_section_legal,
        @StringRes descriptionRes: Int = R.string.appsettings_section_legal_desc,
        isVisible: Flow<Boolean> = flowOf(true)
    ): SettingsSection = SettingsSection(LEGAL, order, titleRes, descriptionRes, false, isVisible, onOpen)
}
