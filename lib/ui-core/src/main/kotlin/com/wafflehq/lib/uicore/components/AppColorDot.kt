package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.theme.AppSpacing

object AppColorDotDefaults {
    val size: Dp = 10.dp
    val borderWidth: Dp = 1.dp
}

@Composable
fun AppColorDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = AppColorDotDefaults.size,
    borderColor: Color? = null,
    borderWidth: Dp = AppColorDotDefaults.borderWidth,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClickLabel = onClickLabel, onClick = onClick) else Modifier)
            .background(color)
            .border(if (borderColor != null) borderWidth else 0.dp, borderColor ?: Color.Transparent, CircleShape),
    )
}

@Composable
fun AppLegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
    dotSize: Dp = AppColorDotDefaults.size,
    borderColor: Color? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        AppColorDot(color = color, size = dotSize, borderColor = borderColor)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
