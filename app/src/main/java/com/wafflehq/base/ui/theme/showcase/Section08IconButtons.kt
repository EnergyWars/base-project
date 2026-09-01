package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppRole
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

@Composable
fun Section08IconButtons() = Section(R.string.sc_s8_title, R.string.sc_s8_desc) {
    Panel(modifier = Modifier.inspectTap("8a")) {
        Subhead(stringResource(R.string.sc_role_primary))
        WrapRow {
            IconBtn(Icons.Outlined.Edit, AppRole.Primary, IconVariant.Standard, inspectCode = "8a.1")
            IconBtn(Icons.Outlined.Check, AppRole.Primary, IconVariant.Filled, inspectCode = "8a.2")
            IconBtn(Icons.Outlined.Search, AppRole.Primary, IconVariant.Tonal, inspectCode = "8a.3")
            IconBtn(Icons.Outlined.Share, AppRole.Primary, IconVariant.Outlined, inspectCode = "8a.4")
            Box(Modifier.height(44.dp).width(1.dp).background(AppTheme.colors.outline))
            IconBtn(Icons.Outlined.Edit, AppRole.Primary, IconVariant.Standard, enabled = false, inspectCode = "8a.5")
            IconBtn(Icons.Outlined.Check, AppRole.Primary, IconVariant.Filled, enabled = false, inspectCode = "8a.6")
            IconBtn(Icons.Outlined.Search, AppRole.Primary, IconVariant.Tonal, enabled = false, inspectCode = "8a.7")
        }
    }
    Spacer(Modifier.height(AppSpacing.md))
    Panel {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxl)) {
            Column(modifier = Modifier.inspectTap("8b"), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Subhead(stringResource(R.string.sc_role_error))
                IconBtn(Icons.Outlined.DeleteOutline, AppRole.Error, IconVariant.Filled, inspectCode = "8b.1")
            }
            Column(modifier = Modifier.inspectTap("8c"), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Subhead(stringResource(R.string.sc_role_success))
                IconBtn(Icons.Filled.Favorite, AppRole.Success, IconVariant.Tonal, inspectCode = "8c.1")
            }
            Column(modifier = Modifier.inspectTap("8d"), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Subhead(stringResource(R.string.sc_role_tertiary))
                IconBtn(Icons.Outlined.Info, AppRole.Tertiary, IconVariant.Outlined, inspectCode = "8d.1")
            }
        }
    }
}

private enum class IconVariant { Standard, Filled, Tonal, Outlined }

@Composable
private fun IconBtn(icon: ImageVector, r: AppRole, variant: IconVariant, enabled: Boolean = true, inspectCode: String? = null) {
    val rc = role(r)
    val alpha = if (enabled) 1f else 0.38f
    var mod = Modifier.size(44.dp).clip(CircleShape)
    val tint: Color
    when (variant) {
        IconVariant.Standard -> tint = rc.accent
        IconVariant.Filled -> { mod = mod.background(rc.accent.copy(alpha = alpha)); tint = rc.onAccent }
        IconVariant.Tonal -> { mod = mod.background(rc.container.copy(alpha = alpha)); tint = rc.onContainer }
        IconVariant.Outlined -> { mod = mod.border(1.5.dp, rc.accent.copy(alpha = alpha), CircleShape); tint = rc.accent }
    }
    Box(modifier = mod.then(if (inspectCode != null) Modifier.inspectId(inspectCode) else Modifier.clickable(enabled = enabled) {}), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = tint.copy(alpha = alpha), modifier = Modifier.size(22.dp))
    }
}
