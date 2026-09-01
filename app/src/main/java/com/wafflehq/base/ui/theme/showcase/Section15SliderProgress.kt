package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.components.AppSlider
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

@Composable
fun Section15SliderProgress() = Section(R.string.sc_s15_title, R.string.sc_s15_desc) {
    Panel {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl)) {
            Column(modifier = Modifier.weight(1f).inspectTap("15a"), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Subhead(stringResource(R.string.sc_slider_sub))
                SliderItem(R.string.sc_slider_continuous, 0.35f, 0f..1f, inspectCode = "15a.1")
                SliderItem(R.string.sc_slider_stepped, 7f, 0f..10f, steps = 9, inspectCode = "15a.2")
                SliderItem(R.string.sc_slider_disabled, 0.6f, 0f..1f, enabled = false, inspectCode = "15a.3")
            }
            Column(modifier = Modifier.weight(1f).inspectTap("15b"), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Subhead(stringResource(R.string.sc_progress_sub))
                Text(stringResource(R.string.sc_prog_lin_det), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
                LinearProgressIndicator(progress = { 0.65f }, modifier = Modifier.fillMaxWidth().inspectId("15b.1"))
                Text(stringResource(R.string.sc_prog_lin_indet), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().inspectId("15b.2"))
                Row(modifier = Modifier.inspectId("15b.3"), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
                    Text(stringResource(R.string.sc_prog_circ), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SliderItem(labelRes: Int, value: Float, range: ClosedFloatingPointRange<Float>, steps: Int = 0, enabled: Boolean = true, inspectCode: String) {
    var v by remember { mutableFloatStateOf(value) }
    Column(modifier = Modifier.inspectId(inspectCode)) {
        Text(stringResource(labelRes), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
        AppSlider(value = v, onValueChange = { v = it }, valueRange = range, steps = steps, enabled = enabled)
    }
}
