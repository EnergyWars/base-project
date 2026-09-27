package com.wafflehq.lib.settings.encryption.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.R as UiCoreR
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.encryption.PasswordTestResult
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.components.AppTextField
import kotlinx.coroutines.delay
import com.wafflehq.lib.uicore.components.AppCircularProgress

private const val AUTO_CLOSE_DELAY_MS = 1000L

@Composable
fun PasswordTestDialog(
    title: String,
    message: String,
    result: PasswordTestResult,
    onTest: (String) -> Unit,
    onResultDismissed: () -> Unit,
    onDismiss: () -> Unit,
) {
    var password by remember { mutableStateOf("") }
    val isChecking = result == PasswordTestResult.Checking

    LaunchedEffect(result) {
        if (result == PasswordTestResult.Correct) {
            delay(AUTO_CLOSE_DELAY_MS)
            onDismiss()
        }
    }

    AppDialog(
        onDismissRequest = { if (!isChecking) onDismiss() },
        properties = DialogProperties(securePolicy = SecureFlagPolicy.SecureOn),
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text(message, style = MaterialTheme.typography.bodyMedium)
                AppTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (result != PasswordTestResult.Idle) onResultDismissed()
                    },
                    label = { Text(stringResource(R.string.appsettings_backup_password_field_label)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    enabled = !isChecking,
                    modifier = Modifier.fillMaxWidth()
                )
                when (result) {
                    PasswordTestResult.Checking -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AppCircularProgress(modifier = Modifier.size(28.dp))
                    }
                    PasswordTestResult.Correct -> Text(
                        stringResource(R.string.appsettings_backup_password_test_correct),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    PasswordTestResult.Incorrect -> Text(
                        stringResource(R.string.appsettings_backup_password_test_incorrect),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    PasswordTestResult.Idle -> {}
                }
            }
        },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.appsettings_backup_password_test_action),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Text,
                enabled = password.isNotEmpty() && !isChecking,
                onClick = { onTest(password) },
            )
        },
        dismissButton = {
            AppButton(
                text = stringResource(UiCoreR.string.uicore_close),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                enabled = !isChecking,
                onClick = onDismiss,
            )
        }
    )
}
