package com.wafflehq.base.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.wafflehq.base.domain.colortheme.ColorTokenCatalog
import com.wafflehq.base.domain.colortheme.tokens.GlobalColorTokens
import com.wafflehq.base.domain.colortheme.tokens.SuccessColorTokens
import com.wafflehq.lib.settings.colors.ColorRamp
import com.wafflehq.lib.settings.colors.ColorRampSteps
import com.wafflehq.lib.settings.colors.ColorRampTable
import com.wafflehq.lib.settings.colors.ColorTokenId
import com.wafflehq.lib.settings.colors.ColorTokenResolver

enum class AppRole {
    Primary,
    Secondary,
    Tertiary,
    Success,
    Warning,
    Error,
    Neutral
}

@Immutable
data class RoleColors(
    val accent: Color,
    val onAccent: Color,
    val container: Color,
    val onContainer: Color,
    val tonalBorder: Color
)

@Immutable
data class AppColors(
    val primary: RoleColors,
    val secondary: RoleColors,
    val tertiary: RoleColors,
    val success: RoleColors,
    val warning: RoleColors,
    val error: RoleColors,
    val neutral: RoleColors,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surface3: Color,
    val outline: Color
) {
    fun forRole(role: AppRole): RoleColors = when (role) {
        AppRole.Primary -> primary
        AppRole.Secondary -> secondary
        AppRole.Tertiary -> tertiary
        AppRole.Success -> success
        AppRole.Warning -> warning
        AppRole.Error -> error
        AppRole.Neutral -> neutral
    }
}

private fun roleColors(
    resolved: Map<ColorTokenId, Color>,
    accent: ColorTokenId,
    onAccent: ColorTokenId,
    container: ColorTokenId,
    onContainer: ColorTokenId
): RoleColors {
    val accentColor = resolved.getValue(accent)
    return RoleColors(
        accent = accentColor,
        onAccent = resolved.getValue(onAccent),
        container = resolved.getValue(container),
        onContainer = resolved.getValue(onContainer),
        tonalBorder = accentColor
    )
}

fun buildAppColors(resolved: Map<ColorTokenId, Color>, scheme: ColorScheme): AppColors = AppColors(
    primary = roleColors(
        resolved,
        GlobalColorTokens.primary,
        GlobalColorTokens.onPrimary,
        GlobalColorTokens.primaryContainer,
        GlobalColorTokens.onPrimaryContainer
    ),
    secondary = roleColors(
        resolved,
        GlobalColorTokens.secondary,
        GlobalColorTokens.onSecondary,
        GlobalColorTokens.secondaryContainer,
        GlobalColorTokens.onSecondaryContainer
    ),
    tertiary = roleColors(
        resolved,
        GlobalColorTokens.tertiary,
        GlobalColorTokens.onTertiary,
        GlobalColorTokens.tertiaryContainer,
        GlobalColorTokens.onTertiaryContainer
    ),
    success = roleColors(
        resolved,
        SuccessColorTokens.success,
        SuccessColorTokens.onSuccess,
        SuccessColorTokens.successContainer,
        SuccessColorTokens.onSuccessContainer
    ),
    warning = roleColors(
        resolved,
        GlobalColorTokens.warning,
        GlobalColorTokens.onWarning,
        GlobalColorTokens.warningContainer,
        GlobalColorTokens.onWarningContainer
    ),
    error = roleColors(
        resolved,
        GlobalColorTokens.error,
        GlobalColorTokens.onError,
        GlobalColorTokens.errorContainer,
        GlobalColorTokens.onErrorContainer
    ),
    neutral = RoleColors(
        accent = scheme.onSurfaceVariant,
        onAccent = scheme.surfaceVariant,
        container = scheme.surfaceVariant,
        onContainer = scheme.onSurfaceVariant,
        tonalBorder = scheme.outline
    ),
    background = scheme.background,
    onBackground = scheme.onBackground,
    surface = scheme.surface,
    onSurface = scheme.onSurface,
    surfaceVariant = scheme.surfaceVariant,
    onSurfaceVariant = scheme.onSurfaceVariant,
    surface3 = scheme.surfaceContainerHigh,
    outline = scheme.outline
)

private fun defaultAppColors(): AppColors {
    val resolved = ColorTokenResolver.resolveAll(
        registry = ColorTokenCatalog.registry,
        overrides = emptyMap(),
        isDark = false,
        baseRampOverrides = emptyMap()
    )
    return buildAppColors(resolved, withAppRoleMapping(lightColorScheme(), resolved))
}

val LocalAppColors = staticCompositionLocalOf { defaultAppColors() }

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current
}

private val ColorRampDisplayNames: Map<ColorRamp, String> =
    ColorRamp.entries.associateWith { ramp -> ramp.name.lowercase().replaceFirstChar { it.uppercase() } }

val AppTheme.colorRamps: List<Pair<String, List<Color>>>
    get() = ColorRamp.entries.map { ramp ->
        ColorRampDisplayNames.getValue(ramp) to ColorRampSteps.map { step -> ColorRampTable.swatch(ramp, step) }
    }
