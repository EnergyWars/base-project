package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R

internal data class Contact(
    val name: String,
    val sub: String,
    val trailing: String,
    val categories: Set<String>,
)

internal fun initials(name: String): String =
    name.trim().split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }

@Composable
internal fun rememberContacts(): List<Contact> {
    val star = stringResource(R.string.sc_glyph_star)
    return listOf(
        Contact(stringResource(R.string.sc_p_anna), stringResource(R.string.sc_pr_lead), star, setOf("team", "fav")),
        Contact(stringResource(R.string.sc_p_jonas), stringResource(R.string.sc_pr_dev), "9:42", setOf("team")),
        Contact(stringResource(R.string.sc_p_clara), stringResource(R.string.sc_pr_design_ext), stringResource(R.string.sc_day_tue), setOf("extern")),
        Contact(stringResource(R.string.sc_p_leon), stringResource(R.string.sc_pr_backend), star, setOf("team", "fav")),
        Contact(stringResource(R.string.sc_p_mara), stringResource(R.string.sc_pr_einkauf), stringResource(R.string.sc_day_mon), setOf("extern")),
        Contact(stringResource(R.string.sc_p_timo), stringResource(R.string.sc_pr_qa), "14:08", setOf("team")),
    )
}

@Composable
fun Section25FilterList() = Section(R.string.sc_s25_title, R.string.sc_s25_desc) {
    val contacts = rememberContacts()
    var query by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("all") }
    val cats = listOf(
        "all" to stringResource(R.string.sc_fl_all),
        "team" to stringResource(R.string.sc_fl_team),
        "extern" to stringResource(R.string.sc_fl_extern),
        "fav" to stringResource(R.string.sc_fl_fav),
    )
    val filtered = contacts.filter { c ->
        (cat == "all" || cat in c.categories) &&
            (query.isBlank() || (c.name + " " + c.sub).contains(query.trim(), ignoreCase = true))
    }
    ListCard(stringResource(R.string.sc_fl_cap), groupCode = "25a") {
        ListHead(stringResource(R.string.sc_fl_title), count = "${filtered.size} / ${contacts.size}")
        ListSearchField(query, { query = it }, stringResource(R.string.sc_search_ph), Icons.Outlined.Search)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            cats.forEach { (key, label) ->
                ListFilterChip(label, cat == key) { cat = key }
            }
        }
        if (filtered.isEmpty()) {
            ListEmpty(stringResource(R.string.sc_fl_empty))
        } else {
            filtered.forEachIndexed { index, c ->
                ListItemRow(modifier = Modifier.inspectId("25a.${index + 1}")) {
                    ListAvatar(initials(c.name))
                    RowBody(c.name, c.sub)
                    TrailingText(c.trailing)
                }
            }
        }
    }
}
