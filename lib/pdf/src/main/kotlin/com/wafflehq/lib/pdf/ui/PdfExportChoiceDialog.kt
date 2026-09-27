package com.wafflehq.lib.pdf.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.pdf.R
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.components.AppDialog

@Composable
fun PdfExportChoiceDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onOpenInReader: (() -> Unit)? = null,
    extraActions: @Composable () -> Unit = {}
) {
    AppDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                if (message != null) {
                    Text(message)
                }
                AppButton(
                    text = stringResource(R.string.pdf_export_preview),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    leadingIcon = Icons.Default.Visibility,
                    onClick = onPreview,
                    modifier = Modifier.fillMaxWidth()
                )
                AppButton(
                    text = stringResource(R.string.pdf_export_share),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    leadingIcon = Icons.Default.Share,
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth()
                )
                AppButton(
                    text = stringResource(R.string.pdf_export_save),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    leadingIcon = Icons.Default.Save,
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth()
                )
                if (onOpenInReader != null) {
                    AppButton(
                        text = stringResource(R.string.pdf_export_open_in_reader),
                        role = AppButtonRole.Primary,
                        variant = AppButtonVariant.Text,
                        leadingIcon = Icons.Default.PictureAsPdf,
                        onClick = onOpenInReader,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                extraActions()
            }
        },
        confirmButton = {},
        dismissButton = {
            AppButton(
                text = stringResource(UiCoreR.string.uicore_cancel),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                onClick = onDismissRequest
            )
        }
    )
}
