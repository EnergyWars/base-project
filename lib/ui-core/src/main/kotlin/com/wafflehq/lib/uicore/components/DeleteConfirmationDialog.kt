package com.wafflehq.lib.uicore.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.uicore.R
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant

@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        properties = AppDialogDefaults.properties,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.uicore_delete),
                role = AppButtonRole.Error,
                variant = AppButtonVariant.Text,
                onClick = onConfirm
            )
        },
        dismissButton = {
            AppButton(
                text = stringResource(R.string.uicore_cancel),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                onClick = onDismiss
            )
        }
    )
}
