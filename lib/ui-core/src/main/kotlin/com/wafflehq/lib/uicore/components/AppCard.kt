package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.button.LocalAppButtonWarningColors
import com.wafflehq.lib.uicore.theme.AppRadius

enum class AppCardVariant { Outlined, Filled }

enum class AppCardRole { Primary, Secondary, Tertiary, Warning, Error, Neutral }

object AppCardDefaults {
    val borderWidth: Dp = 1.dp
    const val BORDER_ALPHA: Float = 0.4f
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    role: AppCardRole = AppCardRole.Neutral,
    variant: AppCardVariant = AppCardVariant.Outlined,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = appCardRoleColors(role, variant)
    AppCardImpl(colors.container, colors.content, colors.border, AppCardDefaults.borderWidth, modifier, onClick, enabled, content)
}

@Composable
fun AppCard(
    containerColor: Color,
    contentColor: Color = contentColorFor(containerColor),
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    borderWidth: Dp = AppCardDefaults.borderWidth,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) = AppCardImpl(containerColor, contentColor, borderColor, borderWidth, modifier, onClick, enabled, content)

@Composable
private fun AppCardImpl(
    containerColor: Color,
    contentColor: Color,
    borderColor: Color?,
    borderWidth: Dp,
    modifier: Modifier,
    onClick: (() -> Unit)?,
    enabled: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    val border = borderColor?.let { BorderStroke(borderWidth, it) }
    val disabledContainer = MaterialTheme.colorScheme.surfaceVariant
    val disabledContent = MaterialTheme.colorScheme.onSurfaceVariant
    val colors = CardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainer,
        disabledContentColor = disabledContent,
    )

    if (onClick == null) {
        Card(modifier = modifier, shape = shape, colors = colors, elevation = elevation, border = border, content = content)
    } else {
        Card(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content,
        )
    }
}

@Immutable
private data class AppCardColors(val container: Color, val content: Color, val border: Color?)

@Composable
private fun appCardRoleColors(role: AppCardRole, variant: AppCardVariant): AppCardColors {
    val scheme = MaterialTheme.colorScheme
    val warning = LocalAppButtonWarningColors.current
    val accent = when (role) {
        AppCardRole.Primary -> scheme.primary
        AppCardRole.Secondary -> scheme.secondary
        AppCardRole.Tertiary -> scheme.tertiary
        AppCardRole.Warning -> warning.warning
        AppCardRole.Error -> scheme.error
        AppCardRole.Neutral -> scheme.outline
    }
    return when (variant) {
        AppCardVariant.Outlined -> AppCardColors(
            container = scheme.surface,
            content = scheme.onSurface,
            border = accent.copy(alpha = AppCardDefaults.BORDER_ALPHA),
        )

        AppCardVariant.Filled -> AppCardColors(
            container = when (role) {
                AppCardRole.Primary -> scheme.primaryContainer
                AppCardRole.Secondary -> scheme.secondaryContainer
                AppCardRole.Tertiary -> scheme.tertiaryContainer
                AppCardRole.Warning -> warning.warningContainer
                AppCardRole.Error -> scheme.errorContainer
                AppCardRole.Neutral -> scheme.surfaceVariant
            },
            content = when (role) {
                AppCardRole.Primary -> scheme.onPrimaryContainer
                AppCardRole.Secondary -> scheme.onSecondaryContainer
                AppCardRole.Tertiary -> scheme.onTertiaryContainer
                AppCardRole.Warning -> warning.onWarningContainer
                AppCardRole.Error -> scheme.onErrorContainer
                AppCardRole.Neutral -> scheme.onSurfaceVariant
            },
            border = null,
        )
    }
}
