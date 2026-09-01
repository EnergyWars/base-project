package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme
import com.wafflehq.base.ui.theme.GeistMono

@Composable
fun Section02Weights() = Section(R.string.sc_s2_title, R.string.sc_s2_desc) {
    val sample = stringResource(R.string.sc_weight_sample)
    Panel {
        WeightRow(R.string.sc_w_light, sample, FontWeight.Light, "2a.1")
        WeightRow(R.string.sc_w_regular, sample, FontWeight.Normal, "2a.2")
        WeightRow(R.string.sc_w_medium, sample, FontWeight.Medium, "2a.3")
        WeightRow(R.string.sc_w_semibold, sample, FontWeight.SemiBold, "2a.4")
        WeightRow(R.string.sc_w_bold, sample, FontWeight.Bold, "2a.5")
    }
}

@Composable
private fun WeightRow(nameRes: Int, sample: String, weight: FontWeight, id: String) {
    Column(
        modifier = Modifier.fillMaxWidth().inspectId(id),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(stringResource(nameRes), style = MaterialTheme.typography.labelSmall.copy(fontFamily = GeistMono), color = AppTheme.colors.onSurface, fontWeight = FontWeight.Bold)
        Text(sample, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = weight), color = AppTheme.colors.onSurface)
    }
}
