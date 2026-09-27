package com.wafflehq.base.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.wafflehq.base.data.model.ThemeMode
import com.wafflehq.base.domain.colortheme.ColorTokenCatalog
import com.wafflehq.base.domain.colortheme.tokens.GlobalColorTokens
import com.wafflehq.lib.navigation.settings.LocalSettingsTypography
import com.wafflehq.lib.navigation.settings.SettingsTypography
import com.wafflehq.lib.settings.colors.Amethyst10
import com.wafflehq.lib.settings.colors.Amethyst30
import com.wafflehq.lib.settings.colors.Amethyst40
import com.wafflehq.lib.settings.colors.Amethyst80
import com.wafflehq.lib.settings.colors.Amethyst90
import com.wafflehq.lib.settings.colors.ColorRamp
import com.wafflehq.lib.settings.colors.ColorTokenId
import com.wafflehq.lib.settings.colors.ColorTokenResolver
import com.wafflehq.lib.settings.colors.ColorValue
import com.wafflehq.lib.settings.colors.DarkBackground
import com.wafflehq.lib.settings.colors.Emerald10
import com.wafflehq.lib.settings.colors.Emerald30
import com.wafflehq.lib.settings.colors.Emerald40
import com.wafflehq.lib.settings.colors.Emerald80
import com.wafflehq.lib.settings.colors.Emerald90
import com.wafflehq.lib.settings.colors.Garnet10
import com.wafflehq.lib.settings.colors.Garnet30
import com.wafflehq.lib.settings.colors.Garnet40
import com.wafflehq.lib.settings.colors.Garnet80
import com.wafflehq.lib.settings.colors.Garnet90
import com.wafflehq.lib.settings.colors.HairlineStrongDark
import com.wafflehq.lib.settings.colors.HairlineStrongLight
import com.wafflehq.lib.settings.colors.InkDark
import com.wafflehq.lib.settings.colors.InkLight
import com.wafflehq.lib.settings.colors.LightBackground
import com.wafflehq.lib.settings.colors.LocalColorTokenRegistry
import com.wafflehq.lib.settings.colors.LocalColorTokens
import com.wafflehq.lib.settings.colors.LocalIsDarkTheme
import com.wafflehq.lib.settings.colors.MaterialRoleMapping
import com.wafflehq.lib.settings.colors.Sapphire10
import com.wafflehq.lib.settings.colors.Sapphire30
import com.wafflehq.lib.settings.colors.Sapphire40
import com.wafflehq.lib.settings.colors.Sapphire80
import com.wafflehq.lib.settings.colors.Sapphire90
import com.wafflehq.lib.settings.colors.withRoleMapping
import com.wafflehq.lib.uicore.button.AppButtonWarningColors
import com.wafflehq.lib.uicore.button.LocalAppButtonWarningColors
import com.wafflehq.lib.uicore.theme.forInput

private val DarkColorScheme = darkColorScheme(
    primary = Sapphire80,
    onPrimary = Sapphire10,
    primaryContainer = Sapphire30,
    onPrimaryContainer = Sapphire90,
    secondary = Emerald80,
    onSecondary = Emerald10,
    secondaryContainer = Emerald30,
    onSecondaryContainer = Emerald90,
    tertiary = Amethyst80,
    onTertiary = Amethyst10,
    tertiaryContainer = Amethyst30,
    onTertiaryContainer = Amethyst90,
    error = Garnet80,
    onError = Garnet10,
    errorContainer = Garnet30,
    onErrorContainer = Garnet90,
    background = DarkBackground,
    onBackground = InkDark,
    outline = HairlineStrongDark
)

private val LightColorScheme = lightColorScheme(
    primary = Sapphire40,
    onPrimary = Color.White,
    primaryContainer = Sapphire90,
    onPrimaryContainer = Sapphire10,
    secondary = Emerald40,
    onSecondary = Color.White,
    secondaryContainer = Emerald90,
    onSecondaryContainer = Emerald10,
    tertiary = Amethyst40,
    onTertiary = Color.White,
    tertiaryContainer = Amethyst90,
    onTertiaryContainer = Amethyst10,
    error = Garnet40,
    onError = Color.White,
    errorContainer = Garnet90,
    onErrorContainer = Garnet10,
    background = LightBackground,
    onBackground = InkLight,
    outline = HairlineStrongLight
)

