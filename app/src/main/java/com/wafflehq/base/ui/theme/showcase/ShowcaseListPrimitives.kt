package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wafflehq.base.ui.theme.AppRole
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

internal val phoneShape = RoundedCornerShape(22.dp)

@Composable
internal fun ListCard(caption: String, groupCode: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Subhead(caption)
        PhoneFrame(
            modifier = if (groupCode != null) Modifier.inspectTap(groupCode) else Modifier,
            content = content,
        )
    }
}

@Composable
internal fun PhoneFrame(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(phoneShape)
            .border(1.dp, AppTheme.colors.outline, phoneShape)
            .background(AppTheme.colors.background),
        content = content,
    )
}

@Composable
internal fun ListHead(title: String, count: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface)
            .heightIn(min = 56.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = AppTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (count != null) {
            Text(count, style = mono.copy(), color = AppTheme.colors.onSurfaceVariant, maxLines = 1)
        }
    }
    Divider()
}

@Composable
internal fun ListAvatar(initials: String, role: AppRole = AppRole.Secondary) {
    val rc = role(role)
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape).background(rc.container),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = rc.onContainer)
    }
}

@Composable
internal fun ListLeadIcon(icon: ImageVector) {
    Box(
        modifier = Modifier.size(40.dp).clip(radiusS).background(AppTheme.colors.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
internal fun ListPrimaryBadge(text: String) {
    val rc = AppTheme.colors.primary
    Box(
        modifier = Modifier.heightIn(min = 20.dp).clip(pill).background(rc.accent).padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = mono.copy(fontWeight = FontWeight.Bold), color = rc.onAccent, maxLines = 1)
    }
}

@Composable
internal fun ListEmpty(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.onSurfaceVariant)
    }
}

@Composable
internal fun RowScope.RowBody(title: String, sub: String? = null) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (sub != null) {
            Text(sub, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun TrailingText(text: String) {
    Text(text, style = mono.copy(), color = AppTheme.colors.onSurfaceVariant, maxLines = 1)
}

@Composable
internal fun ListItemRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

internal fun mixColor(base: Color, accent: Color, fraction: Float): Color = lerp(base, accent, fraction)

@Composable
internal fun ListSearchField(
    query: String,
    onQuery: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .clip(pill)
            .border(1.5.dp, AppTheme.colors.outline, pill)
            .background(AppTheme.colors.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(leadingIcon, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.onSurfaceVariant)
            }
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = AppTheme.colors.onSurface),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(AppTheme.colors.primary.accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun ListFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val rc = AppTheme.colors.secondary
    val bg = if (selected) rc.container else AppTheme.colors.surface
    val fg = if (selected) rc.onContainer else AppTheme.colors.onSurface
    Box(
        modifier = Modifier
            .clip(pill)
            .background(bg)
            .then(if (!selected) Modifier.border(1.dp, AppTheme.colors.outline, pill) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}
