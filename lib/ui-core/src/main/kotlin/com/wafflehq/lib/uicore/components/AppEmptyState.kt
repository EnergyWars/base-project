package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.theme.AppSpacing

object AppEmptyStateDefaults {
    val iconSize: Dp = 64.dp
    const val ICON_ALPHA: Float = 0.4f
}

@Composable
fun AppEmptyState(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AppEmptyStateDefaults.ICON_ALPHA),
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    actionModifier: Modifier = Modifier,
    actionContainerColor: Color? = null,
    actionContentColor: Color? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md, Alignment.CenterVertically),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppEmptyStateDefaults.iconSize),
                tint = iconTint,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            if (actionContainerColor != null && actionContentColor != null) {
                AppButton(
                    text = actionLabel,
                    containerColor = actionContainerColor,
                    contentColor = actionContentColor,
                    variant = AppButtonVariant.Tonal,
                    onClick = onAction,
                    modifier = actionModifier,
                )
            } else {
                AppButton(
                    text = actionLabel,
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Tonal,
                    onClick = onAction,
                    modifier = actionModifier,
                )
            }
        }
        if (content != null) content()
    }
}
