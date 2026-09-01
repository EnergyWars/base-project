package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

@Composable
fun Section28PlainList() = Section(R.string.sc_s28_title, R.string.sc_s28_desc) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        ListCard(stringResource(R.string.sc_pl_cap_a), groupCode = "28a") {
            ListHead(stringResource(R.string.sc_pl_title_a))
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                PlainRow(stringResource(R.string.sc_pl_a1), stringResource(R.string.sc_pl_a1_v), inspectCode = "28a.1")
                PlainRow(stringResource(R.string.sc_pl_a2), stringResource(R.string.sc_pl_a2_v), tinted = true, inspectCode = "28a.2")
                PlainRow(stringResource(R.string.sc_pl_a3), stringResource(R.string.sc_pl_a3_v), inspectCode = "28a.3")
                PlainRow(stringResource(R.string.sc_pl_a4), stringResource(R.string.sc_pl_a4_v), inspectCode = "28a.4")
                PlainRow(stringResource(R.string.sc_pl_a5), stringResource(R.string.sc_pl_a5_v), inspectCode = "28a.5")
            }
        }
        ListCard(stringResource(R.string.sc_pl_cap_b), groupCode = "28b") {
            ListHead(stringResource(R.string.sc_pl_title_b))
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                PlainRow(stringResource(R.string.sc_pl_b1), stringResource(R.string.sc_pl_b1_v), tinted = true, dot = true, inspectCode = "28b.1")
                PlainRow(stringResource(R.string.sc_pl_b2), stringResource(R.string.sc_pl_b2_v), dot = true, inspectCode = "28b.2")
                PlainRow(stringResource(R.string.sc_pl_b3), stringResource(R.string.sc_pl_b3_v), dot = true, inspectCode = "28b.3")
                PlainRow(stringResource(R.string.sc_pl_b4), stringResource(R.string.sc_pl_b4_v), dot = true, inspectCode = "28b.4")
            }
        }
    }
}

@Composable
private fun PlainRow(title: String, trailing: String, tinted: Boolean = false, dot: Boolean = false, inspectCode: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (tinted) AppTheme.colors.primary.container else AppTheme.colors.surface)
            .inspectId(inspectCode)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (dot) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(AppTheme.colors.primary.accent))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        TrailingText(trailing)
    }
}
