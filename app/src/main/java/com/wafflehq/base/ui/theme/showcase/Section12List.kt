package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme
import com.wafflehq.base.ui.theme.GeistMono

@Composable
fun Section12List() = Section(R.string.sc_s12_title, R.string.sc_s12_desc) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(radiusM)
            .background(AppTheme.colors.surface)
            .border(1.dp, AppTheme.colors.outline, radiusM),
    ) {
        ListRow(Icons.Outlined.Circle, R.string.sc_list_one, null, null, R.string.sc_list_one_trail, "12a.1")
        Divider()
        ListRow(Icons.Outlined.Person, R.string.sc_list_anna, R.string.sc_list_anna_sub, null, R.string.sc_list_anna_trail, "12a.2")
        Divider()
        ListRow(Icons.Outlined.LocationOn, R.string.sc_list_park, R.string.sc_list_park_sub, R.string.sc_list_park_x, R.string.sc_list_park_trail, "12a.3")
    }
}

@Composable
private fun ListRow(icon: ImageVector, titleRes: Int, subRes: Int?, xRes: Int?, trailRes: Int, inspectCode: String) {
    val sc = AppTheme.colors.secondary
    Row(
        modifier = Modifier.fillMaxWidth().inspectId(inspectCode).padding(horizontal = AppSpacing.lg, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(sc.container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = sc.onContainer, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(titleRes), style = MaterialTheme.typography.titleSmall, color = AppTheme.colors.onSurface)
            if (subRes != null) Text(stringResource(subRes), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
            if (xRes != null) Text(stringResource(xRes), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
        }
        Text(stringResource(trailRes), style = MaterialTheme.typography.labelSmall.copy(fontFamily = GeistMono), color = AppTheme.colors.onSurfaceVariant)
    }
}
