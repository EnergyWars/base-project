package com.wafflehq.lib.settings.backup.ui

import android.accounts.AccountManager
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.lib.backupcore.BackupRetentionMode
import com.wafflehq.lib.navigation.settings.ListCard
import com.wafflehq.lib.navigation.settings.ListEmpty
import com.wafflehq.lib.navigation.settings.ListHead
import com.wafflehq.lib.navigation.settings.ListItemRow
import com.wafflehq.lib.navigation.settings.ListSearchField
import com.wafflehq.lib.navigation.settings.SettingsBanner
import com.wafflehq.lib.navigation.settings.SettingsDropdownField
import com.wafflehq.lib.navigation.settings.SettingsGroup
import com.wafflehq.lib.navigation.settings.SettingsNavRow
import com.wafflehq.lib.navigation.settings.SettingsRowDivider
import com.wafflehq.lib.navigation.settings.SettingsRowScaffold
import com.wafflehq.lib.navigation.settings.SettingsScaffold
import com.wafflehq.lib.navigation.settings.SettingsSwitchRow
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.backup.BackupMessage
import com.wafflehq.lib.settings.backup.BackupMeta
import com.wafflehq.lib.settings.backup.BackupSettingsController
import com.wafflehq.lib.settings.core.settingsText
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.components.AppEmptyState
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.components.SecureScreenEffect
import com.wafflehq.lib.uicore.components.rememberGuardedDismiss
import com.wafflehq.lib.uicore.io.jsonExportFileName
import com.wafflehq.lib.uicore.theme.AppSpacing
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.wafflehq.lib.navigation.R as NavR
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.components.AppCircularProgress
import com.wafflehq.lib.uicore.components.AppSnackbarHost

object BackupSettingsTestTags {
    const val SIGN_IN_ACTION = "backup_sign_in_action"
    const val SIGN_OUT_ACTION = "backup_sign_out_action"
    const val UNENCRYPTED_WARNING = "backup_unencrypted_warning"
    const val AUTO_TIME_FIELD = "backup_auto_time_field"
    const val RETENTION_MODE_FIELD = "backup_retention_mode_field"
    const val RETENTION_MAX_COUNT_FIELD = "backup_retention_max_count_field"
    const val RETENTION_MAX_AGE_FIELD = "backup_retention_max_age_field"
    const val RETENTION_GENERATIONAL_HINT = "backup_retention_generational_hint"
    const val CREATE_NOW_ACTION = "backup_create_now_action"
    const val EXPORT_ACTION = "backup_export_action"
    const val IMPORT_ACTION = "backup_import_action"
    const val EMPTY = "backup_empty"
    const val SIGN_OUT_CONFIRM = "backup_sign_out_confirm"
    const val CREATE_CONFIRM = "backup_create_confirm"
    const val RESTORE_CONFIRM = "backup_restore_confirm"
    const val IMPORT_CONFIRM = "backup_import_confirm"
    const val DELETE_CONFIRM = "backup_delete_confirm"
    const val PASSWORD_CONFIRM = "backup_password_confirm"
    const val ERROR_DISMISS = "backup_error_dismiss"

    fun row(id: String) = "backup_row_$id"

    fun restore(id: String) = "backup_restore_$id"

    fun delete(id: String) = "backup_delete_$id"
}

