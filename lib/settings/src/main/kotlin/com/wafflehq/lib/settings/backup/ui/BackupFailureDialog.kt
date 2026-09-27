package com.wafflehq.lib.settings.backup.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppDialog

@Composable
fun BackupFailureDialog(onRetryNow: () -> Unit, onDismiss: () -> Unit) {
    AppDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.CloudOff, contentDescription = null) },
        title = { Text(stringResource(R.string.appsettings_backup_failure_dialog_title)) },
        text = { Text(stringResource(R.string.appsettings_backup_failure_dialog_message)) },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.appsettings_backup_failure_action_retry),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Text,
                onClick = onRetryNow,
            )
        },
        dismissButton = {
            AppButton(
                text = stringResource(R.string.appsettings_backup_failure_action_ignore),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                onClick = onDismiss,
            )
        }
    )
}
