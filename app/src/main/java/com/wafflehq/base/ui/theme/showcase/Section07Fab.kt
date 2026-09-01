package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme
import com.wafflehq.base.ui.theme.RoleColors

@Composable
fun Section07Fab() = Section(R.string.sc_s7_title, R.string.sc_s7_desc) {
    val rc = AppTheme.colors.primary
    Panel {
        FlowRowFab {
            FabItem(R.string.sc_fab_small) { FabBox(40.dp, 18.dp, rc, "7a.1") }
            FabItem(R.string.sc_fab_standard) { FabBox(56.dp, 22.dp, rc, "7a.2") }
            FabItem(R.string.sc_fab_large) { FabBox(96.dp, 36.dp, rc, "7a.3") }
            FabItem(R.string.sc_fab_extended) {
                Row(
                    modifier = Modifier
                        .height(56.dp)
                        .shadow(6.dp, radiusL)
                        .clip(radiusL)
                        .background(rc.container)
                        .inspectId("7a.4")
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Icon(Icons.Outlined.Add, null, tint = rc.onContainer, modifier = Modifier.size(22.dp))
                    Text(stringResource(R.string.sc_fab_event), style = MaterialTheme.typography.labelLarge, color = rc.onContainer)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowFab(content: @Composable () -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        content = { content() },
    )
}

@Composable
private fun FabBox(size: Dp, icon: Dp, rc: RoleColors, inspectCode: String) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(6.dp, radiusL)
            .clip(radiusL)
            .background(rc.container)
            .inspectId(inspectCode),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Outlined.Add, null, tint = rc.onContainer, modifier = Modifier.size(icon))
    }
}

@Composable
private fun FabItem(labelRes: Int, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        content()
        Text(stringResource(labelRes), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
    }
}
