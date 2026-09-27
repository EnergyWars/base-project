package com.wafflehq.lib.settings.encryption.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.lib.database.crypto.BackupPasswordStrength
import com.wafflehq.lib.database.crypto.PasswordStrength
import com.wafflehq.lib.database.state.ConversionUiState
import com.wafflehq.lib.navigation.settings.SettingsBanner
import com.wafflehq.lib.navigation.settings.SettingsDropdownField
import com.wafflehq.lib.navigation.settings.SettingsGroup
import com.wafflehq.lib.navigation.settings.SettingsRowDivider
import com.wafflehq.lib.navigation.settings.SettingsRowScaffold
import com.wafflehq.lib.navigation.settings.SettingsScaffold
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.encryption.BackupOnlyEncryptionResult
import com.wafflehq.lib.settings.encryption.BackupReplaceResult
import com.wafflehq.lib.settings.encryption.ChangePasswordResult
import com.wafflehq.lib.settings.encryption.EncryptionSettingsController
import com.wafflehq.lib.settings.encryption.PasswordReminderInterval
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.components.SecureScreenEffect
import com.wafflehq.lib.uicore.components.rememberGuardedDismiss
import com.wafflehq.lib.navigation.R as NavR
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.components.AppSwitch
import com.wafflehq.lib.uicore.components.AppCircularProgress

private val MIN_BACKUP_PASSWORD_LENGTH = BackupPasswordStrength.MIN_LENGTH

