package com.wafflehq.lib.uicore.button

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal data class ButtonRoleColors(
    val filledBackground: Color,
    val filledContent: Color,
    val tonalBackground: Color,
    val tonalContent: Color,
    val tonalBorder: Color,
    val elevatedBackground: Color,
    val elevatedContent: Color,
    val outlinedBorder: Color,
    val outlinedContent: Color,
    val textContent: Color,
)

internal fun customButtonRoleColors(color: Color, onColor: Color): ButtonRoleColors = ButtonRoleColors(
    filledBackground = color, filledContent = onColor,
    tonalBackground = color, tonalContent = onColor, tonalBorder = color,
    elevatedBackground = color, elevatedContent = onColor,
    outlinedBorder = color, outlinedContent = color,
    textContent = color,
)

@Composable
internal fun appButtonRoleColors(role: AppButtonRole): ButtonRoleColors {
    val scheme = MaterialTheme.colorScheme
    val warning = LocalAppButtonWarningColors.current
    return when (role) {
        AppButtonRole.Primary -> ButtonRoleColors(
            filledBackground = scheme.primary, filledContent = scheme.onPrimary,
            tonalBackground = scheme.primaryContainer, tonalContent = scheme.primary, tonalBorder = scheme.primary,
            elevatedBackground = scheme.primaryContainer, elevatedContent = scheme.primary,
            outlinedBorder = scheme.primary, outlinedContent = scheme.primary,
            textContent = scheme.primary,
        )
        AppButtonRole.Secondary -> ButtonRoleColors(
            filledBackground = scheme.secondary, filledContent = scheme.onSecondary,
            tonalBackground = scheme.secondaryContainer, tonalContent = scheme.secondary, tonalBorder = scheme.secondary,
            elevatedBackground = scheme.secondaryContainer, elevatedContent = scheme.secondary,
            outlinedBorder = scheme.secondary, outlinedContent = scheme.secondary,
            textContent = scheme.secondary,
        )
        AppButtonRole.Tertiary -> ButtonRoleColors(
            filledBackground = scheme.tertiary, filledContent = scheme.onTertiary,
            tonalBackground = scheme.tertiaryContainer, tonalContent = scheme.tertiary, tonalBorder = scheme.tertiary,
            elevatedBackground = scheme.tertiaryContainer, elevatedContent = scheme.tertiary,
            outlinedBorder = scheme.tertiary, outlinedContent = scheme.tertiary,
            textContent = scheme.tertiary,
        )
        AppButtonRole.Warning -> ButtonRoleColors(
            filledBackground = warning.warning, filledContent = warning.onWarning,
            tonalBackground = warning.warningContainer, tonalContent = warning.warning, tonalBorder = warning.warning,
            elevatedBackground = warning.warningContainer, elevatedContent = warning.warning,
            outlinedBorder = warning.warning, outlinedContent = warning.warning,
            textContent = warning.warning,
        )
        AppButtonRole.Error -> ButtonRoleColors(
            filledBackground = scheme.error, filledContent = scheme.onError,
            tonalBackground = scheme.errorContainer, tonalContent = scheme.error, tonalBorder = scheme.error,
            elevatedBackground = scheme.errorContainer, elevatedContent = scheme.error,
            outlinedBorder = scheme.error, outlinedContent = scheme.error,
            textContent = scheme.error,
        )
        AppButtonRole.Neutral -> ButtonRoleColors(
            filledBackground = scheme.surfaceVariant, filledContent = scheme.onSurfaceVariant,
            tonalBackground = scheme.surfaceVariant, tonalContent = scheme.onSurfaceVariant, tonalBorder = scheme.outline,
            elevatedBackground = scheme.surfaceVariant, elevatedContent = scheme.onSurfaceVariant,
            outlinedBorder = scheme.outline, outlinedContent = scheme.onSurfaceVariant,
            textContent = scheme.onSurfaceVariant,
        )
    }
}
