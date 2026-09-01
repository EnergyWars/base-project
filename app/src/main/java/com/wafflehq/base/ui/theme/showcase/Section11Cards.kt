package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme
import com.wafflehq.base.ui.theme.GeistMono

@Composable
fun Section11Cards() = Section(R.string.sc_s11_title, R.string.sc_s11_desc) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        ShowcaseCard(R.string.sc_card_filled, R.string.sc_card_filled_cls, R.string.sc_card_filled_meta, CardKind.Filled, "11a.1")
        ShowcaseCard(R.string.sc_card_elevated, R.string.sc_card_elevated_cls, R.string.sc_card_elevated_meta, CardKind.Elevated, "11a.2")
        ShowcaseCard(R.string.sc_card_outlined, R.string.sc_card_outlined_cls, R.string.sc_card_outlined_meta, CardKind.Outlined, "11a.3")
    }
}

private enum class CardKind { Filled, Elevated, Outlined }

@Composable
private fun ShowcaseCard(titleRes: Int, clsRes: Int, metaRes: Int, kind: CardKind, inspectCode: String) {
    var mod = Modifier.fillMaxWidth().clip(radiusM)
    mod = when (kind) {
        CardKind.Filled -> mod.background(AppTheme.colors.surfaceVariant)
        CardKind.Elevated -> Modifier.fillMaxWidth().shadow(2.dp, radiusM).clip(radiusM).background(AppTheme.colors.surface)
        CardKind.Outlined -> mod.background(AppTheme.colors.surface).border(1.dp, AppTheme.colors.outline, radiusM)
    }
    Column(modifier = mod.inspectId(inspectCode).padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(titleRes), style = MaterialTheme.typography.titleMedium, color = AppTheme.colors.onSurface)
            Text(stringResource(clsRes), style = MaterialTheme.typography.bodySmall.copy(fontFamily = GeistMono), color = AppTheme.colors.onSurfaceVariant)
        }
        Text(stringResource(metaRes), style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.onSurfaceVariant)
    }
}
