package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme
import com.wafflehq.base.ui.theme.GeistMono

@Composable
fun Section01Typography() = Section(R.string.sc_s1_title, R.string.sc_s1_desc) {
    val sampleDisplay = stringResource(R.string.sc_sample_display)
    val sampleHeadline = stringResource(R.string.sc_sample_headline)
    val sampleTitle = stringResource(R.string.sc_sample_title)
    val sampleBody = stringResource(R.string.sc_sample_body)
    val sampleLabel = stringResource(R.string.sc_sample_label)
    val t = MaterialTheme.typography
    Panel {
        TypeRow(R.string.sc_t_display_l, R.string.sc_m_display_l, sampleDisplay, t.displayLarge, "1a.1")
        TypeRow(R.string.sc_t_display_m, R.string.sc_m_display_m, sampleDisplay, t.displayMedium, "1a.2")
        TypeRow(R.string.sc_t_display_s, R.string.sc_m_display_s, sampleDisplay, t.displaySmall, "1a.3")
        TypeRow(R.string.sc_t_headline_l, R.string.sc_m_headline_l, sampleHeadline, t.headlineLarge, "1a.4")
        TypeRow(R.string.sc_t_headline_m, R.string.sc_m_headline_m, sampleHeadline, t.headlineMedium, "1a.5")
        TypeRow(R.string.sc_t_headline_s, R.string.sc_m_headline_s, sampleHeadline, t.headlineSmall, "1a.6")
        TypeRow(R.string.sc_t_title_l, R.string.sc_m_title_l, sampleTitle, t.titleLarge, "1a.7")
        TypeRow(R.string.sc_t_title_m, R.string.sc_m_title_m, sampleTitle, t.titleMedium, "1a.8")
        TypeRow(R.string.sc_t_title_s, R.string.sc_m_title_s, sampleTitle, t.titleSmall, "1a.9")
        TypeRow(R.string.sc_t_body_l, R.string.sc_m_body_l, sampleBody, t.bodyLarge, "1a.10")
        TypeRow(R.string.sc_t_body_m, R.string.sc_m_body_m, sampleBody, t.bodyMedium, "1a.11")
        TypeRow(R.string.sc_t_body_s, R.string.sc_m_body_s, sampleBody, t.bodySmall, "1a.12")
        TypeRow(R.string.sc_t_label_l, R.string.sc_m_label_l, sampleLabel, t.labelLarge, "1a.13")
        TypeRow(R.string.sc_t_label_m, R.string.sc_m_label_m, sampleLabel, t.labelMedium, "1a.14")
        TypeRow(R.string.sc_t_label_s, R.string.sc_m_label_s, sampleLabel, t.labelSmall, "1a.15")
    }
}

@Composable
private fun TypeRow(nameRes: Int, metaRes: Int, sample: String, style: TextStyle, code: String) {
    Column(modifier = Modifier.fillMaxWidth().inspectId(code), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(nameRes), style = MaterialTheme.typography.labelSmall.copy(fontFamily = GeistMono), color = AppTheme.colors.onSurface, fontWeight = FontWeight.Bold)
            Text(stringResource(metaRes), style = MaterialTheme.typography.labelSmall.copy(fontFamily = GeistMono), color = AppTheme.colors.onSurfaceVariant)
        }
        Text(sample, style = style, color = AppTheme.colors.onSurface)
    }
}
