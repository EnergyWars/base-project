package com.wafflehq.base.ui.theme.showcase

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppRadius
import com.wafflehq.base.ui.theme.AppRole
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

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
