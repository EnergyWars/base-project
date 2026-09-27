package com.wafflehq.lib.settings.colors.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.lib.navigation.settings.SettingsSurfaceList
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.colors.ColorSettingsController
import com.wafflehq.lib.settings.colors.ColorThemeInfo
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.components.NameInputDialog
import com.wafflehq.lib.uicore.menu.AppDropdownMenu
import com.wafflehq.lib.uicore.menu.AppDropdownMenuItem
import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.components.AppCheckbox

object ColorThemeTestTags {
    const val SELECTOR = "color_theme_selector"
    const val MENU = "color_theme_menu"
    const val NEW = "color_theme_new"
    const val DUPLICATE = "color_theme_duplicate"
    const val RENAME = "color_theme_rename"
    const val DELETE = "color_theme_delete"
    const val NAME_REQUIRED_DIALOG = "color_theme_name_required"
    const val STANDARD_ENTRY = "color_theme_entry_standard"
    fun entry(id: String) = "color_theme_entry_$id"
    fun exportEntry(id: String) = "color_theme_export_$id"
    const val EXPORT_CONFIRM = "color_theme_export_confirm"
    const val IMPORT_CONFIRM = "color_theme_import_confirm"
    fun importEntry(index: Int) = "color_theme_import_$index"
}

private enum class ThemeDialog { NEW, DUPLICATE, RENAME, DELETE }

@Composable
fun ColorThemeNameRequestHost(controller: ColorSettingsController) {
    val requested by controller.themeNameRequested.collectAsStateWithLifecycle()
    if (requested) {
        NameInputDialog(
            title = stringResource(R.string.appsettings_color_theme_name_required_title),
            message = stringResource(R.string.appsettings_color_theme_name_required_message),
            label = stringResource(R.string.appsettings_color_theme_name_label),
            initialName = "",
            confirmLabel = stringResource(R.string.appsettings_color_theme_name_create),
            onConfirm = { controller.createThemeForEdit(it) },
            onDismiss = { controller.dismissThemeNameRequest() }
        )
    }
}

