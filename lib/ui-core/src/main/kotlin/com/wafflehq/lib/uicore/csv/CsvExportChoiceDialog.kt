package com.wafflehq.lib.uicore.csv

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.layout.Arrangement
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.components.AppDialogDefaults
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.uicore.R
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant

@Composable
fun CsvExportChoiceDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onPreview: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null
) {
    AppDialog(
        onDismissRequest = onDismissRequest,
        properties = AppDialogDefaults.properties,
        modifier = modifier,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                if (message != null) {
                    Text(message)
                }
                AppButton(
                    text = stringResource(R.string.uicore_csv_export_preview),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    leadingIcon = Icons.Default.Visibility,
                    onClick = onPreview,
                    modifier = Modifier.fillMaxWidth()
                )
                AppButton(
                    text = stringResource(R.string.uicore_csv_export_share),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    leadingIcon = Icons.Default.Share,
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth()
                )
                AppButton(
                    text = stringResource(R.string.uicore_csv_export_save),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    leadingIcon = Icons.Default.Save,
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            AppButton(
                text = stringResource(R.string.uicore_cancel),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                onClick = onDismissRequest
            )
        }
    )
}
