package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

@Composable
fun Section14Segmented() = Section(R.string.sc_s14_title, R.string.sc_s14_desc) {
    Panel {
        var sel by remember { mutableIntStateOf(0) }
        val options = listOf(R.string.sc_seg_day, R.string.sc_seg_week, R.string.sc_seg_month, R.string.sc_seg_year)
        val sc = AppTheme.colors.secondary
        Row(
            modifier = Modifier
                .clip(pill)
                .border(1.5.dp, AppTheme.colors.outline, pill),
        ) {
            options.forEachIndexed { i, res ->
                val selected = i == sel
                Row(
                    modifier = Modifier
                        .background(if (selected) sc.container else Color.Transparent)
                        .inspectId("14a.${i + 1}") { sel = i }
                        .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (selected) Icon(Icons.Outlined.Check, null, tint = sc.onContainer, modifier = Modifier.size(16.dp))
                    Text(stringResource(res), style = MaterialTheme.typography.labelLarge, color = if (selected) sc.onContainer else AppTheme.colors.onSurface)
                }
                if (i < options.lastIndex) Box(Modifier.width(1.5.dp).height(36.dp).background(AppTheme.colors.outline))
            }
        }
    }
}
