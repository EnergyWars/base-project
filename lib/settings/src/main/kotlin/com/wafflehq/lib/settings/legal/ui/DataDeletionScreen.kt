package com.wafflehq.lib.settings.legal.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.lib.navigation.settings.SettingsScaffold
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.legal.DataDeletionController
import com.wafflehq.lib.settings.legal.DataDeletionState
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.navigation.R as NavR
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.components.AppCircularProgress

data class DataDeletionCloudSyncAction(val label: String, val onClick: () -> Unit)

object DataDeletionTestTags {
    const val EXPLANATION = "data_deletion_explanation"
    const val CLOUD_BACKUP_HINT = "data_deletion_cloud_backup_hint"
    const val CLOUD_SYNC_ACTION = "data_deletion_cloud_sync_action"
    const val DELETE_ACTION = "data_deletion_delete_action"
    const val RETRY_ACTION = "data_deletion_retry_action"
    const val PROGRESS = "data_deletion_progress"
    const val DONE = "data_deletion_done"
    const val ERROR = "data_deletion_error"
    const val CONFIRM_ACTION = "data_deletion_confirm_action"
}

@Composable
fun DataDeletionScreen(
    controller: DataDeletionController,
    explanation: String,
    onBack: () -> Unit,
    cloudBackupHint: String? = null,
    cloudSyncAction: DataDeletionCloudSyncAction? = null
) {
    val state by controller.state.collectAsStateWithLifecycle()
    var showConfirmDialog by remember { mutableStateOf(false) }

    SettingsScaffold(
        title = stringResource(R.string.appsettings_legal_data_deletion),
        onBack = onBack,
        backDescription = stringResource(NavR.string.navigation_navigate_back)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
        ) {
            when (state) {
                DataDeletionState.DONE -> {
                    Text(
                        text = stringResource(R.string.appsettings_data_deletion_done),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag(DataDeletionTestTags.DONE)
                    )
                }
                DataDeletionState.DELETING -> {
                    AppCircularProgress(modifier = Modifier.testTag(DataDeletionTestTags.PROGRESS))
                }
                DataDeletionState.ERROR -> {
                    Text(
                        text = stringResource(R.string.appsettings_data_deletion_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag(DataDeletionTestTags.ERROR)
                    )
                    AppButton(
                        text = stringResource(R.string.appsettings_data_deletion_retry),
                        role = AppButtonRole.Error,
                        variant = AppButtonVariant.Outlined,
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(DataDeletionTestTags.RETRY_ACTION)
                    )
                }
                DataDeletionState.IDLE -> {
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag(DataDeletionTestTags.EXPLANATION)
                    )
                    if (cloudBackupHint != null) {
                        Text(
                            text = cloudBackupHint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag(DataDeletionTestTags.CLOUD_BACKUP_HINT)
                        )
                    }
                    if (cloudSyncAction != null) {
                        AppButton(
                            text = cloudSyncAction.label,
                            role = AppButtonRole.Neutral,
                            variant = AppButtonVariant.Outlined,
                            onClick = cloudSyncAction.onClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(DataDeletionTestTags.CLOUD_SYNC_ACTION)
                        )
                    }
                    AppButton(
                        text = stringResource(R.string.appsettings_data_deletion_action),
                        role = AppButtonRole.Error,
                        variant = AppButtonVariant.Outlined,
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(DataDeletionTestTags.DELETE_ACTION)
                    )
                }
            }
        }
    }

    if (showConfirmDialog) {
        AppDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.appsettings_data_deletion_confirm_title)) },
            text = { Text(stringResource(R.string.appsettings_data_deletion_confirm_message)) },
            confirmButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_delete),
                    role = AppButtonRole.Error,
                    variant = AppButtonVariant.Text,
                    onClick = {
                        showConfirmDialog = false
                        controller.deleteAllData()
                    },
                    modifier = Modifier.testTag(DataDeletionTestTags.CONFIRM_ACTION)
                )
            },
            dismissButton = {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = { showConfirmDialog = false }
                )
            }
        )
    }
}