@Composable
fun EncryptionConfigScreen(
    onBack: () -> Unit,
    controller: EncryptionSettingsController
) {
    val isEncrypted = controller.isDatabaseEncrypted
    val hasBackupPassword = controller.hasBackupPassword
    val hasBackupOnlyEncryption by controller.hasBackupOnlyEncryption.collectAsStateWithLifecycle()
    val backupOnlyEncryptionState by controller.backupOnlyEncryptionState.collectAsStateWithLifecycle()
    val backupReplaceState by controller.backupReplaceState.collectAsStateWithLifecycle()
    val uiState by controller.uiState.collectAsStateWithLifecycle()
    val changePasswordState by controller.changePasswordState.collectAsStateWithLifecycle()
    val passwordTestResult by controller.passwordTestResult.collectAsStateWithLifecycle()
    val passwordReminderInterval by controller.passwordReminderInterval.collectAsStateWithLifecycle()
    var showWarningDialog by remember { mutableStateOf(false) }
    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showTestPasswordDialog by remember { mutableStateOf(false) }
    var showEnableBackupOnlyEncryptionDialog by remember { mutableStateOf(false) }
    var showDisableBackupOnlyEncryptionConfirm by remember { mutableStateOf(false) }
    var showReplaceBackupsOfferDialog by remember { mutableStateOf(false) }

    SecureScreenEffect()

    SettingsScaffold(
        title = stringResource(R.string.appsettings_encryption_config_title),
        onBack = onBack,
        backDescription = stringResource(NavR.string.navigation_navigate_back)
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                SettingsGroup {
                    SettingsRowScaffold(
                        title = stringResource(R.string.appsettings_encryption_status_title),
                        subtitle = stringResource(
                            if (isEncrypted) R.string.appsettings_encryption_status_encrypted
                            else R.string.appsettings_encryption_status_plain
                        )
                    ) {
                        Icon(
                            imageVector = if (isEncrypted) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    SettingsRowDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        AppButton(
                            text = stringResource(
                                if (isEncrypted) R.string.appsettings_encryption_action_decrypt
                                else R.string.appsettings_encryption_action_encrypt
                            ),
                            role = AppButtonRole.Primary,
                            enabled = uiState == ConversionUiState.Idle,
                            onClick = { showWarningDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (uiState == ConversionUiState.InProgress) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                AppCircularProgress()
                                Text(
                                    stringResource(R.string.appsettings_encryption_conversion_in_progress),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (isEncrypted && hasBackupPassword) {
                        SettingsRowDivider()
                        SettingsRowScaffold(
                            title = stringResource(R.string.appsettings_backup_password_change_action),
                            subtitle = stringResource(R.string.appsettings_backup_password_change_subtitle)
                        ) {
                            AppIconButton(
                                icon = Icons.Filled.Key,
                                contentDescription = null,
                                role = AppButtonRole.Primary,
                                onClick = { showChangePasswordDialog = true },
                            )
                        }
                        SettingsRowDivider()
                        SettingsRowScaffold(
                            title = stringResource(R.string.appsettings_backup_password_test_title),
                            subtitle = stringResource(R.string.appsettings_backup_password_test_subtitle)
                        ) {
                            AppButton(
                                text = stringResource(R.string.appsettings_backup_password_test_action),
                                role = AppButtonRole.Primary,
                                variant = AppButtonVariant.Text,
                                onClick = { showTestPasswordDialog = true },
                            )
                        }
                    }
                }
            }
            if (isEncrypted && hasBackupPassword) {
                item {
                    SettingsGroup(label = stringResource(R.string.appsettings_password_reminder_group_title)) {
                        PasswordReminderIntervalDropdown(
                            selected = passwordReminderInterval,
                            onSelected = controller::onPasswordReminderIntervalChanged
                        )
                    }
                }
            }
            if (!isEncrypted) {
                item {
                    SettingsGroup(label = stringResource(R.string.appsettings_backup_only_encryption_section_title)) {
                        SettingsRowScaffold(
                            title = stringResource(R.string.appsettings_backup_only_encryption_toggle),
                            subtitle = stringResource(R.string.appsettings_backup_only_encryption_toggle_desc)
                        ) {
                            AppSwitch(
                                checked = hasBackupOnlyEncryption,
                                onCheckedChange = { enabled ->
                                    if (enabled) showEnableBackupOnlyEncryptionDialog = true
                                    else showDisableBackupOnlyEncryptionConfirm = true
                                }
                            )
                        }
                    }
                }
            }
            item {
                Box(modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)) {
                    SettingsBanner(
                        icon = Icons.Filled.Info,
                        title = stringResource(R.string.appsettings_encryption_info_title),
                        text = stringResource(R.string.appsettings_encryption_info_message),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }

    if (showWarningDialog) {
        AppDialog(
            onDismissRequest = { showWarningDialog = false },
            title = { Text(stringResource(R.string.appsettings_encryption_warning_dialog_title)) },
            text = { Text(stringResource(R.string.appsettings_encryption_warning_dialog_message)) },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_encryption_warning_dialog_confirm),
                    role = AppButtonRole.Warning,
                    variant = AppButtonVariant.Text,
                    onClick = {
                        showWarningDialog = false
                        if (isEncrypted) {
                            controller.startConversion()
                        } else {
                            showSetPasswordDialog = true
                        }
                    },
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = { showWarningDialog = false },
                )
            }
        )
    }

    if (showSetPasswordDialog) {
        BackupPasswordDialog(
            title = stringResource(R.string.appsettings_backup_password_set_title),
            message = stringResource(R.string.appsettings_backup_password_set_message),
            confirmLabel = stringResource(R.string.appsettings_backup_password_set_confirm),
            onDismiss = { showSetPasswordDialog = false },
            onConfirm = { password ->
                showSetPasswordDialog = false
                controller.startConversion(password.toCharArray())
            }
        )
    }

    if (showChangePasswordDialog) {
        BackupPasswordDialog(
            title = stringResource(R.string.appsettings_backup_password_change_action),
            message = stringResource(R.string.appsettings_backup_password_change_message),
            confirmLabel = stringResource(R.string.appsettings_confirm),
            isLoading = changePasswordState == ChangePasswordResult.InProgress,
            onDismiss = {
                showChangePasswordDialog = false
                controller.dismissChangePasswordResult()
            },
            onConfirm = { password -> controller.changeBackupPassword(password) }
        )

        LaunchedEffect(changePasswordState) {
            if (changePasswordState == ChangePasswordResult.Success) {
                showChangePasswordDialog = false
                controller.dismissChangePasswordResult()
                showReplaceBackupsOfferDialog = true
            } else if (changePasswordState == ChangePasswordResult.Error) {
                showChangePasswordDialog = false
            }
        }
    }

    if (showReplaceBackupsOfferDialog) {
        AppDialog(
            onDismissRequest = { showReplaceBackupsOfferDialog = false },
            title = { Text(stringResource(R.string.appsettings_backup_replace_offer_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text(stringResource(R.string.appsettings_backup_replace_offer_message), style = MaterialTheme.typography.bodyMedium)
                    if (backupReplaceState == BackupReplaceResult.InProgress) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            AppCircularProgress(modifier = Modifier.size(28.dp))
                        }
                    }
                }
            },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_backup_replace_offer_confirm),
                    role = AppButtonRole.Warning,
                    variant = AppButtonVariant.Text,
                    enabled = backupReplaceState != BackupReplaceResult.InProgress,
                    onClick = { controller.replaceAllBackupsWithFreshOne() },
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_backup_replace_offer_skip),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    enabled = backupReplaceState != BackupReplaceResult.InProgress,
                    onClick = { showReplaceBackupsOfferDialog = false },
                )
            }
        )

        LaunchedEffect(backupReplaceState) {
            if (backupReplaceState == BackupReplaceResult.Success) {
                showReplaceBackupsOfferDialog = false
                controller.dismissBackupReplaceResult()
            } else if (backupReplaceState == BackupReplaceResult.Error) {
                showReplaceBackupsOfferDialog = false
            }
        }
    }

    if (backupReplaceState == BackupReplaceResult.Error) {
        AppDialog(
            onDismissRequest = { controller.dismissBackupReplaceResult() },
            title = { Text(stringResource(R.string.appsettings_backup_replace_offer_title)) },
            text = { Text(stringResource(R.string.appsettings_backup_replace_offer_error)) },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_confirm),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    onClick = { controller.dismissBackupReplaceResult() },
                )
            }
        )
    }

    if (showTestPasswordDialog) {
        PasswordTestDialog(
            title = stringResource(R.string.appsettings_backup_password_test_title),
            message = stringResource(R.string.appsettings_backup_password_test_message),
            result = passwordTestResult,
            onTest = controller::testBackupPassword,
            onResultDismissed = controller::dismissPasswordTestResult,
            onDismiss = {
                showTestPasswordDialog = false
                controller.dismissPasswordTestResult()
            }
        )
    }

    if (changePasswordState == ChangePasswordResult.Error) {
        AppDialog(
            onDismissRequest = { controller.dismissChangePasswordResult() },
            title = { Text(stringResource(R.string.appsettings_backup_password_change_action)) },
            text = { Text(stringResource(R.string.appsettings_backup_password_change_error)) },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_confirm),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    onClick = { controller.dismissChangePasswordResult() },
                )
            }
        )
    }

    if (showEnableBackupOnlyEncryptionDialog) {
        BackupPasswordDialog(
            title = stringResource(R.string.appsettings_backup_only_encryption_enable_dialog_title),
            message = stringResource(R.string.appsettings_backup_only_encryption_enable_dialog_message),
            confirmLabel = stringResource(R.string.appsettings_backup_only_encryption_enable_confirm),
            isLoading = backupOnlyEncryptionState == BackupOnlyEncryptionResult.InProgress,
            onDismiss = {
                showEnableBackupOnlyEncryptionDialog = false
                controller.dismissBackupOnlyEncryptionResult()
            },
            onConfirm = { password -> controller.enableBackupOnlyEncryption(password) }
        )

        LaunchedEffect(backupOnlyEncryptionState) {
            if (backupOnlyEncryptionState == BackupOnlyEncryptionResult.Success) {
                showEnableBackupOnlyEncryptionDialog = false
                controller.dismissBackupOnlyEncryptionResult()
            } else if (backupOnlyEncryptionState == BackupOnlyEncryptionResult.Error) {
                showEnableBackupOnlyEncryptionDialog = false
            }
        }
    }

    if (backupOnlyEncryptionState == BackupOnlyEncryptionResult.Error) {
        AppDialog(
            onDismissRequest = { controller.dismissBackupOnlyEncryptionResult() },
            title = { Text(stringResource(R.string.appsettings_backup_only_encryption_toggle)) },
            text = { Text(stringResource(R.string.appsettings_backup_only_encryption_error)) },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_confirm),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    onClick = { controller.dismissBackupOnlyEncryptionResult() },
                )
            }
        )
    }

    if (showDisableBackupOnlyEncryptionConfirm) {
        AppDialog(
            onDismissRequest = { showDisableBackupOnlyEncryptionConfirm = false },
            title = { Text(stringResource(R.string.appsettings_backup_only_encryption_disable_confirm_title)) },
            text = { Text(stringResource(R.string.appsettings_backup_only_encryption_disable_confirm_message)) },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_confirm),
                    role = AppButtonRole.Warning,
                    variant = AppButtonVariant.Text,
                    onClick = {
                        showDisableBackupOnlyEncryptionConfirm = false
                        controller.disableBackupOnlyEncryption()
                    },
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = { showDisableBackupOnlyEncryptionConfirm = false },
                )
            }
        )
    }

}

