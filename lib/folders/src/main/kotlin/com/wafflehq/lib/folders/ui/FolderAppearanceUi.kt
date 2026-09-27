package com.wafflehq.lib.folders.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.folders.FolderAppearance
import com.wafflehq.lib.folders.FolderAppearanceKey
import com.wafflehq.lib.folders.FolderAppearanceStore
import com.wafflehq.lib.folders.R
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.components.AppDialogDefaults
import com.wafflehq.lib.uicore.components.IconPickerGrid
import com.wafflehq.lib.uicore.components.rememberGuardedDismiss
import com.wafflehq.lib.uicore.theme.AppSpacing

interface FolderColorPicker {
    @Composable
    fun Content(
        title: String,
        initialColor: Color,
        onColorSelected: (Color) -> Unit,
        onDismiss: () -> Unit
    )
}

val LocalFolderAppearanceStore = staticCompositionLocalOf<FolderAppearanceStore?> { null }

val LocalFolderColorPicker = compositionLocalOf<FolderColorPicker?> { null }

val LocalFolderAppearanceScope = compositionLocalOf<String?> { null }

object FolderAppearanceTestTags {
    const val DIALOG_COLOR_BUTTON = "folder_appearance_color_button"
    const val DIALOG_RESET_BUTTON = "folder_appearance_reset_button"
    const val DIALOG_SAVE_BUTTON = "folder_appearance_save_button"
    const val DIALOG_PREVIEW = "folder_appearance_preview"
    const val ICON_TILE = "folder_icon_tile"
}

@Composable
fun rememberFolderAppearance(key: FolderAppearanceKey?): FolderAppearance {
    val store = LocalFolderAppearanceStore.current
    val all = store?.appearances?.collectAsState()?.value
    return if (key == null || all == null) FolderAppearance() else all[key] ?: FolderAppearance()
}

@Composable
fun rememberCanEditFolderAppearance(key: FolderAppearanceKey?): Boolean =
    key != null && LocalFolderAppearanceStore.current != null

@Composable
fun rememberFolderTint(key: FolderAppearanceKey?, default: Color): Color =
    rememberFolderAppearance(key).colorArgb?.let { Color(it) } ?: default

@Composable
fun FolderAppearanceEditorDialog(
    key: FolderAppearanceKey?,
    defaultIcon: ImageVector,
    defaultColor: Color,
    onDismiss: () -> Unit
) {
    val store = LocalFolderAppearanceStore.current
    val current = rememberFolderAppearance(key)
    if (store == null || key == null) return
    FolderAppearanceDialog(
        current = current,
        defaultIcon = defaultIcon,
        defaultColor = defaultColor,
        onSave = { appearance ->
            store.save(key, appearance)
            onDismiss()
        },
        onDismiss = onDismiss
    )
}

@Composable
fun FolderAppearanceDialog(
    current: FolderAppearance,
    defaultIcon: ImageVector,
    defaultColor: Color,
    onSave: (FolderAppearance) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember(current) { mutableStateOf(current) }
    var showColorPicker by remember { mutableStateOf(false) }
    val colorPicker = LocalFolderColorPicker.current
    val previewColor = draft.colorArgb?.let { Color(it) } ?: defaultColor
    val previewIcon = FolderIconCatalog.resolve(draft.iconKey) ?: defaultIcon
    val guardedDismiss = rememberGuardedDismiss(hasChanges = draft != current, onDismiss = onDismiss)
    AppDialog(
        onDismissRequest = guardedDismiss,
        properties = AppDialogDefaults.properties,
        icon = {
            Icon(
                Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(stringResource(R.string.folder_appearance_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    FolderIconTile(
                        accent = previewColor,
                        icon = previewIcon,
                        size = FolderListDefaults.previewTileSize,
                        glyphSize = FolderListDefaults.previewGlyphSize,
                        modifier = Modifier.testTag(FolderAppearanceTestTags.DIALOG_PREVIEW)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        if (colorPicker != null) {
                            AppButton(
                                text = stringResource(R.string.folder_appearance_color),
                                role = AppButtonRole.Primary,
                                variant = AppButtonVariant.Tonal,
                                onClick = { showColorPicker = true },
                                modifier = Modifier.testTag(FolderAppearanceTestTags.DIALOG_COLOR_BUTTON)
                            )
                        }
                        AppButton(
                            text = stringResource(R.string.folder_appearance_reset),
                            role = AppButtonRole.Neutral,
                            variant = AppButtonVariant.Text,
                            enabled = !draft.isDefault,
                            onClick = { draft = FolderAppearance() },
                            modifier = Modifier.testTag(FolderAppearanceTestTags.DIALOG_RESET_BUTTON)
                        )
                    }
                }
                IconPickerGrid(
                    options = FolderIconCatalog.pickerOptions(),
                    selectedKey = draft.iconKey,
                    onSelect = { key -> draft = draft.copy(iconKey = key) },
                    cellSize = FolderListDefaults.iconPickerCellSize,
                    gridHeight = FolderListDefaults.iconPickerGridHeight
                )
            }
        },
        confirmButton = {
            AppButton(
                text = stringResource(UiCoreR.string.uicore_save),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                onClick = { onSave(draft) },
                modifier = Modifier.testTag(FolderAppearanceTestTags.DIALOG_SAVE_BUTTON)
            )
        },
        dismissButton = {
            AppButton(
                text = stringResource(UiCoreR.string.uicore_cancel),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                onClick = onDismiss
            )
        }
    )
    if (showColorPicker && colorPicker != null) {
        colorPicker.Content(
            title = stringResource(R.string.folder_appearance_color_title),
            initialColor = previewColor,
            onColorSelected = { color ->
                draft = draft.copy(colorArgb = color.toArgb())
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}
