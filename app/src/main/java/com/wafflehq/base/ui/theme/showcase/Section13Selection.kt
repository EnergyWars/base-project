package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

@Composable
fun Section13Selection() = Section(R.string.sc_s13_title, R.string.sc_s13_desc) {
    Panel {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl)) {
            Column(modifier = Modifier.weight(1f).inspectTap("13a"), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Subhead(stringResource(R.string.sc_sel_checkbox))
                CheckRow(R.string.sc_sel_on, true, code = "13a.1")
                CheckRow(R.string.sc_sel_off, false, code = "13a.2")
                CheckRow(R.string.sc_sel_locked, true, enabled = false, code = "13a.3")
            }
            Column(modifier = Modifier.weight(1f).inspectTap("13b"), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Subhead(stringResource(R.string.sc_sel_switch))
                SwitchRow(R.string.sc_sel_light_mode, true, code = "13b.1")
                SwitchRow(R.string.sc_sel_push, false, code = "13b.2")
                SwitchRow(R.string.sc_sel_locked, false, enabled = false, code = "13b.3")
            }
            Column(modifier = Modifier.weight(1f).inspectTap("13c"), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Subhead(stringResource(R.string.sc_sel_radio))
                var sel by remember { mutableIntStateOf(0) }
                RadioRow(R.string.sc_sel_system, sel == 0, code = "13c.1") { sel = 0 }
                RadioRow(R.string.sc_sel_light, sel == 1, code = "13c.2") { sel = 1 }
                RadioRow(R.string.sc_sel_dark, sel == 2, code = "13c.3") { sel = 2 }
            }
        }
    }
}

@Composable
private fun CheckRow(labelRes: Int, checked: Boolean, enabled: Boolean = true, code: String) {
    var c by remember { mutableStateOf(checked) }
    Row(modifier = Modifier.inspectId(code), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = c, onCheckedChange = { c = it }, enabled = enabled)
        Text(stringResource(labelRes), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurface)
    }
}

@Composable
private fun SwitchRow(labelRes: Int, checked: Boolean, enabled: Boolean = true, code: String) {
    var c by remember { mutableStateOf(checked) }
    Row(modifier = Modifier.inspectId(code), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Switch(checked = c, onCheckedChange = { c = it }, enabled = enabled)
        Text(stringResource(labelRes), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurface)
    }
}

@Composable
private fun RadioRow(labelRes: Int, selected: Boolean, code: String, onClick: () -> Unit) {
    Row(modifier = Modifier.inspectId(code), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Text(stringResource(labelRes), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurface)
    }
}
