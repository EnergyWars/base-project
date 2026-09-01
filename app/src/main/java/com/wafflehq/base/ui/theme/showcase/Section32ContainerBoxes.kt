package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppRole
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

private data class BoxLevel(val labRes: Int, val pctRes: Int)

@Composable
fun Section32ContainerBoxes() = Section(R.string.sc_s32_title, R.string.sc_s32_desc) {
    val roles = listOf(
        Triple(AppRole.Primary, R.string.sc_role_primary, R.string.sc_cb_meta_primary),
        Triple(AppRole.Secondary, R.string.sc_role_secondary, R.string.sc_cb_meta_secondary),
        Triple(AppRole.Tertiary, R.string.sc_role_tertiary, R.string.sc_cb_meta_tertiary),
        Triple(AppRole.Success, R.string.sc_role_success, R.string.sc_cb_meta_success),
        Triple(AppRole.Warning, R.string.sc_role_warning, R.string.sc_cb_meta_warning),
        Triple(AppRole.Error, R.string.sc_role_error, R.string.sc_cb_meta_error),
    )
    Panel {
        roles.forEachIndexed { index, (r, nameRes, metaRes) ->
            ContainerBoxRow(r, nameRes, metaRes, "32" + ('a' + index))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContainerBoxRow(r: AppRole, nameRes: Int, metaRes: Int, groupCode: String) {
    val rc = role(r)
    val levels = listOf(
        BoxLevel(R.string.sc_cb_lv0, R.string.sc_cb_pct0),
        BoxLevel(R.string.sc_cb_lv1, R.string.sc_cb_pct1),
        BoxLevel(R.string.sc_cb_lv2, R.string.sc_cb_pct2),
        BoxLevel(R.string.sc_cb_lv3, R.string.sc_cb_pct3),
        BoxLevel(R.string.sc_cb_lv4, R.string.sc_cb_pct4),
        BoxLevel(R.string.sc_cb_lv5, R.string.sc_cb_pct5),
    )
    Column(modifier = Modifier.inspectTap(groupCode), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(rc.accent))
            Text(stringResource(nameRes), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = AppTheme.colors.onSurface)
            Text(stringResource(metaRes), style = mono.copy(), color = AppTheme.colors.onSurfaceVariant)
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            maxItemsInEachRow = 3,
        ) {
            levels.forEachIndexed { index, level ->
                TintBox(Modifier.weight(1f), r, index, level, "$groupCode.${index + 1}")
            }
        }
    }
}

@Composable
private fun TintBox(modifier: Modifier, r: AppRole, index: Int, level: BoxLevel, inspectCode: String) {
    val rc = role(r)
    val surface = AppTheme.colors.surface
    val outline = AppTheme.colors.outline
    val bg: Color
    val border: Color
    val fg: Color
    when (index) {
        0 -> { bg = surface; border = mixColor(outline, rc.accent, 0.40f); fg = AppTheme.colors.onSurface }
        1 -> { bg = mixColor(surface, rc.accent, 0.06f); border = mixColor(outline, rc.accent, 0.16f); fg = AppTheme.colors.onSurface }
        2 -> { bg = mixColor(surface, rc.accent, 0.12f); border = mixColor(outline, rc.accent, 0.24f); fg = AppTheme.colors.onSurface }
        3 -> { bg = mixColor(surface, rc.accent, 0.22f); border = mixColor(outline, rc.accent, 0.34f); fg = AppTheme.colors.onSurface }
        4 -> { bg = rc.container; border = mixColor(outline, rc.accent, 0.38f); fg = rc.onContainer }
        else -> { bg = rc.accent; border = rc.accent; fg = rc.onAccent }
    }
    Column(
        modifier = modifier
            .heightIn(min = 84.dp)
            .clip(radiusM)
            .background(bg)
            .border(1.dp, border, radiusM)
            .inspectId(inspectCode)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(stringResource(level.labRes), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = fg)
        Text(stringResource(level.pctRes), style = mono.copy(), color = fg.copy(alpha = 0.72f))
    }
}
