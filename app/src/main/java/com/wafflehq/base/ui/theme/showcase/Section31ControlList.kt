package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing

@Composable
fun Section31ControlList() = Section(R.string.sc_s31_title, R.string.sc_s31_desc) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        ListCard(stringResource(R.string.sc_ctl_cap_a), groupCode = "31a") {
            ListHead(stringResource(R.string.sc_ctl_title_a))
            SwitchControlRow(Icons.Outlined.Notifications, stringResource(R.string.sc_ctl_a1), stringResource(R.string.sc_ctl_a1_sub), true, "31a.1")
            SwitchControlRow(Icons.Outlined.MailOutline, stringResource(R.string.sc_ctl_a2), stringResource(R.string.sc_ctl_a2_sub), false, "31a.2")
            SwitchControlRow(Icons.Outlined.Schedule, stringResource(R.string.sc_ctl_a3), stringResource(R.string.sc_ctl_a3_sub), true, "31a.3")
        }
        ListCard(stringResource(R.string.sc_ctl_cap_b), groupCode = "31b") {
            ListHead(stringResource(R.string.sc_ctl_title_b))
            CheckControlRow(stringResource(R.string.sc_ctl_b1), stringResource(R.string.sc_done), true, "31b.1")
            CheckControlRow(stringResource(R.string.sc_ctl_b2), stringResource(R.string.sc_open), false, "31b.2")
            BadgeControlRow(stringResource(R.string.sc_ctl_b3), stringResource(R.string.sc_ctl_b_sub), "7", "31b.3")
            BadgeControlRow(stringResource(R.string.sc_ctl_b4), stringResource(R.string.sc_ctl_b_sub), "24", "31b.4")
        }
    }
}

@Composable
private fun SwitchControlRow(icon: ImageVector, title: String, sub: String, checked: Boolean, inspectCode: String) {
    var c by remember { mutableStateOf(checked) }
    ListItemRow(modifier = Modifier.inspectId(inspectCode)) {
        ListLeadIcon(icon)
        RowBody(title, sub)
        Switch(checked = c, onCheckedChange = { c = it })
    }
}

@Composable
private fun CheckControlRow(title: String, sub: String, checked: Boolean, inspectCode: String) {
    var c by remember { mutableStateOf(checked) }
    ListItemRow(modifier = Modifier.inspectId(inspectCode)) {
        Checkbox(checked = c, onCheckedChange = { c = it })
        RowBody(title, sub)
    }
}

@Composable
private fun BadgeControlRow(title: String, sub: String, badge: String, inspectCode: String) {
    ListItemRow(modifier = Modifier.inspectId(inspectCode)) {
        ListAvatar(initials(title))
        RowBody(title, sub)
        ListPrimaryBadge(badge)
    }
}
