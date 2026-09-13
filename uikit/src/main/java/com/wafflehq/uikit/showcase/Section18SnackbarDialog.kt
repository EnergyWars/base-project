package com.wafflehq.uikit.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.uikit.R
import com.wafflehq.uikit.components.AppButton
import com.wafflehq.uikit.components.AppDialog
import com.wafflehq.uikit.components.AppDialogConfirmButton
import com.wafflehq.uikit.components.AppDialogDismissButton
import com.wafflehq.uikit.components.ButtonVariant
import com.wafflehq.uikit.theme.AppRadius
import com.wafflehq.uikit.theme.AppRole
import com.wafflehq.uikit.theme.AppSpacing
import com.wafflehq.uikit.theme.AppTheme

@Composable
fun Section18SnackbarDialog() = Section(R.string.sc_s18_title, R.string.sc_s18_desc) {
    Panel {
        Subhead(stringResource(R.string.sc_snack_sub))
        Column(modifier = Modifier.inspectTap("18a"), verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            SnackbarBox("18a.1") {
                Text(stringResource(R.string.sc_snack_deleted), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.inverseOnSurface)
                Spacer(Modifier.weight(1f))
                Text(stringResource(R.string.sc_snack_undo), style = MaterialTheme.typography.labelLarge, color = AppTheme.colors.primary.accent, modifier = Modifier.clickable {}.padding(horizontal = 8.dp, vertical = 4.dp))
            }
            SnackbarBox("18a.2") {
                Text(stringResource(R.string.sc_snack_location), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.inverseOnSurface)
            }
        }
        Subhead(stringResource(R.string.sc_dialog_sub))
        Column(modifier = Modifier.inspectTap("18b")) {
            DialogPreview("18b.1")
        }
        Subhead(stringResource(R.string.sc_dialog_live_sub))
        Column(modifier = Modifier.inspectTap("18c")) {
            LiveDialogDemo("18c.1", "18c.2", "18c.3")
        }
    }
}

@Composable
private fun LiveDialogDemo(triggerCode: String, cancelCode: String, confirmCode: String) {
    var open by remember { mutableStateOf(false) }
    AppButton(
        text = stringResource(R.string.sc_dialog_live_trigger),
        role = AppRole.Error,
        variant = ButtonVariant.Outlined,
        onClick = { open = true },
        modifier = Modifier.inspectId(triggerCode),
    )
    if (open) {
        AppDialog(
            onDismissRequest = { open = false },
            title = { Text(stringResource(R.string.sc_dialog_title)) },
            text = { Text(stringResource(R.string.sc_dialog_body)) },
            confirmButton = {
                AppDialogConfirmButton(
                    text = stringResource(R.string.sc_dialog_delete),
                    onClick = { open = false },
                    role = AppRole.Error,
                    modifier = Modifier.inspectId(confirmCode),
                )
            },
            dismissButton = {
                AppDialogDismissButton(
                    text = stringResource(R.string.sc_dialog_cancel),
                    onClick = { open = false },
                    modifier = Modifier.inspectId(cancelCode),
                )
            },
        )
    }
}

@Composable
private fun SnackbarBox(inspectCode: String, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, radiusS)
            .clip(radiusS)
            .background(MaterialTheme.colorScheme.inverseSurface)
            .inspectId(inspectCode)
            .padding(horizontal = AppSpacing.lg, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        content = content,
    )
}

@Composable
private fun DialogPreview(inspectCode: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(AppRadius.dialog))
            .clip(RoundedCornerShape(AppRadius.dialog))
            .background(AppTheme.colors.surface)
            .border(1.dp, AppTheme.colors.outline, RoundedCornerShape(AppRadius.dialog))
            .inspectId(inspectCode)
            .padding(AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(stringResource(R.string.sc_dialog_title), style = MaterialTheme.typography.titleLarge, color = AppTheme.colors.onSurface)
        Text(stringResource(R.string.sc_dialog_body), style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.onSurfaceVariant)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End)) {
            ShowcaseButton(stringResource(R.string.sc_dialog_cancel), AppRole.Neutral, BtnVariant.Text)
            ShowcaseButton(stringResource(R.string.sc_dialog_delete), AppRole.Error, BtnVariant.Filled)
        }
    }
}
