package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppRole
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

@Composable
fun Section09Chips() = Section(R.string.sc_s9_title, R.string.sc_s9_desc) {
    Panel {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Subhead(stringResource(R.string.sc_chip_assist_sub))
            WrapRow {
                AssistChip(stringResource(R.string.sc_chip_tip), Icons.Outlined.Info, "9a.1")
                OutlineChip(stringResource(R.string.sc_chip_addlist), "9a.2")
                ContainerChip(stringResource(R.string.sc_chip_callable), AppRole.Success, "9a.3")
                ContainerChip(stringResource(R.string.sc_chip_urgent), AppRole.Error, "9a.4")
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Subhead(stringResource(R.string.sc_chip_filter_sub))
            var selected by remember { mutableIntStateOf(0) }
            val filters = listOf(R.string.sc_chip_all, R.string.sc_chip_callable_today, R.string.sc_chip_capacity, R.string.sc_chip_nearby)
            WrapRow {
                filters.forEachIndexed { i, res ->
                    FilterChip(stringResource(res), AppRole.Secondary, i == selected, "9a.${5 + i}") { selected = i }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Subhead(stringResource(R.string.sc_chip_input_sub))
            WrapRow {
                InputChip(stringResource(R.string.sc_chip_anna), stringResource(R.string.sc_av_am), AppRole.Primary, selected = true, removable = true, inspectCode = "9a.9")
                InputChip(stringResource(R.string.sc_chip_jonas), stringResource(R.string.sc_av_jb), AppRole.Primary, selected = false, removable = false, inspectCode = "9a.10")
                InputChip(stringResource(R.string.sc_chip_sara), stringResource(R.string.sc_av_sf), AppRole.Error, selected = true, removable = true, inspectCode = "9a.11")
            }
        }
    }
}

@Composable
private fun chipBase(modifier: Modifier, bg: Color, border: Color?, inspectCode: String, content: @Composable () -> Unit) {
    Row(
        modifier = modifier
            .clip(pill)
            .background(bg)
            .then(if (border != null) Modifier.border(1.dp, border, pill) else Modifier)
            .inspectId(inspectCode)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        content = { content() },
    )
}

@Composable
private fun AssistChip(label: String, icon: ImageVector, inspectCode: String) {
    chipBase(Modifier, AppTheme.colors.surface, AppTheme.colors.outline, inspectCode) {
        Icon(icon, null, tint = AppTheme.colors.onSurface, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.onSurface)
    }
}

@Composable
private fun OutlineChip(label: String, inspectCode: String) {
    chipBase(Modifier, AppTheme.colors.surface, AppTheme.colors.outline, inspectCode) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.onSurface)
    }
}

@Composable
private fun ContainerChip(label: String, r: AppRole, inspectCode: String) {
    val rc = role(r)
    chipBase(Modifier, rc.container, null, inspectCode) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = rc.onContainer)
    }
}

@Composable
private fun FilterChip(label: String, r: AppRole, selected: Boolean, inspectCode: String, onClick: () -> Unit) {
    val rc = role(r)
    val bg = if (selected) rc.container else AppTheme.colors.surface
    val fg = if (selected) rc.onContainer else AppTheme.colors.onSurface
    Row(
        modifier = Modifier
            .clip(pill)
            .background(bg)
            .then(if (!selected) Modifier.border(1.dp, AppTheme.colors.outline, pill) else Modifier)
            .inspectId(inspectCode, onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

@Composable
private fun InputChip(label: String, initials: String, r: AppRole, selected: Boolean, removable: Boolean, inspectCode: String) {
    val rc = role(r)
    val bg = if (selected) rc.container else AppTheme.colors.surfaceVariant
    val fg = if (selected) rc.onContainer else AppTheme.colors.onSurface
    Row(
        modifier = Modifier
            .clip(pill)
            .background(bg)
            .inspectId(inspectCode)
            .padding(start = 4.dp, end = if (removable) 6.dp else 14.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier.size(20.dp).clip(CircleShape).background(rc.accent),
            contentAlignment = Alignment.Center,
        ) {
            Text(initials, style = mono.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = rc.onAccent)
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = fg)
        if (removable) {
            Box(
                modifier = Modifier.size(16.dp).clip(CircleShape).background(fg.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Close, null, tint = fg, modifier = Modifier.size(11.dp))
            }
        }
    }
}
