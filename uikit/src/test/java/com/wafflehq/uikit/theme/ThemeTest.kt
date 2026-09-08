package com.wafflehq.uikit.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTest {

    private val palette = WafflePalette.Default

    @Test
    fun `roleColors light uses tone 40 as accent with white on-accent`() {
        val colors = palette.primary.roleColors(dark = false)
        assertEquals(palette.primary["40"], colors.accent)
        assertEquals(Color.White, colors.onAccent)
        assertEquals(palette.primary["90"], colors.container)
        assertEquals(palette.primary["10"], colors.onContainer)
        assertEquals(palette.primary["30"], colors.tonalBorder)
    }

    @Test
    fun `roleColors dark uses tone 80 as accent`() {
        val colors = palette.primary.roleColors(dark = true)
        assertEquals(palette.primary["80"], colors.accent)
        assertEquals(palette.primary["20"], colors.onAccent)
        assertEquals(palette.primary["30"], colors.container)
        assertEquals(palette.primary["90"], colors.onContainer)
        assertEquals(palette.primary["70"], colors.tonalBorder)
    }

    @Test
    fun `buildAppColors light maps every role and surface`() {
        val colors = buildAppColors(palette, dark = false)
        assertEquals(palette.primary.roleColors(false), colors.primary)
        assertEquals(palette.secondary.roleColors(false), colors.secondary)
        assertEquals(palette.tertiary.roleColors(false), colors.tertiary)
        assertEquals(palette.success.roleColors(false), colors.success)
        assertEquals(palette.warning.roleColors(false), colors.warning)
        assertEquals(palette.error.roleColors(false), colors.error)
        assertEquals(palette.neutral.roleColors(false), colors.neutral)
        assertEquals(palette.lightBackground, colors.background)
        assertEquals(palette.onSurfaceLight, colors.onBackground)
        assertEquals(palette.lightSurface, colors.surface)
        assertEquals(palette.onSurfaceLight, colors.onSurface)
        assertEquals(palette.lightSurfaceVariant, colors.surfaceVariant)
        assertEquals(palette.onSurfaceVariantLight, colors.onSurfaceVariant)
        assertEquals(palette.lightSurface3, colors.surface3)
        assertEquals(palette.outlineLight, colors.outline)
    }

    @Test
    fun `buildAppColors dark maps every role and surface`() {
        val colors = buildAppColors(palette, dark = true)
        assertEquals(palette.primary.roleColors(true), colors.primary)
        assertEquals(palette.darkBackground, colors.background)
        assertEquals(palette.onSurfaceDark, colors.onBackground)
        assertEquals(palette.darkSurface, colors.surface)
        assertEquals(palette.onSurfaceDark, colors.onSurface)
        assertEquals(palette.darkSurfaceVariant, colors.surfaceVariant)
        assertEquals(palette.onSurfaceVariantDark, colors.onSurfaceVariant)
        assertEquals(palette.darkSurface3, colors.surface3)
        assertEquals(palette.outlineDark, colors.outline)
    }

    @Test
    fun `AppColors forRole dispatches to the correct role`() {
        val colors = buildAppColors(palette, dark = false)
        assertEquals(colors.primary, colors.forRole(AppRole.Primary))
        assertEquals(colors.secondary, colors.forRole(AppRole.Secondary))
        assertEquals(colors.tertiary, colors.forRole(AppRole.Tertiary))
        assertEquals(colors.success, colors.forRole(AppRole.Success))
        assertEquals(colors.warning, colors.forRole(AppRole.Warning))
        assertEquals(colors.error, colors.forRole(AppRole.Error))
        assertEquals(colors.neutral, colors.forRole(AppRole.Neutral))
    }

    @Test
    fun `buildExtendedColors light exposes success warning neutral accents`() {
        val extended = buildExtendedColors(palette, dark = false)
        val success = palette.success.roleColors(false)
        val warning = palette.warning.roleColors(false)
        val neutral = palette.neutral.roleColors(false)
        assertEquals(success.accent, extended.success)
        assertEquals(success.onAccent, extended.onSuccess)
        assertEquals(success.container, extended.successContainer)
        assertEquals(success.onContainer, extended.onSuccessContainer)
        assertEquals(warning.accent, extended.warning)
        assertEquals(warning.onAccent, extended.onWarning)
        assertEquals(warning.container, extended.warningContainer)
        assertEquals(warning.onContainer, extended.onWarningContainer)
        assertEquals(neutral.accent, extended.neutral)
        assertEquals(neutral.onAccent, extended.onNeutral)
        assertEquals(neutral.container, extended.neutralContainer)
        assertEquals(neutral.onContainer, extended.onNeutralContainer)
    }

    @Test
    fun `buildExtendedColors dark uses dark role colors`() {
        val extended = buildExtendedColors(palette, dark = true)
        assertEquals(palette.success.roleColors(true).accent, extended.success)
    }

    @Test
    fun `buildMaterialColorScheme light matches app colors`() {
        val scheme = buildMaterialColorScheme(palette, dark = false)
        val primary = palette.primary.roleColors(false)
        assertEquals(primary.accent, scheme.primary)
        assertEquals(primary.onAccent, scheme.onPrimary)
        assertEquals(primary.container, scheme.primaryContainer)
        assertEquals(primary.onContainer, scheme.onPrimaryContainer)
        assertEquals(palette.lightBackground, scheme.background)
        assertEquals(palette.onSurfaceLight, scheme.onBackground)
        assertEquals(palette.lightSurface, scheme.surface)
        assertEquals(palette.darkSurfaceVariant, scheme.inverseSurface)
        assertEquals(palette.lightBackground, scheme.inverseOnSurface)
    }

    @Test
    fun `buildMaterialColorScheme dark matches app colors`() {
        val scheme = buildMaterialColorScheme(palette, dark = true)
        val primary = palette.primary.roleColors(true)
        assertEquals(primary.accent, scheme.primary)
        assertEquals(palette.darkBackground, scheme.background)
        assertEquals(palette.onSurfaceDark, scheme.onBackground)
        assertEquals(palette.darkSurface, scheme.surface)
        assertEquals(palette.lightBackground, scheme.inverseSurface)
        assertEquals(palette.darkBackground, scheme.inverseOnSurface)
    }
}
