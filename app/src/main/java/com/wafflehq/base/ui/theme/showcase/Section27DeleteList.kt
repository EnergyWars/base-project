package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

private data class FileItem(val id: Int, val name: String, val sub: String, val ext: String)

@Composable
fun Section27DeleteList() = Section(R.string.sc_s27_title, R.string.sc_s27_desc) {
    val initial = listOf(
        FileItem(0, stringResource(R.string.sc_del_f1), stringResource(R.string.sc_del_f1_sub), "PDF"),
        FileItem(1, stringResource(R.string.sc_del_f2), stringResource(R.string.sc_del_f2_sub), "FIG"),
        FileItem(2, stringResource(R.string.sc_del_f3), stringResource(R.string.sc_del_f3_sub), "MD"),
        FileItem(3, stringResource(R.string.sc_del_f4), stringResource(R.string.sc_del_f4_sub), "ZIP"),
    )
    val items = remember { mutableStateListOf(*initial.toTypedArray()) }
    val selected = remember { mutableStateListOf<Int>() }

    ListCard(stringResource(R.string.sc_del_cap), groupCode = "27a") {
        ListHead(stringResource(R.string.sc_del_title), count = stringResource(R.string.sc_del_count, items.size))
        if (selected.isNotEmpty()) {
            BulkBar(
                count = selected.size,
                onClear = { selected.clear() },
                onDelete = {
                    items.removeAll { it.id in selected }
                    selected.clear()
                },
            )
        }
        if (items.isEmpty()) {
            ListEmpty(stringResource(R.string.sc_del_empty))
        } else {
            items.forEachIndexed { index, file ->
                val isSel = file.id in selected
                ListItemRow(
                    modifier = Modifier.background(if (isSel) AppTheme.colors.primary.container else AppTheme.colors.surface).inspectId("27a.${index + 1}"),
                ) {
                    Checkbox(
                        checked = isSel,
                        onCheckedChange = { checked -> if (checked) selected.add(file.id) else selected.remove(file.id) },
                    )
                    ListAvatar(initials(file.name))
                    RowBody(file.name, file.sub)
                    TrailingText(file.ext)
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.label_delete),
                        tint = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                items.removeAll { it.id == file.id }
                                selected.remove(file.id)
                            }
                            .padding(7.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BulkBar(count: Int, onClear: () -> Unit, onDelete: () -> Unit) {
    val primary = AppTheme.colors.primary
    val error = AppTheme.colors.error
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(primary.container)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            stringResource(R.string.sc_del_selected, count),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = primary.onContainer,
            modifier = Modifier.weight(1f),
        )
        Text(
            stringResource(R.string.sc_del_clear),
            style = MaterialTheme.typography.labelLarge,
            color = primary.onContainer,
            modifier = Modifier.clip(pill).clickable(onClick = onClear).padding(horizontal = 10.dp, vertical = 6.dp),
        )
        Row(
            modifier = Modifier.clip(pill).background(error.accent).clickable(onClick = onDelete).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Outlined.DeleteOutline, null, tint = error.onAccent, modifier = Modifier.size(16.dp))
            Text(stringResource(R.string.label_delete), style = MaterialTheme.typography.labelLarge, color = error.onAccent)
        }
    }
}
