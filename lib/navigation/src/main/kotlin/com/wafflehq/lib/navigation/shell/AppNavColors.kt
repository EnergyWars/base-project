package com.wafflehq.lib.navigation.shell

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppNavColors(
    val topBarBackground: Color,
    val topBarDivider: Color,
    val topBarSelectedPillBackground: Color,
    val topBarSelectedIcon: Color,
    val topBarSelectedLabel: Color,
    val topBarUnselectedIcon: Color,
    val topBarUnselectedLabel: Color,
    val drawerBackground: Color,
    val drawerTitle: Color,
    val drawerSectionLabel: Color,
    val selectedPill: Color,
    val drawerSelectedContainer: Color,
    val drawerUnselectedContainer: Color,
    val drawerSelectedIcon: Color,
    val drawerSelectedLabel: Color,
    val drawerUnselectedIcon: Color,
    val drawerUnselectedLabel: Color,
    val selectedTile: Color,
    val selectedTileIcon: Color,
    val accents: List<Color>
) {
    fun accentAt(index: Int?): Color? =
        if (index == null || accents.isEmpty()) null else accents[index.mod(accents.size)]

    companion object {
        @Composable
        fun fromMaterialTheme(): AppNavColors {
            val scheme = MaterialTheme.colorScheme
            return AppNavColors(
                topBarBackground = scheme.surface,
                topBarDivider = scheme.outline,
                topBarSelectedPillBackground = scheme.primaryContainer,
                topBarSelectedIcon = scheme.onPrimaryContainer,
                topBarSelectedLabel = scheme.onSurface,
                topBarUnselectedIcon = scheme.onSurfaceVariant,
                topBarUnselectedLabel = scheme.onSurfaceVariant,
                drawerBackground = scheme.surface,
                drawerTitle = scheme.onSurface,
                drawerSectionLabel = scheme.onSurfaceVariant,
                selectedPill = scheme.primary,
                drawerSelectedContainer = scheme.primaryContainer,
                drawerUnselectedContainer = Color.Transparent,
                drawerSelectedIcon = scheme.onPrimaryContainer,
                drawerSelectedLabel = scheme.onPrimaryContainer,
                drawerUnselectedIcon = scheme.onSurfaceVariant,
                drawerUnselectedLabel = scheme.onSurface,
                selectedTile = scheme.primary,
                selectedTileIcon = scheme.onPrimary,
                accents = listOf(scheme.primary, scheme.error, scheme.secondary, scheme.tertiary)
            )
        }
    }
}

val LocalAppNavColors = staticCompositionLocalOf<AppNavColors?> { null }

@Composable
fun appNavColors(): AppNavColors = LocalAppNavColors.current ?: AppNavColors.fromMaterialTheme()
