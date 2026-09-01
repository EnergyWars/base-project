package com.wafflehq.base.ui.theme.showcase

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppTheme

@Composable
fun Section30AccordionList() = Section(R.string.sc_s30_title, R.string.sc_s30_desc) {
    ListCard(stringResource(R.string.sc_acc_cap), groupCode = "30a") {
        ListHead(stringResource(R.string.sc_acc_title))
        Column {
            AccordionItem(stringResource(R.string.sc_acc_q1), stringResource(R.string.sc_acc_a1), initiallyOpen = true, inspectCode = "30a.1")
            Divider()
            AccordionItem(stringResource(R.string.sc_acc_q2), stringResource(R.string.sc_acc_a2), inspectCode = "30a.2")
            Divider()
            AccordionItem(stringResource(R.string.sc_acc_q3), stringResource(R.string.sc_acc_a3), inspectCode = "30a.3")
        }
    }
}

@Composable
private fun AccordionItem(question: String, answer: String, initiallyOpen: Boolean = false, inspectCode: String) {
    var open by remember { mutableStateOf(initiallyOpen) }
    val rotation by animateFloatAsState(if (open) 90f else 0f, label = "chev")
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .inspectId(inspectCode) { open = !open }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                question,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = AppTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Outlined.ChevronRight,
                null,
                tint = AppTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp).rotate(rotation),
            )
        }
        AnimatedVisibility(visible = open) {
            Text(
                answer,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            )
        }
    }
}
