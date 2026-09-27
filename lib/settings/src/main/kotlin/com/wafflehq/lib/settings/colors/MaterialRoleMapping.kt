package com.wafflehq.lib.settings.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

data class MaterialRoleMapping(
    val primary: ColorTokenId,
    val onPrimary: ColorTokenId,
    val primaryContainer: ColorTokenId,
    val onPrimaryContainer: ColorTokenId,
    val secondary: ColorTokenId,
    val onSecondary: ColorTokenId,
    val secondaryContainer: ColorTokenId,
    val onSecondaryContainer: ColorTokenId,
    val tertiary: ColorTokenId,
    val onTertiary: ColorTokenId,
    val tertiaryContainer: ColorTokenId,
    val onTertiaryContainer: ColorTokenId,
    val error: ColorTokenId,
    val onError: ColorTokenId,
    val errorContainer: ColorTokenId,
    val onErrorContainer: ColorTokenId,
    val background: ColorTokenId,
    val onBackground: ColorTokenId,
    val outline: ColorTokenId
)

fun ColorScheme.withRoleMapping(
    resolved: Map<ColorTokenId, Color>,
    mapping: MaterialRoleMapping
): ColorScheme {
    val primary = resolved[mapping.primary] ?: this.primary
    val onPrimary = resolved[mapping.onPrimary] ?: this.onPrimary
    val primaryContainer = resolved[mapping.primaryContainer] ?: this.primaryContainer
    val onPrimaryContainer = resolved[mapping.onPrimaryContainer] ?: this.onPrimaryContainer
    val secondary = resolved[mapping.secondary] ?: this.secondary
    val onSecondary = resolved[mapping.onSecondary] ?: this.onSecondary
    val secondaryContainer = resolved[mapping.secondaryContainer] ?: this.secondaryContainer
    val onSecondaryContainer = resolved[mapping.onSecondaryContainer] ?: this.onSecondaryContainer
    val tertiary = resolved[mapping.tertiary] ?: this.tertiary
    val onTertiary = resolved[mapping.onTertiary] ?: this.onTertiary
    val tertiaryContainer = resolved[mapping.tertiaryContainer] ?: this.tertiaryContainer
    val onTertiaryContainer = resolved[mapping.onTertiaryContainer] ?: this.onTertiaryContainer
    val error = resolved[mapping.error] ?: this.error
    val onError = resolved[mapping.onError] ?: this.onError
    val errorContainer = resolved[mapping.errorContainer] ?: this.errorContainer
    val onErrorContainer = resolved[mapping.onErrorContainer] ?: this.onErrorContainer
    val background = resolved[mapping.background] ?: this.background
    val onBackground = resolved[mapping.onBackground] ?: this.onBackground
    val outline = resolved[mapping.outline] ?: this.outline

    return copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        background = background,
        onBackground = onBackground,
        surface = background,
        onSurface = onBackground,
        surfaceVariant = background,
        onSurfaceVariant = onBackground,
        surfaceContainer = background,
        surfaceContainerLow = background,
        surfaceContainerHigh = background,
        surfaceTint = primary,
        inversePrimary = primary,
        outline = outline,
        outlineVariant = outline,
        inverseSurface = onBackground,
        inverseOnSurface = background
    )
}
