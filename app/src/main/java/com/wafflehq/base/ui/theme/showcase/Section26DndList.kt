package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

private data class Task(val name: String, val meta: String)

@Composable
fun Section26DndList() = Section(R.string.sc_s26_title, R.string.sc_s26_desc) {
    val initial = listOf(
        Task(stringResource(R.string.sc_dnd_t1), stringResource(R.string.sc_today)),
        Task(stringResource(R.string.sc_dnd_t2), stringResource(R.string.sc_day_tue)),
        Task(stringResource(R.string.sc_dnd_t3), stringResource(R.string.sc_day_wed)),
        Task(stringResource(R.string.sc_dnd_t4), stringResource(R.string.sc_day_fri)),
        Task(stringResource(R.string.sc_dnd_t5), stringResource(R.string.sc_next_week)),
    )
    val items = remember { mutableStateListOf(*initial.toTypedArray()) }
    ListCard(stringResource(R.string.sc_dnd_cap), groupCode = "26a") {
        ListHead(stringResource(R.string.sc_dnd_title))
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(Icons.Outlined.OpenWith, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Text(stringResource(R.string.sc_dnd_hint), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            items.forEachIndexed { index, task ->
                DndItem(rank = index + 1, task = task, inspectCode = "26a.${index + 1}") {
                    if (index > 0) {
                        val it2 = items.removeAt(index)
                        items.add(index - 1, it2)
                    }
                }
            }
        }
    }
}

@Composable
private fun DndItem(rank: Int, task: Task, inspectCode: String, onMoveUp: () -> Unit) {
    val primary = AppTheme.colors.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(radiusM)
            .background(AppTheme.colors.surface)
            .border(1.dp, AppTheme.colors.outline, radiusM)
            .inspectId(inspectCode)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            Icons.Outlined.DragIndicator,
            contentDescription = stringResource(R.string.sc_dnd_move_up),
            tint = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(22.dp).clickable(onClick = onMoveUp),
        )
        Box(
            modifier = Modifier.size(36.dp).clip(radiusS).background(primary.accent),
            contentAlignment = Alignment.Center,
        ) {
            Text("$rank", style = mono.copy(fontWeight = FontWeight.Bold), color = primary.onAccent)
        }
        Text(
            task.name,
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(task.meta, style = mono.copy(), color = AppTheme.colors.onSurfaceVariant, maxLines = 1)
    }
}
