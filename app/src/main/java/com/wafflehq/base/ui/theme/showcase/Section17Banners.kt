package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppRole
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

@Composable
fun Section17Banners() = Section(R.string.sc_s17_title, R.string.sc_s17_desc) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Banner(AppRole.Primary, Icons.Outlined.Info, R.string.sc_ban_primary_t, R.string.sc_ban_primary_d, "17a.1", showClose = true)
        Banner(AppRole.Success, Icons.Outlined.Check, R.string.sc_ban_success_t, R.string.sc_ban_success_d, "17a.2", showClose = true)
        Banner(AppRole.Warning, Icons.Outlined.WarningAmber, R.string.sc_ban_warning_t, R.string.sc_ban_warning_d, "17a.3", showClose = true)
        Banner(AppRole.Error, Icons.Outlined.ErrorOutline, R.string.sc_ban_error_t, R.string.sc_ban_error_d, "17a.4", showClose = true)
        NeutralBanner(Icons.Outlined.Info, R.string.sc_ban_neutral_t, R.string.sc_ban_neutral_d, "17a.5", showClose = true)
        Banner(AppRole.Secondary, Icons.Outlined.Info, R.string.sc_ban_sync_t, R.string.sc_ban_sync_d, "17a.6", showClose = true)
        Banner(AppRole.Tertiary, Icons.Outlined.Info, R.string.sc_ban_widget_t, R.string.sc_ban_widget_d, "17a.7", showClose = true)
        Banner(AppRole.Primary, Icons.Outlined.Info, R.string.sc_ban_update_t, R.string.sc_ban_update_d, "17a.8", actionRes = R.string.sc_ban_update_action, showClose = true)
        Banner(AppRole.Error, Icons.Outlined.ErrorOutline, R.string.sc_ban_storage_t, R.string.sc_ban_storage_d, "17a.9", actionRes = R.string.sc_ban_storage_action, showClose = true, filled = true)
    }
}

@Composable
private fun Banner(
    r: AppRole,
    icon: ImageVector,
    titleRes: Int,
    descRes: Int,
    inspectCode: String,
    actionRes: Int? = null,
    showClose: Boolean = false,
    filled: Boolean = false,
) {
    val rc = role(r)
    val bg = if (filled) rc.accent else rc.container
    val fg = if (filled) rc.onAccent else rc.onContainer
    val iconTint = if (filled) rc.onAccent else rc.accent
    BannerBase(bg, iconTint, fg, icon, titleRes, descRes, inspectCode, actionRes, showClose)
}

@Composable
private fun NeutralBanner(
    icon: ImageVector,
    titleRes: Int,
    descRes: Int,
    inspectCode: String,
    showClose: Boolean = false,
) {
    BannerBase(
        AppTheme.colors.surfaceVariant,
        AppTheme.colors.onSurfaceVariant,
        AppTheme.colors.onSurface,
        icon, titleRes, descRes, inspectCode, null, showClose,
    )
}

@Composable
private fun BannerBase(
    bg: Color,
    iconTint: Color,
    fg: Color,
    icon: ImageVector,
    titleRes: Int,
    descRes: Int,
    inspectCode: String,
    actionRes: Int?,
    showClose: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(radiusM).background(bg).inspectId(inspectCode).padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(titleRes), style = MaterialTheme.typography.titleSmall, color = fg)
                Text(stringResource(descRes), style = MaterialTheme.typography.bodySmall, color = fg.copy(alpha = 0.92f))
            }
            if (showClose) {
                Text("✕", style = MaterialTheme.typography.bodyMedium, color = fg.copy(alpha = 0.7f))
            }
        }
        if (actionRes != null) {
            Text(
                stringResource(actionRes),
                style = MaterialTheme.typography.labelLarge,
                color = iconTint,
            )
        }
    }
}
