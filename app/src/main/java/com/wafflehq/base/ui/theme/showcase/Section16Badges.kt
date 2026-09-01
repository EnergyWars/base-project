package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppRole
import com.wafflehq.base.ui.theme.AppTheme

@Composable
fun Section16Badges() = Section(R.string.sc_s16_title, R.string.sc_s16_desc) {
    Panel {
        WrapRow(modifier = Modifier.inspectTap("16a")) {
            IconBadge(Icons.Outlined.Notifications, "12", "16a.1")
            IconBadge(Icons.Filled.Favorite, "3", "16a.2")
            IconBadge(Icons.Outlined.Search, null, "16a.3")
        }
        WrapRow(modifier = Modifier.inspectTap("16b")) {
            Pill(R.string.sc_role_primary, AppRole.Primary, "16b.1")
            Pill(R.string.sc_role_secondary, AppRole.Secondary, "16b.2")
            Pill(R.string.sc_role_tertiary, AppRole.Tertiary, "16b.3")
            Pill(R.string.sc_role_success, AppRole.Success, "16b.4")
            Pill(R.string.sc_role_warning, AppRole.Warning, "16b.5")
            Pill(R.string.sc_role_error, AppRole.Error, "16b.6")
            Pill(R.string.sc_role_neutral, AppRole.Neutral, "16b.7")
        }
        WrapRow(modifier = Modifier.inspectTap("16c")) {
            PillText(stringResource(R.string.sc_badge_saved), AppRole.Success, "16c.1")
            PillText(stringResource(R.string.sc_badge_discarded), AppRole.Error, "16c.2")
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector, count: String?, inspectCode: String) {
    val err = AppTheme.colors.error
    Box(modifier = Modifier.size(44.dp).inspectId(inspectCode), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = AppTheme.colors.onSurface, modifier = Modifier.size(24.dp))
        if (count != null) {
            Box(
                modifier = Modifier.align(Alignment.TopEnd).clip(pill).background(err.accent).padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(count, style = mono.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = err.onAccent)
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(err.accent)
                    .border(2.dp, AppTheme.colors.surface, CircleShape),
            )
        }
    }
}

@Composable
private fun Pill(labelRes: Int, r: AppRole, inspectCode: String) = PillImpl(stringResource(labelRes), r, withDot = true, inspectCode = inspectCode)

@Composable
private fun PillText(label: String, r: AppRole, inspectCode: String) = PillImpl(label, r, withDot = false, inspectCode = inspectCode)

@Composable
private fun PillImpl(label: String, r: AppRole, withDot: Boolean, inspectCode: String) {
    val rc = role(r)
    Row(
        modifier = Modifier.clip(pill).background(rc.container).inspectId(inspectCode).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (withDot) Box(Modifier.size(6.dp).clip(CircleShape).background(rc.accent))
        Text(label, style = MaterialTheme.typography.labelMedium, color = rc.onContainer)
    }
}
