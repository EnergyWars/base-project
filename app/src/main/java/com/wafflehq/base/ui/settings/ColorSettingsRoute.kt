package com.wafflehq.base.ui.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.wafflehq.base.domain.colortheme.ColorTokenCatalog
import com.wafflehq.base.domain.colortheme.ColorTokenCategory
import com.wafflehq.base.ui.theme.GeistMono
import com.wafflehq.lib.settings.colors.ui.ColorCategoryScreen
import com.wafflehq.lib.settings.colors.ui.ColorSettingsScreen
import com.wafflehq.lib.uicore.io.jsonExportFileName

private const val EXPORT_FILE_PREFIX = "baseapp_theme"

internal val AllColorCategoryKeys: Set<String> = ColorTokenCategory.entries.mapTo(mutableSetOf()) { it.name }

internal fun colorExportFileName(themeName: String?): String =
    jsonExportFileName(themeName?.let { "${EXPORT_FILE_PREFIX}_$it" } ?: "${EXPORT_FILE_PREFIX}s")

@Composable
fun ColorSettingsRoute(
    onBack: () -> Unit,
    onNavigateToCategory: (String) -> Unit,
    viewModel: ColorSettingsViewModel = hiltViewModel()
) {
    ColorSettingsScreen(
        controller = viewModel.controller,
        registry = ColorTokenCatalog.registry,
        visibleCategoryKeys = AllColorCategoryKeys,
        onBack = onBack,
        onNavigateToCategory = onNavigateToCategory,
        exportFileName = ::colorExportFileName
    )
}

@Composable
fun ColorCategoryRoute(
    categoryKey: String,
    onBack: () -> Unit,
    viewModel: ColorSettingsViewModel = hiltViewModel()
) {
    ColorCategoryScreen(
        categoryKey = categoryKey,
        controller = viewModel.controller,
        registry = ColorTokenCatalog.registry,
        onBack = onBack,
        idFontFamily = GeistMono
    )
}
