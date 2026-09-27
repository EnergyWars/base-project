package com.wafflehq.lib.settings.encryption.ui

import com.wafflehq.lib.uicore.R as UiCoreR
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.database.conversion.ConversionFailureReason
import com.wafflehq.lib.database.state.ConversionUiState
import com.wafflehq.lib.database.state.EncryptionConversionBus
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppDialog

@Composable
fun EncryptionConversionStatusDialogs(
    uiState: ConversionUiState,
    onRestartNow: () -> Unit
) {
    if (uiState is ConversionUiState.SuccessAwaitingRestart) {
        AppDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.appsettings_encryption_success_title)) },
            text = { Text(stringResource(R.string.appsettings_encryption_success_message)) },
            confirmButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_ok),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    onClick = onRestartNow,
                )
            }
        )
    }

    val errorState = uiState as? ConversionUiState.Error
    if (errorState != null) {
        val dismiss = { EncryptionConversionBus.update(ConversionUiState.Idle) }
        AppDialog(
            onDismissRequest = dismiss,
            title = { Text(stringResource(R.string.appsettings_encryption_error_title)) },
            text = {
                Text(
                    stringResource(
                        if (errorState.reason == ConversionFailureReason.INTEGRITY_CHECK_FAILED ||
                            errorState.reason == ConversionFailureReason.ROW_COUNT_MISMATCH
                        ) {
                            R.string.appsettings_encryption_error_verification_message
                        } else {
                            R.string.appsettings_encryption_error_export_message
                        }
                    )
                )
            },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_confirm),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    onClick = dismiss,
                )
            }
        )
    }
}