@Composable
private fun PasswordReminderIntervalDropdown(
    selected: PasswordReminderInterval,
    onSelected: (PasswordReminderInterval) -> Unit
) {
    val options = listOf(
        PasswordReminderInterval.NEVER to R.string.appsettings_password_reminder_interval_never,
        PasswordReminderInterval.EVERY_LAUNCH to R.string.appsettings_password_reminder_interval_every_launch,
        PasswordReminderInterval.DAILY to R.string.appsettings_password_reminder_interval_daily,
        PasswordReminderInterval.WEEKLY to R.string.appsettings_password_reminder_interval_weekly,
        PasswordReminderInterval.MONTHLY to R.string.appsettings_password_reminder_interval_monthly,
    )
    val selectedIndex = options.indexOfFirst { it.first == selected }.coerceAtLeast(0)
    SettingsDropdownField(
        label = stringResource(R.string.appsettings_password_reminder_interval_label),
        value = stringResource(options[selectedIndex].second),
        options = options.map { stringResource(it.second) },
        selectedIndex = selectedIndex,
        onSelect = { onSelected(options[it].first) }
    )
}

@Composable
private fun passwordStrengthLabel(strength: PasswordStrength): String = stringResource(
    when (strength) {
        PasswordStrength.WEAK -> R.string.appsettings_backup_password_strength_weak
        PasswordStrength.MEDIUM -> R.string.appsettings_backup_password_strength_medium
        PasswordStrength.STRONG -> R.string.appsettings_backup_password_strength_strong
    }
)

