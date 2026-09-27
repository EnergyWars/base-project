package com.wafflehq.lib.settings.encryption.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.theme.AppSpacing

@Composable
fun EncryptionBackupNagDialog(
    onSetupDrive: () -> Unit,
    onManualBackup: () -> Unit,
    onHandleMyself: () -> Unit
) {
    AppDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        icon = {
            Icon(
                Icons.Default.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                stringResource(R.string.appsettings_encryption_backup_nag_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                stringResource(R.string.appsettings_encryption_backup_nag_message),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                AppButton(
                    text = stringResource(R.string.appsettings_encryption_backup_nag_action_setup_drive),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    onClick = onSetupDrive,
                    modifier = Modifier.fillMaxWidth(),
                )
                AppButton(
                    text = stringResource(R.string.appsettings_encryption_backup_nag_action_manual_backup),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Outlined,
                    onClick = onManualBackup,
                    modifier = Modifier.fillMaxWidth(),
                )
                AppButton(
                    text = stringResource(R.string.appsettings_encryption_backup_nag_action_handle_myself),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = onHandleMyself,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    )
}
