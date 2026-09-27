package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.uicore.R
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.theme.AppSpacing

object SaveDiscardChangesDialogTestTags {
    const val DIALOG = "save_discard_changes_dialog"
    const val SAVE = "save_discard_changes_save"
    const val DISCARD = "save_discard_changes_discard"
    const val CANCEL = "save_discard_changes_cancel"
}

@Composable
fun SaveDiscardChangesDialog(
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit,
    extraText: String? = null,
) {
    AppDialog(
        onDismissRequest = onDismiss,
        properties = AppDialogDefaults.properties,
        modifier = Modifier.testTag(SaveDiscardChangesDialogTestTags.DIALOG),
        title = { Text(stringResource(R.string.uicore_save_changes_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text(stringResource(R.string.uicore_save_changes_message))
                if (extraText != null) {
                    Text(
                        text = extraText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.uicore_save),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Text,
                onClick = onSave,
                modifier = Modifier.testTag(SaveDiscardChangesDialogTestTags.SAVE),
            )
        },
        dismissButton = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                AppButton(
                    text = stringResource(R.string.uicore_discard_changes_confirm),
                    role = AppButtonRole.Error,
                    variant = AppButtonVariant.Text,
                    onClick = onDiscard,
                    modifier = Modifier.testTag(SaveDiscardChangesDialogTestTags.DISCARD),
                )
                AppButton(
                    text = stringResource(R.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = onDismiss,
                    modifier = Modifier.testTag(SaveDiscardChangesDialogTestTags.CANCEL),
                )
            }
        },
    )
}