@Composable
private fun BackupPasswordDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean = false
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var hasChanges by remember { mutableStateOf(false) }
    val tooShort = password.isNotEmpty() && password.length < MIN_BACKUP_PASSWORD_LENGTH
    val mismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val strength = remember(password) { BackupPasswordStrength.evaluate(password) }
    val isWeak = password.isNotEmpty() && !tooShort && strength == PasswordStrength.WEAK
    val isValid = password.length >= MIN_BACKUP_PASSWORD_LENGTH && password == confirmPassword
    val guardedDismiss = rememberGuardedDismiss(hasChanges = hasChanges, onDismiss = onDismiss)

    AppDialog(
        onDismissRequest = guardedDismiss,
        properties = DialogProperties(securePolicy = SecureFlagPolicy.SecureOn),
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text(message, style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(R.string.appsettings_backup_password_not_resettable_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AppTextField(
                    value = password,
                    onValueChange = { password = it; hasChanges = true },
                    label = { Text(stringResource(R.string.appsettings_backup_password_field_label)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = tooShort,
                    supportingText = if (tooShort) {
                        { Text(stringResource(R.string.appsettings_backup_password_too_short_error, MIN_BACKUP_PASSWORD_LENGTH)) }
                    } else if (password.isNotEmpty()) {
                        { Text(passwordStrengthLabel(strength)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isWeak) {
                    Text(
                        stringResource(R.string.appsettings_backup_password_weak_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                AppTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; hasChanges = true },
                    label = { Text(stringResource(R.string.appsettings_backup_password_confirm_field_label)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = mismatch,
                    supportingText = if (mismatch) {
                        { Text(stringResource(R.string.appsettings_backup_password_mismatch_error)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AppCircularProgress(modifier = Modifier.size(28.dp))
                    }
                }
            }
        },
        confirmButton = {
            AppButton(
                text = confirmLabel,
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Text,
                enabled = isValid && !isLoading,
                onClick = { onConfirm(password) },
            )
        },
        dismissButton = {
            AppButton(
                text = stringResource(UiCoreR.string.uicore_cancel),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                enabled = !isLoading,
                onClick = onDismiss,
            )
        }
    )
}