@Composable
fun BackupSettingsScreen(
    controller: BackupSettingsController,
    onBack: () -> Unit,
    exportFileNamePrefix: String,
    unencryptedWarningText: String,
    timeField: @Composable (time: LocalTime, label: String, useQuickInput: Boolean, onTimeChange: (LocalTime) -> Unit) -> Unit,
    formatTimestamp: (Instant) -> String = ::defaultBackupTimestamp
) {
    val state by controller.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(context.getString(message.labelRes()), duration = SnackbarDuration.Short)
        controller.clearSnackbar()
    }

    val accountPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        controller.onAccountPicked(result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME))
    }

    LaunchedEffect(Unit) {
        controller.accountPickerEvent.collect { intent ->
            accountPickerLauncher.launch(intent)
        }
    }

    val authorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        controller.onAuthorizationResult(result.data)
    }

    LaunchedEffect(Unit) {
        controller.authorizationRequiredEvent.collect { pendingIntent ->
            authorizationLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { controller.exportToFile(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { controller.confirmLocalImport(it) } }

    if (state.signOutConfirm) {
        AppDialog(
            onDismissRequest = controller::dismissSignOut,
            title = { Text(stringResource(R.string.appsettings_backup_sign_out_confirm_title)) },
            text = { Text(stringResource(R.string.appsettings_backup_sign_out_confirm_message)) },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_backup_sign_out),
                    role = AppButtonRole.Error,
                    variant = AppButtonVariant.Text,
                    onClick = controller::signOut,
                    modifier = Modifier.testTag(BackupSettingsTestTags.SIGN_OUT_CONFIRM),
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = controller::dismissSignOut,
                )
            }
        )
    }

    if (state.createConfirm) {
        AppDialog(
            onDismissRequest = controller::dismissCreateBackup,
            title = { Text(stringResource(R.string.appsettings_backup_create_confirm_title)) },
            text = { Text(stringResource(R.string.appsettings_backup_create_confirm_message)) },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_confirm),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    onClick = controller::createBackup,
                    modifier = Modifier.testTag(BackupSettingsTestTags.CREATE_CONFIRM),
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = controller::dismissCreateBackup,
                )
            }
        )
    }

    state.restoreConfirmTarget?.let { target ->
        AppDialog(
            onDismissRequest = controller::dismissRestore,
            title = { Text(stringResource(R.string.appsettings_backup_restore_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.appsettings_backup_restore_confirm_message,
                        formatTimestamp(target.createdAt)
                    )
                )
            },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_confirm),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    onClick = controller::restoreBackup,
                    modifier = Modifier.testTag(BackupSettingsTestTags.RESTORE_CONFIRM),
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = controller::dismissRestore,
                )
            }
        )
    }

    if (state.localImportConfirmUri != null) {
        AppDialog(
            onDismissRequest = controller::dismissLocalImport,
            title = { Text(stringResource(R.string.appsettings_backup_import_confirm_title)) },
            text = { Text(stringResource(R.string.appsettings_backup_import_confirm_message)) },
            confirmButton = {
                AppButton(
                    text = stringResource(R.string.appsettings_confirm),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    onClick = controller::importFromFile,
                    modifier = Modifier.testTag(BackupSettingsTestTags.IMPORT_CONFIRM),
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = controller::dismissLocalImport,
                )
            }
        )
    }

    if (state.passwordPrompt != null) {
        SecureScreenEffect()
    }

    state.passwordPrompt?.let { prompt ->
        BackupPasswordPromptDialog(
            wrongPassword = prompt.wrongPassword,
            onDismiss = controller::dismissPasswordPrompt,
            onConfirm = controller::retryWithPassword
        )
    }

    if (state.deleteConfirmTarget != null) {
        AppDialog(
            onDismissRequest = controller::dismissDelete,
            title = { Text(stringResource(R.string.appsettings_backup_delete_confirm_title)) },
            text = { Text(stringResource(R.string.appsettings_backup_delete_confirm_message)) },
            confirmButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_delete),
                    role = AppButtonRole.Error,
                    variant = AppButtonVariant.Text,
                    onClick = controller::deleteBackup,
                    modifier = Modifier.testTag(BackupSettingsTestTags.DELETE_CONFIRM),
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = controller::dismissDelete,
                )
            }
        )
    }

    state.error?.let { error ->
        AppDialog(
            onDismissRequest = controller::clearError,
            title = { Text(stringResource(R.string.appsettings_backup_error_title)) },
            text = { Text(settingsText(error)) },
            confirmButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_ok),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    onClick = controller::clearError,
                    modifier = Modifier.testTag(BackupSettingsTestTags.ERROR_DISMISS),
                )
            }
        )
    }

    SettingsScaffold(
        title = stringResource(R.string.appsettings_backup_settings_title),
        onBack = onBack,
        backDescription = stringResource(NavR.string.navigation_navigate_back),
        snackbarHost = { AppSnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = AppSpacing.xxl)
        ) {
            if (!state.isSignedIn) {
                item {
                    AppEmptyState(
                        text = stringResource(R.string.appsettings_backup_sign_in_description),
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.CloudSync,
                        actionLabel = stringResource(R.string.appsettings_backup_sign_in_google),
                        onAction = controller::requestSignIn,
                        actionModifier = Modifier.testTag(BackupSettingsTestTags.SIGN_IN_ACTION)
                    )
                }
            } else {
                item {
                    SettingsGroup(label = stringResource(R.string.appsettings_backup_account)) {
                        SettingsRowScaffold(
                            title = state.accountEmail.ifEmpty { stringResource(R.string.appsettings_backup_account) },
                            subtitle = stringResource(R.string.appsettings_backup_signed_in),
                            trailing = {
                                AppButton(
                                    text = stringResource(R.string.appsettings_backup_sign_out),
                                    role = AppButtonRole.Neutral,
                                    variant = AppButtonVariant.Text,
                                    onClick = controller::confirmSignOut,
                                    modifier = Modifier.testTag(BackupSettingsTestTags.SIGN_OUT_ACTION),
                                )
                            }
                        )
                    }
                }

                item {
                    SettingsGroup(label = stringResource(R.string.appsettings_backup_auto_section_title)) {
                        SettingsSwitchRow(
                            title = stringResource(R.string.appsettings_backup_auto_toggle),
                            subtitle = stringResource(R.string.appsettings_backup_auto_toggle_desc),
                            checked = state.autoBackupEnabled,
                            onCheckedChange = controller::onAutoBackupToggled
                        )
                        AnimatedVisibility(
                            visible = state.autoBackupEnabled,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Box(modifier = Modifier.testTag(BackupSettingsTestTags.AUTO_TIME_FIELD)) {
                                timeField(
                                    LocalTime.of(state.backupTimeMinutes / 60, state.backupTimeMinutes % 60),
                                    stringResource(R.string.appsettings_backup_auto_time_label),
                                    state.useQuickTimeInput
                                ) { newTime ->
                                    controller.onBackupTimeChanged(newTime.hour * 60 + newTime.minute)
                                }
                            }
                        }
                    }
                }

                if (state.autoBackupEnabled && !controller.backupsAreEncrypted) {
                    item {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                                .testTag(BackupSettingsTestTags.UNENCRYPTED_WARNING)
                        ) {
                            SettingsBanner(
                                icon = Icons.Filled.LockOpen,
                                title = stringResource(R.string.appsettings_backup_only_encryption_toggle),
                                text = unencryptedWarningText,
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                item {
                    SettingsGroup(label = stringResource(R.string.appsettings_backup_retention_section_title)) {
                        SettingsSwitchRow(
                            title = stringResource(R.string.appsettings_backup_retention_toggle),
                            subtitle = stringResource(R.string.appsettings_backup_retention_toggle_desc),
                            checked = state.retentionPolicyEnabled,
                            onCheckedChange = controller::onRetentionPolicyToggled
                        )
                        AnimatedVisibility(
                            visible = state.retentionPolicyEnabled,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                BackupRetentionModeDropdown(
                                    selected = state.retentionMode,
                                    onSelected = controller::onRetentionModeChanged
                                )
                                when (state.retentionMode) {
                                    BackupRetentionMode.COUNT -> BackupRetentionNumberField(
                                        value = state.retentionMaxCount,
                                        label = stringResource(R.string.appsettings_backup_retention_max_count_label),
                                        range = 1..999,
                                        maxDigits = 3,
                                        testTag = BackupSettingsTestTags.RETENTION_MAX_COUNT_FIELD,
                                        onValueChange = controller::onRetentionMaxCountChanged
                                    )
                                    BackupRetentionMode.AGE -> BackupRetentionNumberField(
                                        value = state.retentionMaxAgeDays,
                                        label = stringResource(R.string.appsettings_backup_retention_max_age_days_label),
                                        range = 1..3650,
                                        maxDigits = 4,
                                        testTag = BackupSettingsTestTags.RETENTION_MAX_AGE_FIELD,
                                        suffix = stringResource(R.string.appsettings_backup_retention_days_suffix),
                                        onValueChange = controller::onRetentionMaxAgeDaysChanged
                                    )
                                    BackupRetentionMode.GENERATIONAL -> Text(
                                        text = stringResource(R.string.appsettings_backup_retention_generational_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.testTag(BackupSettingsTestTags.RETENTION_GENERATIONAL_HINT)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    SettingsGroup(label = stringResource(R.string.appsettings_backup_save_now_title)) {
                        val formattedLastBackup = state.lastBackupTime.toInstantOrNull()?.let(formatTimestamp)
                        SettingsNavRow(
                            title = stringResource(R.string.appsettings_backup_create_now),
                            subtitle = if (formattedLastBackup != null)
                                stringResource(R.string.appsettings_backup_last_backup, formattedLastBackup)
                            else stringResource(R.string.appsettings_backup_save_now_info),
                            onClick = controller::confirmCreateBackup,
                            modifier = Modifier.testTag(BackupSettingsTestTags.CREATE_NOW_ACTION),
                            trailing = if (state.isLoading) {
                                {
                                    AppCircularProgress(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                {}
                            }
                        )
                    }
                }

                item {
                    SettingsGroup(label = stringResource(R.string.appsettings_backup_local_section_title)) {
                        SettingsNavRow(
                            title = stringResource(R.string.appsettings_backup_export_button),
                            subtitle = stringResource(R.string.appsettings_backup_export_desc),
                            onClick = { exportLauncher.launch(jsonExportFileName(exportFileNamePrefix)) },
                            modifier = Modifier.testTag(BackupSettingsTestTags.EXPORT_ACTION),
                            trailing = {}
                        )
                        SettingsRowDivider()
                        SettingsNavRow(
                            title = stringResource(R.string.appsettings_backup_import_button),
                            subtitle = stringResource(R.string.appsettings_backup_import_desc),
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.testTag(BackupSettingsTestTags.IMPORT_ACTION),
                            trailing = {}
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(AppSpacing.lg))
                    Column(modifier = Modifier.padding(horizontal = AppSpacing.md)) {
                        val filtered = state.backups.filter { backup ->
                            query.isBlank() || formatTimestamp(backup.createdAt)
                                .contains(query.trim(), ignoreCase = true)
                        }
                        ListCard {
                            ListHead(
                                stringResource(R.string.appsettings_backup_versions_title)
                            )
                            ListSearchField(
                                query = query,
                                onQuery = { query = it },
                                placeholder = stringResource(R.string.appsettings_backup_search)
                            )
                            if (filtered.isEmpty()) {
                                Box(modifier = Modifier.testTag(BackupSettingsTestTags.EMPTY)) {
                                    ListEmpty(stringResource(R.string.appsettings_backup_empty))
                                }
                            } else {
                                filtered.forEachIndexed { index, backup ->
                                    if (index > 0) {
                                        SettingsRowDivider()
                                    }
                                    BackupRow(
                                        backup = backup,
                                        isProtected = backup.isManual,
                                        formatTimestamp = formatTimestamp,
                                        onReplace = { controller.confirmRestore(backup) },
                                        onDelete = { controller.confirmDelete(backup) }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(AppSpacing.sm))
                }
            }
        }
    }
}

private fun BackupMessage.labelRes(): Int = when (this) {
    BackupMessage.CREATED -> R.string.appsettings_backup_created_success
    BackupMessage.EXPORTED -> R.string.appsettings_backup_export_success
    BackupMessage.RESTORED -> R.string.appsettings_backup_restored_success
    BackupMessage.DELETED -> R.string.appsettings_backup_deleted_success
}

fun defaultBackupTimestamp(instant: Instant): String =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(instant)

private fun String.toInstantOrNull(): Instant? = if (isEmpty()) null else runCatching { Instant.parse(this) }.getOrNull()

@Composable
private fun BackupRetentionModeDropdown(
    selected: BackupRetentionMode,
    onSelected: (BackupRetentionMode) -> Unit
) {
    val options = listOf(
        BackupRetentionMode.COUNT to R.string.appsettings_backup_retention_mode_count,
        BackupRetentionMode.AGE to R.string.appsettings_backup_retention_mode_age,
        BackupRetentionMode.GENERATIONAL to R.string.appsettings_backup_retention_mode_generational,
    )
    val selectedIndex = options.indexOfFirst { it.first == selected }.coerceAtLeast(0)
    Box(modifier = Modifier.testTag(BackupSettingsTestTags.RETENTION_MODE_FIELD)) {
        SettingsDropdownField(
            label = stringResource(R.string.appsettings_backup_retention_mode_label),
            value = stringResource(options[selectedIndex].second),
            options = options.map { stringResource(it.second) },
            selectedIndex = selectedIndex,
            onSelect = { onSelected(options[it].first) }
        )
    }
}

@Composable
private fun BackupRetentionNumberField(
    value: Int,
    label: String,
    range: IntRange,
    maxDigits: Int,
    testTag: String,
    onValueChange: (Int) -> Unit,
    suffix: String? = null
) {
    var input by remember(value) { mutableStateOf(value.toString()) }
    AppTextField(
        value = input,
        onValueChange = { raw ->
            val filtered = raw.filter { it.isDigit() }.take(maxDigits)
            input = filtered
            filtered.toIntOrNull()?.takeIf { it in range }?.let(onValueChange)
        },
        label = { Text(label) },
        suffix = suffix?.let { suffixText -> { Text(suffixText) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = input.isNotEmpty() && input.toIntOrNull() !in range,
        modifier = Modifier.fillMaxWidth().testTag(testTag)
    )
}

@Composable
private fun BackupRow(
    backup: BackupMeta,
    isProtected: Boolean,
    formatTimestamp: (Instant) -> String,
    onReplace: () -> Unit,
    onDelete: () -> Unit,
) {
    ListItemRow(modifier = Modifier.testTag(BackupSettingsTestTags.row(backup.id))) {
        Icon(
            imageVector = when {
                backup.isFailed -> Icons.Outlined.ErrorOutline
                backup.encrypted -> Icons.Filled.Lock
                else -> Icons.Filled.LockOpen
            },
            contentDescription = stringResource(
                if (backup.isFailed) R.string.appsettings_backup_failed_label
                else if (backup.encrypted) R.string.appsettings_backup_encrypted_label
                else R.string.appsettings_backup_plain_label
            ),
            tint = when {
                backup.isFailed -> MaterialTheme.colorScheme.error
                backup.encrypted -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(22.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            val (datePart, timePart) = formatTimestamp(backup.createdAt).split(" ", limit = 2)
                .let { it[0] to it.getOrElse(1) { "" } }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    datePart,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    timePart,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                if (isProtected) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = stringResource(R.string.appsettings_backup_manual_protected_label),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                if (backup.isPreRestoreSnapshot) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = stringResource(R.string.appsettings_backup_pre_restore_label),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            if (backup.isFailed) {
                Text(
                    stringResource(R.string.appsettings_backup_failed_label) + (backup.failureReason?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                val context = LocalContext.current
                val originPrefix = if (backup.isPreRestoreSnapshot) stringResource(R.string.appsettings_backup_pre_restore_label) + " · " else ""
                Text(
                    originPrefix + stringResource(
                        if (backup.encrypted) R.string.appsettings_backup_encrypted_label
                        else R.string.appsettings_backup_plain_label
                    ) + (backup.sizeBytes?.let { " · ${Formatter.formatShortFileSize(context, it)}" } ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (backup.encrypted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                AppButton(
                    text = stringResource(R.string.appsettings_backup_action_replace),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Tonal,
                    onClick = onReplace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag(BackupSettingsTestTags.restore(backup.id)),
                )
            }
        }
        AppIconButton(
            icon = Icons.Default.Delete,
            contentDescription = stringResource(UiCoreR.string.uicore_delete),
            role = AppButtonRole.Error,
            onClick = onDelete,
            modifier = Modifier.testTag(BackupSettingsTestTags.delete(backup.id)),
        )
    }
}

@Composable
private fun BackupPasswordPromptDialog(
    wrongPassword: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var hasChanges by remember { mutableStateOf(false) }
    val guardedDismiss = rememberGuardedDismiss(hasChanges = hasChanges, onDismiss = onDismiss)

    AppDialog(
        onDismissRequest = guardedDismiss,
        properties = DialogProperties(securePolicy = SecureFlagPolicy.SecureOn),
        title = { Text(stringResource(R.string.appsettings_backup_password_enter_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text(
                    stringResource(R.string.appsettings_backup_password_enter_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                AppTextField(
                    value = password,
                    onValueChange = { password = it; hasChanges = true },
                    label = { Text(stringResource(R.string.appsettings_backup_password_field_label)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = wrongPassword,
                    supportingText = if (wrongPassword) {
                        { Text(stringResource(R.string.appsettings_backup_password_wrong_error)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.appsettings_confirm),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Text,
                enabled = password.isNotEmpty(),
                onClick = { onConfirm(password) },
                modifier = Modifier.testTag(BackupSettingsTestTags.PASSWORD_CONFIRM),
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