@Composable
fun ColorThemeSelector(
    controller: ColorSettingsController,
    modifier: Modifier = Modifier
) {
    val themes by controller.themes.collectAsStateWithLifecycle()
    val activeId by controller.activeThemeId.collectAsStateWithLifecycle()
    val active = themes.firstOrNull { it.id == activeId }
    val standardName = stringResource(R.string.appsettings_color_theme_standard_name)
    var listExpanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<ThemeDialog?>(null) }

    when (dialog) {
        ThemeDialog.NEW -> NameInputDialog(
            title = stringResource(R.string.appsettings_color_theme_new),
            label = stringResource(R.string.appsettings_color_theme_name_label),
            initialName = "",
            confirmLabel = stringResource(R.string.appsettings_color_theme_name_create),
            onConfirm = { controller.createTheme(it); dialog = null },
            onDismiss = { dialog = null }
        )
        ThemeDialog.DUPLICATE -> NameInputDialog(
            title = stringResource(R.string.appsettings_color_theme_duplicate),
            label = stringResource(R.string.appsettings_color_theme_name_label),
            initialName = active?.name.orEmpty(),
            confirmLabel = stringResource(R.string.appsettings_color_theme_name_create),
            onConfirm = { controller.createTheme(it, copyFromActive = true); dialog = null },
            onDismiss = { dialog = null }
        )
        ThemeDialog.RENAME -> active?.let { theme ->
            NameInputDialog(
                title = stringResource(R.string.appsettings_color_theme_rename),
                label = stringResource(R.string.appsettings_color_theme_name_label),
                initialName = theme.name,
                confirmLabel = stringResource(UiCoreR.string.uicore_save),
                onConfirm = { controller.renameTheme(theme.id, it); dialog = null },
                onDismiss = { dialog = null }
            )
        }
        ThemeDialog.DELETE -> active?.let { theme ->
            AppDialog(
                onDismissRequest = { dialog = null },
                title = { Text(stringResource(R.string.appsettings_color_theme_delete_confirm_title)) },
                text = { Text(stringResource(R.string.appsettings_color_theme_delete_confirm_message, theme.name)) },
                confirmButton = {
                    AppButton(
                        text = stringResource(R.string.appsettings_color_theme_delete),
                        role = AppButtonRole.Error,
                        variant = AppButtonVariant.Text,
                        onClick = { controller.deleteTheme(theme.id); dialog = null },
                    )
                },
                dismissButton = {
                    AppButton(
                        text = stringResource(UiCoreR.string.uicore_cancel),
                        role = AppButtonRole.Neutral,
                        variant = AppButtonVariant.Text,
                        onClick = { dialog = null },
                    )
                }
            )
        }
        null -> Unit
    }

    Column(modifier = modifier.fillMaxWidth()) {
        SettingsSurfaceList {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.appsettings_color_theme_section_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppButton(
                            text = active?.name ?: standardName,
                            role = AppButtonRole.Neutral,
                            variant = AppButtonVariant.Text,
                            onClick = { listExpanded = true },
                            modifier = Modifier.testTag(ColorThemeTestTags.SELECTOR),
                        )
                        AppDropdownMenu(expanded = listExpanded, onDismissRequest = { listExpanded = false }) {
                            AppDropdownMenuItem(
                                text = { Text(standardName) },
                                onClick = { controller.activateTheme(null); listExpanded = false },
                                selected = active == null,
                                modifier = Modifier.testTag(ColorThemeTestTags.STANDARD_ENTRY)
                            )
                            themes.forEach { theme ->
                                AppDropdownMenuItem(
                                    text = { Text(theme.name) },
                                    onClick = { controller.activateTheme(theme.id); listExpanded = false },
                                    selected = active?.id == theme.id,
                                    modifier = Modifier.testTag(ColorThemeTestTags.entry(theme.id))
                                )
                            }
                        }
                    }
                }
                Column {
                    AppIconButton(
                        icon = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.appsettings_color_theme_menu),
                        role = AppButtonRole.Neutral,
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag(ColorThemeTestTags.MENU),
                    )
                    AppDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        AppDropdownMenuItem(
                            text = { Text(stringResource(R.string.appsettings_color_theme_new)) },
                            onClick = { menuExpanded = false; dialog = ThemeDialog.NEW },
                            modifier = Modifier.testTag(ColorThemeTestTags.NEW)
                        )
                        if (active != null) {
                            AppDropdownMenuItem(
                                text = { Text(stringResource(R.string.appsettings_color_theme_duplicate)) },
                                onClick = { menuExpanded = false; dialog = ThemeDialog.DUPLICATE },
                                modifier = Modifier.testTag(ColorThemeTestTags.DUPLICATE)
                            )
                            AppDropdownMenuItem(
                                text = { Text(stringResource(R.string.appsettings_color_theme_rename)) },
                                onClick = { menuExpanded = false; dialog = ThemeDialog.RENAME },
                                modifier = Modifier.testTag(ColorThemeTestTags.RENAME)
                            )
                            AppDropdownMenuItem(
                                text = { Text(stringResource(R.string.appsettings_color_theme_delete)) },
                                onClick = { menuExpanded = false; dialog = ThemeDialog.DELETE },
                                modifier = Modifier.testTag(ColorThemeTestTags.DELETE)
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = stringResource(
                if (active == null) R.string.appsettings_color_theme_standard_hint
                else R.string.appsettings_color_theme_custom_hint
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)
        )
    }
}

@Composable
fun ColorThemeExportDialog(
    themes: List<ColorThemeInfo>,
    initiallySelected: Set<String>,
    onConfirm: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(initiallySelected.filter { id -> themes.any { it.id == id } }.toSet()) }
    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.appsettings_color_theme_export_title)) },
        text = {
            LazyColumn {
                items(themes, key = { it.id }) { theme ->
                    val checked = theme.id in selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = if (checked) selected - theme.id else selected + theme.id }
                            .testTag(ColorThemeTestTags.exportEntry(theme.id)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppCheckbox(checked = checked, onCheckedChange = null)
                        Text(theme.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = AppSpacing.xs))
                    }
                }
            }
        },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.appsettings_color_theme_export_confirm),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Text,
                enabled = selected.isNotEmpty(),
                onClick = { onConfirm(selected) },
                modifier = Modifier.testTag(ColorThemeTestTags.EXPORT_CONFIRM),
            )
        },
        dismissButton = {
            AppButton(
                text = stringResource(UiCoreR.string.uicore_cancel),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                onClick = onDismiss,
            )
        }
    )
}
