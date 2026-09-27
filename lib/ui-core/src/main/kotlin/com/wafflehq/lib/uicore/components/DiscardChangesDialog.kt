package com.wafflehq.lib.uicore.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.uicore.R
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant

@Composable
fun DiscardChangesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        properties = AppDialogDefaults.properties,
        title = { Text(stringResource(R.string.uicore_discard_changes_title)) },
        text = { Text(stringResource(R.string.uicore_discard_changes_message)) },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.uicore_discard_changes_confirm),
                role = AppButtonRole.Error,
                variant = AppButtonVariant.Text,
                onClick = onConfirm,
            )
        },
        dismissButton = {
            AppButton(
                text = stringResource(R.string.uicore_cancel),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                onClick = onDismiss,
            )
        }
    )
}

@Composable
fun rememberGuardedDismiss(hasChanges: Boolean, onDismiss: () -> Unit): () -> Unit {
    var showConfirm by remember { mutableStateOf(false) }
    if (showConfirm) {
        DiscardChangesDialog(
            onConfirm = {
                showConfirm = false
                onDismiss()
            },
            onDismiss = { showConfirm = false }
        )
    }
    return remember(hasChanges, onDismiss) { { if (hasChanges) showConfirm = true else onDismiss() } }
}
