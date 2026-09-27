package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.button.LocalAppButtonWarningColors
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.theme.AppSpacing

enum class AppBannerRole { Primary, Secondary, Tertiary, Warning, Error, Neutral }

enum class AppBannerVariant { Strip, Card }

object AppBannerDefaults {
    val iconSize: Dp = 20.dp
    val cardShape = RoundedCornerShape(AppRadius.card)
}

@Composable
fun AppBanner(
    text: String,
    modifier: Modifier = Modifier,
    role: AppBannerRole = AppBannerRole.Secondary,
    variant: AppBannerVariant = AppBannerVariant.Strip,
    title: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues? = null,
    borderColor: Color? = null,
    textModifier: Modifier = Modifier,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    action: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = appBannerRoleColors(role)
    AppBanner(
        text = text,
        containerColor = colors.container,
        contentColor = colors.content,
        modifier = modifier,
        variant = variant,
        title = title,
        icon = icon,
        onClick = onClick,
        contentPadding = contentPadding,
        borderColor = borderColor,
        textModifier = textModifier,
        footer = footer,
        action = action,
    )
}

@Composable
fun AppBanner(
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = contentColorFor(containerColor),
    variant: AppBannerVariant = AppBannerVariant.Strip,
    title: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues? = null,
    borderColor: Color? = null,
    textModifier: Modifier = Modifier,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    action: (@Composable RowScope.() -> Unit)? = null,
) {
    val shape = if (variant == AppBannerVariant.Card) AppBannerDefaults.cardShape else RectangleShape
    val vertical = if (variant == AppBannerVariant.Card) AppSpacing.md else AppSpacing.sm
    val padding = contentPadding ?: PaddingValues(horizontal = AppSpacing.lg, vertical = vertical)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .then(if (borderColor != null) Modifier.border(BorderStroke(1.dp, borderColor), shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(AppBannerDefaults.iconSize))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (title != null) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, color = contentColor)
            }
            Text(
                text = text,
                style = if (title != null) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelLarge,
                color = contentColor,
                modifier = textModifier,
            )
            if (footer != null) footer()
        }
        if (action != null) action()
    }
}

@Immutable
private data class AppBannerColors(val container: Color, val content: Color)

@Composable
private fun appBannerRoleColors(role: AppBannerRole): AppBannerColors {
    val scheme = MaterialTheme.colorScheme
    val warning = LocalAppButtonWarningColors.current
    return when (role) {
        AppBannerRole.Primary -> AppBannerColors(scheme.primaryContainer, scheme.onPrimaryContainer)
        AppBannerRole.Secondary -> AppBannerColors(scheme.secondaryContainer, scheme.onSecondaryContainer)
        AppBannerRole.Tertiary -> AppBannerColors(scheme.tertiaryContainer, scheme.onTertiaryContainer)
        AppBannerRole.Warning -> AppBannerColors(warning.warningContainer, warning.onWarningContainer)
        AppBannerRole.Error -> AppBannerColors(scheme.errorContainer, scheme.onErrorContainer)
        AppBannerRole.Neutral -> AppBannerColors(scheme.surfaceVariant, scheme.onSurfaceVariant)
    }
}
