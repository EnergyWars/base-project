package com.wafflehq.lib.settings.colors

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class MaterialRoleMappingTest {

    private val primaryId = ColorTokenId("m.primary")
    private val onPrimaryId = ColorTokenId("m.onPrimary")
    private val backgroundId = ColorTokenId("m.background")
    private val onBackgroundId = ColorTokenId("m.onBackground")
    private val outlineId = ColorTokenId("m.outline")
    private val unmappedId = ColorTokenId("m.unmapped")

    private val mapping = MaterialRoleMapping(
        primary = primaryId,
        onPrimary = onPrimaryId,
        primaryContainer = unmappedId,
        onPrimaryContainer = unmappedId,
        secondary = unmappedId,
        onSecondary = unmappedId,
        secondaryContainer = unmappedId,
        onSecondaryContainer = unmappedId,
        tertiary = unmappedId,
        onTertiary = unmappedId,
        tertiaryContainer = unmappedId,
        onTertiaryContainer = unmappedId,
        error = unmappedId,
        onError = unmappedId,
        errorContainer = unmappedId,
        onErrorContainer = unmappedId,
        background = backgroundId,
        onBackground = onBackgroundId,
        outline = outlineId
    )

    private val primary = Color(0xFF112233)
    private val onPrimary = Color(0xFF445566)
    private val background = Color(0xFF778899)
    private val onBackground = Color(0xFFAABBCC)
    private val outline = Color(0xFFDDEEFF)

    private val resolved = mapOf(
        primaryId to primary,
        onPrimaryId to onPrimary,
        backgroundId to background,
        onBackgroundId to onBackground,
        outlineId to outline
    )

    @Test
    fun `mapped roles take their token color`() {
        val scheme = lightColorScheme().withRoleMapping(resolved, mapping)

        assertEquals(primary, scheme.primary)
        assertEquals(onPrimary, scheme.onPrimary)
        assertEquals(background, scheme.background)
        assertEquals(onBackground, scheme.onBackground)
        assertEquals(outline, scheme.outline)
    }

    @Test
    fun `surface roles alias background so surfaces stay flat`() {
        val scheme = lightColorScheme().withRoleMapping(resolved, mapping)

        assertEquals(background, scheme.surface)
        assertEquals(background, scheme.surfaceVariant)
        assertEquals(background, scheme.surfaceContainer)
        assertEquals(background, scheme.surfaceContainerLow)
        assertEquals(background, scheme.surfaceContainerHigh)
        assertEquals(background, scheme.inverseOnSurface)
        assertEquals(onBackground, scheme.onSurface)
        assertEquals(onBackground, scheme.onSurfaceVariant)
        assertEquals(onBackground, scheme.inverseSurface)
    }

    @Test
    fun `tint and outline variants follow primary and outline`() {
        val scheme = lightColorScheme().withRoleMapping(resolved, mapping)

        assertEquals(primary, scheme.surfaceTint)
        assertEquals(primary, scheme.inversePrimary)
        assertEquals(outline, scheme.outlineVariant)
    }

    @Test
    fun `a role whose token is missing keeps the base scheme color`() {
        val base = lightColorScheme()

        val scheme = base.withRoleMapping(resolved, mapping)

        assertEquals(base.secondary, scheme.secondary)
        assertEquals(base.error, scheme.error)
        assertEquals(base.tertiaryContainer, scheme.tertiaryContainer)
    }

    @Test
    fun `an empty resolution leaves every mapped role untouched`() {
        val base = lightColorScheme()

        val scheme = base.withRoleMapping(emptyMap(), mapping)

        assertEquals(base.primary, scheme.primary)
        assertEquals(base.background, scheme.background)
        assertEquals(base.outline, scheme.outline)
    }

    @Test
    fun `two tokens may point at the same role source`() {
        val shared = ColorTokenId("m.shared")
        val sharedColor = Color(0xFF010203)
        val sharedMapping = mapping.copy(primary = shared, background = shared)

        val scheme = lightColorScheme().withRoleMapping(mapOf(shared to sharedColor), sharedMapping)

        assertEquals(sharedColor, scheme.primary)
        assertEquals(sharedColor, scheme.background)
        assertEquals(sharedColor, scheme.surface)
    }
}
