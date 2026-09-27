package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.uicore.R
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.theme.AppSpacing

@Composable
fun NameInputDialog(
    title: String,
    label: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = stringResource(R.string.uicore_ok),
    dismissLabel: String = stringResource(R.string.uicore_cancel),
    message: String? = null
) {
    var name by remember { mutableStateOf(initialName) }
    AppDialog(
        onDismissRequest = onDismiss,
        properties = AppDialogDefaults.properties,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(label) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            AppButton(
                text = confirmLabel,
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim()) },
            )
        },
        dismissButton = {
            AppButton(
                text = dismissLabel,
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                onClick = onDismiss,
            )
        }
    )
}
