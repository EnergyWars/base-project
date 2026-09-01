package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme
import com.wafflehq.base.ui.theme.GeistMono

@Composable
fun Section21Spacing() = Section(R.string.sc_s21_title, R.string.sc_s21_desc) {
    Panel {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl)) {
            Column(modifier = Modifier.weight(1f).inspectTap("21a"), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Subhead(stringResource(R.string.sc_spacing_sub))
                SpacingRow(R.string.sc_sp_xs, 4.dp, R.string.sc_px_4, "21a.1")
                SpacingRow(R.string.sc_sp_sm, 8.dp, R.string.sc_px_8, "21a.2")
                SpacingRow(R.string.sc_sp_md, 12.dp, R.string.sc_px_12, "21a.3")
                SpacingRow(R.string.sc_sp_lg, 16.dp, R.string.sc_px_16, "21a.4")
                SpacingRow(R.string.sc_sp_xl, 24.dp, R.string.sc_px_24, "21a.5")
                SpacingRow(R.string.sc_sp_xxl, 32.dp, R.string.sc_px_32, "21a.6")
            }
            Column(modifier = Modifier.weight(1f).inspectTap("21b"), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Subhead(stringResource(R.string.sc_radii_sub))
                RadiusRow(4.dp, R.string.sc_r_xs, R.string.sc_r_xs_d, "21b.1")
                RadiusRow(8.dp, R.string.sc_r_s, R.string.sc_r_s_d, "21b.2")
                RadiusRow(12.dp, R.string.sc_r_m, R.string.sc_r_m_d, "21b.3")
                RadiusRow(16.dp, R.string.sc_r_l, R.string.sc_r_l_d, "21b.4")
                RadiusRow(28.dp, R.string.sc_r_xl, R.string.sc_r_xl_d, "21b.5")
                RadiusRow(999.dp, R.string.sc_r_pill, R.string.sc_r_pill_d, "21b.6")
            }
        }
    }
}

@Composable
private fun SpacingRow(labelRes: Int, size: Dp, pxRes: Int, inspectCode: String) {
    Row(modifier = Modifier.inspectId(inspectCode), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Text(stringResource(labelRes), style = MaterialTheme.typography.labelMedium.copy(fontFamily = GeistMono), color = AppTheme.colors.onSurface, modifier = Modifier.width(36.dp))
        Box(Modifier.width(size).height(14.dp).clip(RoundedCornerShape(2.dp)).background(AppTheme.colors.secondary.accent))
        Spacer(Modifier.weight(1f))
        Text(stringResource(pxRes), style = MaterialTheme.typography.labelSmall.copy(fontFamily = GeistMono), color = AppTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun RadiusRow(radius: Dp, titleRes: Int, descRes: Int, inspectCode: String) {
    val sc = AppTheme.colors.secondary
    Row(modifier = Modifier.inspectId(inspectCode), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(radius))
                .background(sc.container)
                .border(1.5.dp, sc.accent, RoundedCornerShape(radius)),
        )
        Column {
            Text(stringResource(titleRes), style = MaterialTheme.typography.titleSmall, color = AppTheme.colors.onSurface)
            Text(stringResource(descRes), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
        }
    }
}
