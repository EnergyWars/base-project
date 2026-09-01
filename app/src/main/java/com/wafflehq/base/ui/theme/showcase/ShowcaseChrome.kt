package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

@Composable
fun ShowcaseLede() {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Text(
            text = stringResource(R.string.sc_eyebrow),
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono.fontFamily, letterSpacing = 1.sp),
            color = AppTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.sc_title),
            style = MaterialTheme.typography.displaySmall,
            color = AppTheme.colors.onBackground,
        )
        Text(
            text = stringResource(R.string.sc_lede),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
fun ShowcaseThemeToggle(dark: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Row(
            modifier = Modifier
                .clip(pill)
                .border(1.dp, AppTheme.colors.outline, pill),
        ) {
            ToggleSegment(stringResource(R.string.sc_toggle_light), selected = !dark) { onToggle(false) }
            ToggleSegment(stringResource(R.string.sc_toggle_dark), selected = dark) { onToggle(true) }
        }
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier
                .clip(pill)
                .background(AppTheme.colors.primary.accent)
                .clickable {}
                .padding(horizontal = AppSpacing.lg, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(Icons.Outlined.Download, null, tint = AppTheme.colors.primary.onAccent, modifier = Modifier.size(16.dp))
            Text(
                stringResource(R.string.sc_download),
                style = MaterialTheme.typography.labelLarge,
                color = AppTheme.colors.primary.onAccent,
            )
        }
    }
}

@Composable
private fun ToggleSegment(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (selected) AppTheme.colors.primary.accent else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) AppTheme.colors.primary.onAccent else AppTheme.colors.onSurfaceVariant,
        )
    }
}
