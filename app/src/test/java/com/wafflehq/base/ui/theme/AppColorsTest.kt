package com.wafflehq.base.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.wafflehq.base.domain.colortheme.ColorTokenCatalog
import com.wafflehq.base.domain.colortheme.tokens.GlobalColorTokens
import com.wafflehq.base.domain.colortheme.tokens.SuccessColorTokens
import com.wafflehq.lib.settings.colors.ColorRamp
import com.wafflehq.lib.settings.colors.ColorTokenId
import com.wafflehq.lib.settings.colors.ColorTokenResolver
import com.wafflehq.lib.settings.colors.ColorValue
import com.wafflehq.lib.settings.colors.Emerald40
import com.wafflehq.lib.settings.colors.Sapphire10
import com.wafflehq.lib.settings.colors.Sapphire40
import com.wafflehq.lib.settings.colors.Sapphire90
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppColorsTest {

    private fun resolved(dark: Boolean, overrides: Map<ColorTokenId, ColorValue> = emptyMap()): Map<ColorTokenId, Color> =
        ColorTokenResolver.resolveAll(ColorTokenCatalog.registry, overrides, dark)

    private fun scheme(dark: Boolean, resolved: Map<ColorTokenId, Color>): ColorScheme =
        withAppRoleMapping(if (dark) darkColorScheme() else lightColorScheme(), resolved)

    private fun colors(dark: Boolean, overrides: Map<ColorTokenId, ColorValue> = emptyMap()): AppColors {
        val tokens = resolved(dark, overrides)
        return buildAppColors(tokens, scheme(dark, tokens))
    }

    private data class RoleTokens(val accent: ColorTokenId, val onAccent: ColorTokenId, val container: ColorTokenId, val onContainer: ColorTokenId)

    private val roleTokens = mapOf(
        AppRole.Primary to RoleTokens(
            GlobalColorTokens.primary, GlobalColorTokens.onPrimary,
            GlobalColorTokens.primaryContainer, GlobalColorTokens.onPrimaryContainer
        ),
        AppRole.Secondary to RoleTokens(
            GlobalColorTokens.secondary, GlobalColorTokens.onSecondary,
            GlobalColorTokens.secondaryContainer, GlobalColorTokens.onSecondaryContainer
        ),
        AppRole.Tertiary to RoleTokens(
            GlobalColorTokens.tertiary, GlobalColorTokens.onTertiary,
            GlobalColorTokens.tertiaryContainer, GlobalColorTokens.onTertiaryContainer
        ),
        AppRole.Success to RoleTokens(
            SuccessColorTokens.success, SuccessColorTokens.onSuccess,
            SuccessColorTokens.successContainer, SuccessColorTokens.onSuccessContainer
        ),
        AppRole.Warning to RoleTokens(
            GlobalColorTokens.warning, GlobalColorTokens.onWarning,
            GlobalColorTokens.warningContainer, GlobalColorTokens.onWarningContainer
        ),
        AppRole.Error to RoleTokens(
            GlobalColorTokens.error, GlobalColorTokens.onError,
            GlobalColorTokens.errorContainer, GlobalColorTokens.onErrorContainer
        )
    )

    @Test
    fun `each token based role resolves its four tokens in light and dark`() {
        listOf(false, true).forEach { dark ->
            val tokens = resolved(dark)
            val appColors = colors(dark)
            roleTokens.forEach { (role, ids) ->
                val actual = appColors.forRole(role)
                assertEquals("$role accent dark=$dark", tokens.getValue(ids.accent), actual.accent)
                assertEquals("$role onAccent dark=$dark", tokens.getValue(ids.onAccent), actual.onAccent)
                assertEquals("$role container dark=$dark", tokens.getValue(ids.container), actual.container)
                assertEquals("$role onContainer dark=$dark", tokens.getValue(ids.onContainer), actual.onContainer)
                assertEquals("$role tonalBorder dark=$dark", actual.accent, actual.tonalBorder)
            }
        }
    }

    @Test
    fun `forRole maps every role to its property`() {
        val appColors = colors(dark = false)
        assertEquals(appColors.primary, appColors.forRole(AppRole.Primary))
        assertEquals(appColors.secondary, appColors.forRole(AppRole.Secondary))
        assertEquals(appColors.tertiary, appColors.forRole(AppRole.Tertiary))
        assertEquals(appColors.success, appColors.forRole(AppRole.Success))
        assertEquals(appColors.warning, appColors.forRole(AppRole.Warning))
        assertEquals(appColors.error, appColors.forRole(AppRole.Error))
        assertEquals(appColors.neutral, appColors.forRole(AppRole.Neutral))
        assertEquals(appColors.secondary, appColors.success)
        assertEquals(AppRole.entries.size - 1, AppRole.entries.map { appColors.forRole(it) }.toSet().size)
    }

    @Test
    fun `light defaults use the sapphire and emerald ramps`() {
        val appColors = colors(dark = false)
        assertEquals(Sapphire40, appColors.primary.accent)
        assertEquals(Color.White, appColors.primary.onAccent)
        assertEquals(Sapphire90, appColors.primary.container)
        assertEquals(Sapphire10, appColors.primary.onContainer)
        assertEquals(Emerald40, appColors.success.accent)
    }

    @Test
    fun `neutral role follows the on-background outline and background tokens`() {
        listOf(false, true).forEach { dark ->
            val tokens = resolved(dark)
            val neutral = colors(dark).neutral
            assertEquals(tokens.getValue(GlobalColorTokens.onBackground), neutral.accent)
            assertEquals(tokens.getValue(GlobalColorTokens.onBackground), neutral.onContainer)
            assertEquals(tokens.getValue(GlobalColorTokens.background), neutral.onAccent)
            assertEquals(tokens.getValue(GlobalColorTokens.background), neutral.container)
            assertEquals(tokens.getValue(GlobalColorTokens.outline), neutral.tonalBorder)
        }
    }

    @Test
    fun `surface fields follow the background tokens`() {
        listOf(false, true).forEach { dark ->
            val tokens = resolved(dark)
            val appColors = colors(dark)
            val background = tokens.getValue(GlobalColorTokens.background)
            val onBackground = tokens.getValue(GlobalColorTokens.onBackground)
            assertEquals(background, appColors.background)
            assertEquals(background, appColors.surface)
            assertEquals(background, appColors.surfaceVariant)
            assertEquals(background, appColors.surface3)
            assertEquals(onBackground, appColors.onBackground)
            assertEquals(onBackground, appColors.onSurface)
            assertEquals(onBackground, appColors.onSurfaceVariant)
            assertEquals(tokens.getValue(GlobalColorTokens.outline), appColors.outline)
        }
    }

    @Test
    fun `light and dark palettes differ`() {
        assertNotEquals(colors(false).primary.accent, colors(true).primary.accent)
        assertNotEquals(colors(false).background, colors(true).background)
        assertNotEquals(colors(false).success.accent, colors(true).success.accent)
    }

    @Test
    fun `token overrides propagate to the role colors`() {
        val custom = Color(0xFF123456)
        val overrides = mapOf(
            GlobalColorTokens.primary to ColorValue.Custom(0xFF123456.toInt()),
            SuccessColorTokens.success to ColorValue.Custom(0xFF654321.toInt())
        )
        val appColors = colors(dark = false, overrides = overrides)
        assertEquals(custom, appColors.primary.accent)
        assertEquals(Color(0xFF654321), appColors.success.accent)
        assertEquals(Color(0xFF123456), appColors.primary.tonalBorder)
    }

    @Test
    fun `color ramps expose seven named ramps with nine tones`() {
        val ramps = AppTheme.colorRamps
        assertEquals(ColorRamp.entries.size, ramps.size)
        assertEquals(
            listOf("Sapphire", "Aquamarine", "Amethyst", "Emerald", "Citrine", "Garnet", "Graphite"),
            ramps.map { it.first }
        )
        ramps.forEach { (_, tones) -> assertEquals(9, tones.size) }
        assertEquals(Sapphire40, ramps.first().second[3])
    }

    @Test
    fun `color ramp tones get lighter from 10 to 90`() {
        AppTheme.colorRamps.forEach { (name, tones) ->
            val luminances = tones.map { 0.299f * it.red + 0.587f * it.green + 0.114f * it.blue }
            assertTrue(name, luminances.zipWithNext().all { (a, b) -> a < b })
        }
    }

    @Test
    fun `app role mapping points at the global tokens`() {
        assertEquals(GlobalColorTokens.primary, AppMaterialRoleMapping.primary)
        assertEquals(GlobalColorTokens.errorContainer, AppMaterialRoleMapping.errorContainer)
        assertEquals(GlobalColorTokens.background, AppMaterialRoleMapping.background)
        assertEquals(GlobalColorTokens.outline, AppMaterialRoleMapping.outline)
    }

    @Test
    fun `warning colors for buttons come from the warning tokens`() {
        val tokens = resolved(dark = true)
        val warning = buttonWarningColorsFrom(tokens)
        assertEquals(tokens.getValue(GlobalColorTokens.warning), warning.warning)
        assertEquals(tokens.getValue(GlobalColorTokens.onWarning), warning.onWarning)
        assertEquals(tokens.getValue(GlobalColorTokens.warningContainer), warning.warningContainer)
        assertEquals(tokens.getValue(GlobalColorTokens.onWarningContainer), warning.onWarningContainer)
    }
}
