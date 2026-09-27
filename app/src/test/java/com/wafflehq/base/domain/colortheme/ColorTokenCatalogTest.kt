package com.wafflehq.base.domain.colortheme

import com.wafflehq.base.domain.colortheme.tokens.GlobalColorTokens
import com.wafflehq.base.domain.colortheme.tokens.SuccessColorTokens
import com.wafflehq.lib.settings.colors.ColorTokenId
import com.wafflehq.lib.settings.colors.Emerald30
import com.wafflehq.lib.settings.colors.Emerald40
import com.wafflehq.lib.settings.colors.Emerald80
import com.wafflehq.lib.settings.colors.Emerald90
import com.wafflehq.lib.settings.colors.ColorTokenResolver
import com.wafflehq.lib.uicore.color.contrastRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorTokenCatalogTest {

    private val registry = ColorTokenCatalog.registry

    @Test
    fun `all token ids are unique`() {
        assertEquals(registry.all.size, registry.all.map { it.id }.toSet().size)
    }

    @Test
    fun `catalog contains the global and success tokens and nothing else`() {
        val expected = GlobalColorTokens.tokens + SuccessColorTokens.tokens
        assertEquals(expected, registry.all)
        assertEquals(expected, ColorTokenCatalog.all)
    }

    @Test
    fun `global category has the 19 material roles plus 4 warning roles`() {
        assertEquals(23, GlobalColorTokens.tokens.size)
        assertEquals(23, registry.tokensOf(ColorTokenCategory.GLOBAL.name).size)
    }

    @Test
    fun `success category contains exactly the four success roles`() {
        val ids = registry.tokensOf(ColorTokenCategory.SUCCESS.name).map { it.id }
        assertEquals(
            listOf(
                SuccessColorTokens.success,
                SuccessColorTokens.onSuccess,
                SuccessColorTokens.successContainer,
                SuccessColorTokens.onSuccessContainer
            ),
            ids
        )
    }

    @Test
    fun `every token belongs to a registered category and uses its prefix`() {
        registry.all.forEach { token ->
            assertNotNull(registry.categoryByKey[token.categoryKey])
            assertTrue(token.id.value.startsWith(token.categoryKey.lowercase() + "."))
        }
    }

    @Test
    fun `every category is registered as always visible and has resources`() {
        ColorTokenCategory.entries.forEach { category ->
            val descriptor = registry.categoryByKey.getValue(category.name)
            assertTrue(descriptor.alwaysVisible)
            assertEquals(category.labelRes, descriptor.labelRes)
            assertEquals(category.descriptionRes, descriptor.descriptionRes)
            assertTrue(category.labelRes != 0)
            assertTrue(category.descriptionRes != 0)
        }
        assertEquals(ColorTokenCategory.entries.size, registry.alwaysVisibleCategoryKeys.size)
    }

    @Test
    fun `label and description resources are distinct per token`() {
        assertEquals(registry.all.size, registry.all.map { it.labelRes }.toSet().size)
        assertEquals(registry.all.size, registry.all.map { it.descriptionRes }.toSet().size)
    }

    @Test
    fun `short ids follow category order`() {
        assertEquals("a.1", registry.shortId(GlobalColorTokens.primary))
        assertEquals("b.1", registry.shortId(SuccessColorTokens.success))
        assertEquals("b.4", registry.shortId(SuccessColorTokens.onSuccessContainer))
    }

    @Test
    fun `every on-token has a counterpart and reaches AA contrast in light and dark`() {
        val onTokens = registry.all.filter { token ->
            val suffix = token.id.value.substringAfter('.')
            suffix.startsWith("on") && suffix[2].isUpperCase()
        }
        assertEquals(13, onTokens.size)
        onTokens.forEach { on ->
            val counterpartId = registry.contrastCounterpartOrNull(on.id)
            assertNotNull(on.id.value, counterpartId)
            val counterpart = registry.byId.getValue(counterpartId!!)
            listOf(false, true).forEach { dark ->
                val ratio = contrastRatio(
                    ColorTokenResolver.resolve(on, null, dark),
                    ColorTokenResolver.resolve(counterpart, null, dark)
                )
                assertTrue("${on.id.value} dark=$dark ratio=$ratio", ratio >= 4.5)
            }
        }
    }

    @Test
    fun `outline has no contrast counterpart`() {
        assertNull(registry.contrastCounterpartOrNull(GlobalColorTokens.outline))
    }

    @Test
    fun `unknown token id is not registered`() {
        assertNull(registry.byId[ColorTokenId("global.unknown")])
    }

    @Test
    fun `success defaults come from the emerald ramp`() {
        val success = registry.byId.getValue(SuccessColorTokens.success)
        val container = registry.byId.getValue(SuccessColorTokens.successContainer)
        assertEquals(Emerald40, success.defaultLight)
        assertEquals(Emerald80, success.defaultDark)
        assertEquals(Emerald90, container.defaultLight)
        assertEquals(Emerald30, container.defaultDark)
    }
}
