package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

@Composable
fun Section20Dividers() = Section(R.string.sc_s20_title, R.string.sc_s20_desc) {
    Panel {
        Box(Modifier.fillMaxWidth().height(1.dp).background(AppTheme.colors.outline).inspectId("20a.1"))
        Box(Modifier.fillMaxWidth().height(2.dp).background(AppTheme.colors.onSurfaceVariant).inspectId("20a.2"))
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(stringResource(R.string.sc_div_left), style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.onSurface)
            Box(Modifier.width(1.dp).height(24.dp).background(AppTheme.colors.outline).inspectId("20a.3"))
            Text(stringResource(R.string.sc_div_mid), style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.onSurface)
            Box(Modifier.width(1.dp).height(24.dp).background(AppTheme.colors.outline).inspectId("20a.4"))
            Text(stringResource(R.string.sc_div_right), style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.onSurface)
        }
    }
}
