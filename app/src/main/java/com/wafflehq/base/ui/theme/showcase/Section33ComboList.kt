package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppTheme

private data class ComboItem(val id: Int, val name: String, val sub: String, val categories: Set<String>)

@Composable
fun Section33ComboList() = Section(R.string.sc_s33_title, R.string.sc_s33_desc) {
    val initial = listOf(
        ComboItem(0, stringResource(R.string.sc_p_anna), stringResource(R.string.sc_pr_lead), setOf("team", "fav")),
        ComboItem(1, stringResource(R.string.sc_p_jonas), stringResource(R.string.sc_pr_dev), setOf("team")),
        ComboItem(2, stringResource(R.string.sc_p_clara), stringResource(R.string.sc_pr_design_ext), setOf("extern")),
        ComboItem(3, stringResource(R.string.sc_p_leon), stringResource(R.string.sc_pr_backend), setOf("team", "fav")),
        ComboItem(4, stringResource(R.string.sc_p_mara), stringResource(R.string.sc_pr_einkauf), setOf("extern")),
        ComboItem(5, stringResource(R.string.sc_p_timo), stringResource(R.string.sc_pr_qa), setOf("team")),
    )
    val items = remember { mutableStateListOf(*initial.toTypedArray()) }
    val selected = remember { mutableStateListOf<Int>() }
    var query by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("all") }
    val cats = listOf(
        "all" to stringResource(R.string.sc_fl_all),
        "team" to stringResource(R.string.sc_fl_team),
        "extern" to stringResource(R.string.sc_fl_extern),
        "fav" to stringResource(R.string.sc_fl_fav),
    )
    val visible = items.filter { c ->
        (cat == "all" || cat in c.categories) &&
            (query.isBlank() || (c.name + " " + c.sub).contains(query.trim(), ignoreCase = true))
    }
    ListCard(stringResource(R.string.sc_co_cap), groupCode = "33a") {
        ListHead(stringResource(R.string.sc_co_title), count = "${visible.size} / ${items.size}")
        if (selected.isNotEmpty()) {
            ComboBulkBar(
                count = selected.size,
                onClear = { selected.clear() },
                onDelete = {
                    items.removeAll { it.id in selected }
                    selected.clear()
                },
            )
        }
        ListSearchField(query, { query = it }, stringResource(R.string.sc_search_ph), Icons.Outlined.Search)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            cats.forEach { (key, label) -> ListFilterChip(label, cat == key) { cat = key } }
        }
        if (visible.isEmpty()) {
            ListEmpty(stringResource(R.string.sc_fl_empty))
        } else {
            visible.forEachIndexed { index, item ->
                val isSel = item.id in selected
                ListItemRow(
                    modifier = Modifier.background(if (isSel) AppTheme.colors.primary.container else AppTheme.colors.surface).inspectId("33a.${index + 1}"),
                ) {
                    Icon(
                        Icons.Outlined.DragIndicator,
                        contentDescription = stringResource(R.string.sc_dnd_move_up),
                        tint = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp).clickable {
                            val idx = items.indexOfFirst { it.id == item.id }
                            if (idx > 0) {
                                val moved = items.removeAt(idx)
                                items.add(idx - 1, moved)
                            }
                        },
                    )
                    Checkbox(
                        checked = isSel,
                        onCheckedChange = { checked -> if (checked) selected.add(item.id) else selected.remove(item.id) },
                    )
                    ListAvatar(initials(item.name))
                    RowBody(item.name, item.sub)
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.label_delete),
                        tint = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable {
                                items.removeAll { it.id == item.id }
                                selected.remove(item.id)
                            }
                            .padding(7.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ComboBulkBar(count: Int, onClear: () -> Unit, onDelete: () -> Unit) {
    val primary = AppTheme.colors.primary
    val error = AppTheme.colors.error
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(primary.container)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Outlined.DeleteOutline, null, tint = error.onAccent, modifier = Modifier.size(16.dp))
            Text(stringResource(R.string.label_delete), style = MaterialTheme.typography.labelLarge, color = error.onAccent)
        }
    }
}
