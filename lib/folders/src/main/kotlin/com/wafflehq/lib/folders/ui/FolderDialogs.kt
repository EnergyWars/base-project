package com.wafflehq.lib.folders.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.wafflehq.lib.folders.FolderDeletionAction
import com.wafflehq.lib.folders.FolderNames
import com.wafflehq.lib.folders.R
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppCard
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.components.AppDialogDefaults
import com.wafflehq.lib.uicore.components.AppRadioButton
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.components.emptyAwareTextFieldColors
import com.wafflehq.lib.uicore.components.rememberGuardedDismiss
import com.wafflehq.lib.uicore.theme.AppSpacing

@Composable
fun FolderNameDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    label: String = stringResource(R.string.folder_name_label),
    confirmLabel: String = stringResource(UiCoreR.string.uicore_save),
    extraContent: @Composable ColumnScope.(markChanged: () -> Unit) -> Unit = {}
) {
    var name by remember { mutableStateOf(initialName) }
    var hasChanges by remember { mutableStateOf(false) }
    val guardedDismiss = rememberGuardedDismiss(hasChanges = hasChanges, onDismiss = onDismiss)
    val normalized = FolderNames.normalize(name)
    AppDialog(
        onDismissRequest = guardedDismiss,
        properties = AppDialogDefaults.properties,
        icon = {
            Icon(
                Icons.Default.CreateNewFolder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it; hasChanges = true },
                    label = { Text(label) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = emptyAwareTextFieldColors(isEmpty = name.isEmpty())
                )
                extraContent { hasChanges = true }
            }
        },
        confirmButton = {
            AppButton(
                text = confirmLabel,
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                enabled = normalized != null,
                onClick = { normalized?.let(onConfirm) },
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

@Composable
fun FolderDeleteDialog(
    onConfirm: (FolderDeletionAction) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.folder_delete_confirm_title)
) {
    var selected by remember { mutableStateOf(FolderDeletionAction.DELETE_CONTENTS) }
    AppDialog(
        onDismissRequest = onDismiss,
        properties = AppDialogDefaults.properties,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text(
                    text = stringResource(R.string.folder_delete_choice_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FolderDeleteOptionRow(
                    selected = selected == FolderDeletionAction.MOVE_CONTENTS_UP,
                    title = stringResource(R.string.folder_delete_option_move_up),
                    subtitle = stringResource(R.string.folder_delete_option_move_up_hint),
                    onClick = { selected = FolderDeletionAction.MOVE_CONTENTS_UP }
                )
                FolderDeleteOptionRow(
                    selected = selected == FolderDeletionAction.DELETE_CONTENTS,
                    title = stringResource(R.string.folder_delete_option_delete_all),
                    subtitle = stringResource(R.string.folder_delete_option_delete_all_hint),
                    onClick = { selected = FolderDeletionAction.DELETE_CONTENTS }
                )
            }
        },
        confirmButton = {
            AppButton(
                text = stringResource(UiCoreR.string.uicore_delete),
                role = AppButtonRole.Error,
                variant = AppButtonVariant.Tonal,
                onClick = { onConfirm(selected) },
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

@Composable
private fun FolderDeleteOptionRow(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) colors.primaryContainer else Color.Transparent,
        label = "deleteOptionBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            colors.primary
        } else {
            colors.outline.copy(alpha = FolderListDefaults.cardBorderAlpha)
        },
        label = "deleteOptionBorder"
    )
    AppCard(
        containerColor = backgroundColor,
        borderColor = borderColor,
        borderWidth = FolderListDefaults.cardBorderWidth,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = onClick)
                .padding(vertical = AppSpacing.sm, horizontal = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppRadioButton(selected = selected, onClick = onClick)
            Column(modifier = Modifier.padding(start = AppSpacing.xs, end = AppSpacing.sm)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) colors.onPrimaryContainer else colors.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
                )
            }
        }
    }
}
