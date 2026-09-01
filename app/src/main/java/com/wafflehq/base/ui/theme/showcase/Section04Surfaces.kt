package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

@Composable
fun Section04Surfaces() = Section(R.string.sc_s4_title, R.string.sc_s4_desc) {
    val c = AppTheme.colors
    Panel {
        SurfaceRow(c.background, R.string.sc_surf_bg, R.string.sc_surf_bg_d, "4a.1", border = true)
        SurfaceRow(c.surface, R.string.sc_surf_surface, R.string.sc_surf_surface_d, "4a.2", border = true)
        SurfaceRow(c.surfaceVariant, R.string.sc_surf_variant, R.string.sc_surf_variant_d, "4a.3", border = true)
        SurfaceRow(c.outline, R.string.sc_surf_outline, R.string.sc_surf_outline_d, "4a.4")
        SurfaceRow(c.onSurface, R.string.sc_surf_on, R.string.sc_surf_on_d, "4a.5")
        SurfaceRow(c.onSurfaceVariant, R.string.sc_surf_onvar, R.string.sc_surf_onvar_d, "4a.6")
    }
}

@Composable
private fun SurfaceRow(color: Color, titleRes: Int, descRes: Int, id: String, border: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 44.dp)
                .clip(radiusXs)
                .background(color)
                .then(if (border) Modifier.border(1.dp, AppTheme.colors.outline, radiusXs) else Modifier)
                .inspectId(id),
        )
        Column {
            Text(stringResource(titleRes), style = MaterialTheme.typography.titleSmall, color = AppTheme.colors.onSurface)
            Text(stringResource(descRes), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
        }
    }
}
