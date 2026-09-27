package com.wafflehq.lib.folders.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.wafflehq.lib.uicore.components.AppStatusPill
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.components.AppLabelChip

@Composable
fun FolderIconTile(
    accent: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emoji: String? = null,
    size: Dp = FolderListDefaults.entryTileSize,
    glyphSize: Dp = FolderListDefaults.entryTileGlyphSize
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(AppRadius.card))
            .background(accent.copy(alpha = FolderListDefaults.tileAlpha)),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(glyphSize)
            )
        } else if (emoji != null) {
            Text(text = emoji, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun FolderEntryTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    maxLines: Int = 1
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
fun FolderMetaLine(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    maxLines: Int = 1
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = FolderListDefaults.metaAlpha),
                modifier = Modifier.size(FolderListDefaults.metaIconSize)
            )
        }
        Text(
            text = text,
            style = style,
            color = color,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderMetaFlow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        content = { content() }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderTagRow(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    if (tags.isEmpty()) return
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        tags.forEach { tag ->
            AppLabelChip(text = tag)
        }
    }
}