val AppMaterialRoleMapping = MaterialRoleMapping(
    primary = GlobalColorTokens.primary,
    onPrimary = GlobalColorTokens.onPrimary,
    primaryContainer = GlobalColorTokens.primaryContainer,
    onPrimaryContainer = GlobalColorTokens.onPrimaryContainer,
    secondary = GlobalColorTokens.secondary,
    onSecondary = GlobalColorTokens.onSecondary,
    secondaryContainer = GlobalColorTokens.secondaryContainer,
    onSecondaryContainer = GlobalColorTokens.onSecondaryContainer,
    tertiary = GlobalColorTokens.tertiary,
    onTertiary = GlobalColorTokens.onTertiary,
    tertiaryContainer = GlobalColorTokens.tertiaryContainer,
    onTertiaryContainer = GlobalColorTokens.onTertiaryContainer,
    error = GlobalColorTokens.error,
    onError = GlobalColorTokens.onError,
    errorContainer = GlobalColorTokens.errorContainer,
    onErrorContainer = GlobalColorTokens.onErrorContainer,
    background = GlobalColorTokens.background,
    onBackground = GlobalColorTokens.onBackground,
    outline = GlobalColorTokens.outline
)

fun withAppRoleMapping(base: ColorScheme, resolved: Map<ColorTokenId, Color>): ColorScheme =
    base.withRoleMapping(resolved, AppMaterialRoleMapping)

fun buttonWarningColorsFrom(resolved: Map<ColorTokenId, Color>): AppButtonWarningColors = AppButtonWarningColors(
    warning = resolved.getValue(GlobalColorTokens.warning),
    onWarning = resolved.getValue(GlobalColorTokens.onWarning),
    warningContainer = resolved.getValue(GlobalColorTokens.warningContainer),
    onWarningContainer = resolved.getValue(GlobalColorTokens.onWarningContainer)
)

fun resolveDarkTheme(themeMode: ThemeMode, systemDark: Boolean): Boolean = when (themeMode) {
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
    ThemeMode.SYSTEM -> systemDark
}

private data class ResolvedTheme(
    val colorTokens: Map<ColorTokenId, Color>,
    val colorScheme: ColorScheme,
    val appColors: AppColors,
    val buttonWarningColors: AppButtonWarningColors
)

@Composable
fun BaseAppTheme(
    themeMode: ThemeMode,
    colorOverridesLight: Map<ColorTokenId, ColorValue> = emptyMap(),
    colorOverridesDark: Map<ColorTokenId, ColorValue> = emptyMap(),
    baseRampOverrides: Map<ColorRamp, Color> = emptyMap(),
    content: @Composable () -> Unit
) {
    val isDark = resolveDarkTheme(themeMode, isSystemInDarkTheme())
    val colorOverrides = if (isDark) colorOverridesDark else colorOverridesLight
    val resolvedTheme = remember(colorOverrides, isDark, baseRampOverrides) {
        val resolvedColorTokens = ColorTokenResolver.resolveAll(
            registry = ColorTokenCatalog.registry,
            overrides = colorOverrides,
            isDark = isDark,
            baseRampOverrides = baseRampOverrides
        )
        val colorScheme = withAppRoleMapping(if (isDark) DarkColorScheme else LightColorScheme, resolvedColorTokens)
        ResolvedTheme(
            colorTokens = resolvedColorTokens,
            colorScheme = colorScheme,
            appColors = buildAppColors(resolvedColorTokens, colorScheme),
            buttonWarningColors = buttonWarningColorsFrom(resolvedColorTokens)
        )
    }
    val baseTextStyle = BaseAppTypography.bodyLarge.forInput()
    CompositionLocalProvider(
        LocalIsDarkTheme provides isDark,
        LocalColorTokens provides resolvedTheme.colorTokens,
        LocalColorTokenRegistry provides ColorTokenCatalog.registry,
        LocalAppColors provides resolvedTheme.appColors,
        LocalAppButtonWarningColors provides resolvedTheme.buttonWarningColors,
        LocalTextStyle provides baseTextStyle,
        LocalSettingsTypography provides remember {
            SettingsTypography(labelFontFamily = GeistMono, inputStyle = { it.forInput() })
        }
    ) {
        MaterialTheme(
            colorScheme = resolvedTheme.colorScheme,
            typography = BaseAppTypography,
            shapes = BaseAppShapes,
            content = content
        )
    }
}
