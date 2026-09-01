package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppTheme

private data class Member(val name: String, val role: String, val starred: Boolean = false)

@Composable
fun Section29GroupedList() = Section(R.string.sc_s29_title, R.string.sc_s29_desc) {
    val star = stringResource(R.string.sc_glyph_star)
    val groups = listOf(
        "A" to listOf(
            Member(stringResource(R.string.sc_p_anna), stringResource(R.string.sc_pr_lead), starred = true),
            Member(stringResource(R.string.sc_p_ali), stringResource(R.string.sc_pr_marketing)),
        ),
        "C" to listOf(Member(stringResource(R.string.sc_p_clara), stringResource(R.string.sc_pr_design))),
        "J" to listOf(Member(stringResource(R.string.sc_p_jonas), stringResource(R.string.sc_pr_dev))),
        "L" to listOf(Member(stringResource(R.string.sc_p_leon), stringResource(R.string.sc_pr_backend), starred = true)),
        "T" to listOf(Member(stringResource(R.string.sc_p_timo), stringResource(R.string.sc_pr_qa))),
    )
    ListCard(stringResource(R.string.sc_gl_cap), groupCode = "29a") {
        ListHead(stringResource(R.string.sc_gl_title))
        Column(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
            var memberIndex = 0
            groups.forEach { (label, members) ->
                GroupLabel(label)
                members.forEach { m ->
                    memberIndex += 1
                    ListItemRow(modifier = Modifier.inspectId("29a.$memberIndex")) {
                        ListAvatar(initials(m.name))
                        RowBody(m.name, m.role)
                        TrailingText(if (m.starred) star else "")
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupLabel(label: String) {
    Text(
        label,
        style = mono.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.8.sp),
        color = AppTheme.colors.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}
