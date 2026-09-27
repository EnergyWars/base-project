package com.wafflehq.lib.settings.colors.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.lib.navigation.R as NavR
import com.wafflehq.lib.navigation.settings.SettingsNavRow
import com.wafflehq.lib.navigation.settings.SettingsRowDivider
import com.wafflehq.lib.navigation.settings.SettingsScaffold
import com.wafflehq.lib.navigation.settings.SettingsSurfaceList
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.colors.ColorRamp
import com.wafflehq.lib.settings.colors.ColorRampTable
import com.wafflehq.lib.settings.colors.ColorSettingsController
import com.wafflehq.lib.settings.colors.ColorTokenRegistry
import com.wafflehq.lib.settings.colors.labelRes
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.io.jsonExportFileName
import com.wafflehq.lib.uicore.components.AppSnackbarHost
import com.wafflehq.lib.uicore.components.AppTabRow
import com.wafflehq.lib.uicore.components.AppTab

private val FileNameUnsafeChars = Regex("[^\\p{L}\\p{N}_-]+")

@Composable
fun ColorSettingsScreen(
    controller: ColorSettingsController,
    registry: ColorTokenRegistry,
    visibleCategoryKeys: Set<String>,
    onBack: () -> Unit,
    onNavigateToCategory: (String) -> Unit,
    exportFileName: (themeName: String?) -> String = { name -> jsonExportFileName(name ?: "themes") }
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by controller.snackbarMessage.collectAsStateWithLifecycle()
    val importCandidates by controller.importCandidates.collectAsStateWithLifecycle()
    val lightOverrides by controller.overrides(isDark = false).collectAsStateWithLifecycle()
    val darkOverrides by controller.overrides(isDark = true).collectAsStateWithLifecycle()
    val changedTokenIds = lightOverrides.keys + darkOverrides.keys
    val simplifiedViewActive by controller.simplifiedViewActive.collectAsStateWithLifecycle()
    val baseRampOverrides by controller.baseRampOverrides.collectAsStateWithLifecycle()
    var editingRamp by remember { mutableStateOf<ColorRamp?>(null) }
    var resettingRamp by remember { mutableStateOf<ColorRamp?>(null) }
    var showResetAllConfirm by remember { mutableStateOf(false) }

    val snackbarText = snackbarMessage?.let { stringResource(it.labelRes) }
    LaunchedEffect(snackbarMessage) {
        val msg = snackbarText ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
        controller.clearSnackbar()
    }

    val themes by controller.themes.collectAsStateWithLifecycle()
    val activeThemeId by controller.activeThemeId.collectAsStateWithLifecycle()
    var showExportDialog by remember { mutableStateOf(false) }
    var exportThemeIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val importFallbackName = stringResource(R.string.appsettings_color_theme_section_title)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { target ->
            context.contentResolver.openOutputStream(target)?.use { out -> controller.exportToStream(out, exportThemeIds) }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { target ->
            context.contentResolver.openInputStream(target)?.use { input -> controller.loadImportCandidates(input, importFallbackName) }
        }
    }

    if (importCandidates.isNotEmpty()) {
        ColorThemeImportDialog(
            controller = controller,
            onDismiss = { controller.dismissImport() }
        )
    }

    if (showExportDialog) {
        ColorThemeExportDialog(
            themes = themes,
            initiallySelected = setOfNotNull(activeThemeId),
            onConfirm = { ids ->
                showExportDialog = false
                exportThemeIds = ids
                val single = themes.singleOrNull { it.id in ids }
                exportLauncher.launch(exportFileName(single?.name?.replace(FileNameUnsafeChars, "_")))
            },
            onDismiss = { showExportDialog = false }
        )
    }

    ColorThemeNameRequestHost(controller)

    val visibleCategories = registry.categories
        .filter { registry.tokensOf(it.key).isNotEmpty() && it.key in visibleCategoryKeys }

    if (showResetAllConfirm) {
        AppDialog(
            onDismissRequest = { showResetAllConfirm = false },
            title = { Text(stringResource(R.string.appsettings_color_reset_all_confirm_title)) },
            text = { Text(stringResource(R.string.appsettings_color_reset_all_confirm_message)) },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_color_reset_token),
                    role = AppButtonRole.Error,
                    variant = AppButtonVariant.Text,
                    onClick = {
                        controller.resetAll()
                        showResetAllConfirm = false
                    },
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = { showResetAllConfirm = false },
                )
            }
        )
    }

    resettingRamp?.let { ramp ->
        AppDialog(
            onDismissRequest = { resettingRamp = null },
            title = { Text(stringResource(R.string.appsettings_color_reset_ramp_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.appsettings_color_reset_ramp_confirm_message,
                        stringResource(ramp.labelRes)
                    )
                )
            },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_color_reset_token),
                    role = AppButtonRole.Error,
                    variant = AppButtonVariant.Text,
                    onClick = {
                        controller.resetBaseRamp(ramp)
                        resettingRamp = null
                    },
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = { resettingRamp = null },
                )
            }
        )
    }

    editingRamp?.let { ramp ->
        BaseRampEditorDialog(
            title = stringResource(ramp.labelRes),
            currentValue = baseRampOverrides[ramp],
            defaultSeed = ColorRampTable.baseSeed(ramp),
            onValueSelected = { color ->
                controller.setBaseRampOverride(ramp, color)
                editingRamp = null
            },
            onReset = {
                controller.resetBaseRamp(ramp)
                editingRamp = null
            },
            onDismiss = { editingRamp = null }
        )
    }

    SettingsScaffold(
        title = stringResource(R.string.appsettings_color_settings_title),
        onBack = onBack,
        backDescription = stringResource(NavR.string.navigation_navigate_back),
        actions = {
            ColorThemeExportImportActions(
                onExport = { showExportDialog = true },
                exportEnabled = themes.isNotEmpty(),
                onImport = { importLauncher.launch(arrayOf("application/json")) }
            )
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item { ColorThemeSelector(controller = controller, modifier = Modifier.padding(top = AppSpacing.sm)) }
            item {
                AppTabRow(selectedTabIndex = if (simplifiedViewActive) 0 else 1) {
                    AppTab(
                        selected = simplifiedViewActive,
                        onClick = { controller.setSimplifiedViewActive(true) },
                        text = { Text(stringResource(R.string.appsettings_color_view_mode_simplified)) }
                    )
                    AppTab(
                        selected = !simplifiedViewActive,
                        onClick = { controller.setSimplifiedViewActive(false) },
                        text = { Text(stringResource(R.string.appsettings_color_view_mode_advanced)) }
                    )
                }
            }
            if (simplifiedViewActive) {
                item {
                    SimplifiedColorSettingsList(
                        baseRampOverrides = baseRampOverrides,
                        onEditRamp = { ramp -> editingRamp = ramp },
                        onResetRamp = { ramp -> resettingRamp = ramp },
                        modifier = Modifier.padding(top = AppSpacing.sm)
                    )
                }
            } else {
                item {
                    SettingsSurfaceList {
                        visibleCategories.forEachIndexed { index, category ->
                            val isChanged = registry.tokensOf(category.key).any { it.id in changedTokenIds }
                            SettingsNavRow(
                                title = stringResource(category.labelRes),
                                subtitle = stringResource(category.descriptionRes),
                                onClick = { onNavigateToCategory(category.key) },
                                trailing = if (isChanged) {
                                    { CategoryChangedBadgeWithChevron() }
                                } else {
                                    null
                                }
                            )
                            if (index != visibleCategories.lastIndex) {
                                SettingsRowDivider()
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(AppSpacing.lg),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AppButton(
                        text = stringResource(R.string.appsettings_color_reset_all),
                        role = AppButtonRole.Neutral,
                        variant = AppButtonVariant.Text,
                        onClick = { showResetAllConfirm = true },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChangedBadgeWithChevron() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        ColorCustomizedBadge()
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun RowScope.ColorThemeExportImportActions(
    onExport: () -> Unit,
    onImport: () -> Unit,
    exportEnabled: Boolean
) {
    AppIconButton(
        icon = Icons.Filled.FileUpload,
        contentDescription = stringResource(R.string.appsettings_color_theme_import_button),
        role = AppButtonRole.Neutral,
        onClick = onImport,
    )
    AppIconButton(
        icon = Icons.Filled.FileDownload,
        contentDescription = stringResource(R.string.appsettings_color_theme_export_button),
        role = AppButtonRole.Neutral,
        enabled = exportEnabled,
        onClick = onExport,
    )
}
