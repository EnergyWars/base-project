package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorTokenRegistryTest {

    private fun token(id: String, categoryKey: String) = ColorToken(
        id = ColorTokenId(id),
        categoryKey = categoryKey,
        labelRes = 0,
        descriptionRes = 0,
        defaultLight = Color(0xFF112233),
        defaultDark = Color(0xFF445566)
    )

    private fun category(key: String, alwaysVisible: Boolean = false) =
        ColorCategoryDescriptor(key = key, labelRes = 0, descriptionRes = 0, alwaysVisible = alwaysVisible)

    private val registry = ColorTokenRegistry(
        categories = listOf(category("GLOBAL", alwaysVisible = true), category("MODULE")),
        all = listOf(
            token("global.primary", "GLOBAL"),
            token("global.onPrimary", "GLOBAL"),
            token("module.accent", "MODULE"),
            token("module.onAccent", "MODULE"),
            token("module.tint", "MODULE")
        )
    )

    @Test
    fun `byId indexes every token`() {
        assertEquals(5, registry.byId.size)
        assertEquals(ColorTokenId("module.tint"), registry.byId.getValue(ColorTokenId("module.tint")).id)
    }

    @Test
    fun `byCategory groups tokens under their category key`() {
        assertEquals(listOf("global.primary", "global.onPrimary"), registry.tokensOf("GLOBAL").map { it.id.value })
        assertEquals(3, registry.tokensOf("MODULE").size)
        assertTrue(registry.tokensOf("UNKNOWN").isEmpty())
    }

    @Test
    fun `shortIds combine the category letter with the position inside that category`() {
        assertEquals("a.1", registry.shortId(ColorTokenId("global.primary")))
        assertEquals("a.2", registry.shortId(ColorTokenId("global.onPrimary")))
        assertEquals("b.1", registry.shortId(ColorTokenId("module.accent")))
        assertEquals("b.3", registry.shortId(ColorTokenId("module.tint")))
    }

    @Test
    fun `category letters stay plain letters past the twenty sixth category`() {
        val keys = (0 until 28).map { "C$it" }
        val wide = ColorTokenRegistry(
            categories = keys.map { category(it) },
            all = keys.map { token("${it.lowercase()}.only", it) }
        )

        assertEquals("z.1", wide.shortId(ColorTokenId("c25.only")))
        assertEquals("aa.1", wide.shortId(ColorTokenId("c26.only")))
        assertEquals("ab.1", wide.shortId(ColorTokenId("c27.only")))
    }

    @Test
    fun `alwaysVisibleCategoryKeys lists only the categories marked as such`() {
        assertEquals(setOf("GLOBAL"), registry.alwaysVisibleCategoryKeys)
    }

    @Test
    fun `contrast counterpart resolves an on-prefixed token to its base token`() {
        assertEquals(
            ColorTokenId("module.accent"),
            registry.contrastCounterpartOrNull(ColorTokenId("module.onAccent"))
        )
    }

    @Test
    fun `contrast counterpart resolves a base token to its on-prefixed token`() {
        assertEquals(
            ColorTokenId("module.onAccent"),
            registry.contrastCounterpartOrNull(ColorTokenId("module.accent"))
        )
    }

    @Test
    fun `contrast counterpart is null when the counterpart is not registered`() {
        assertNull(registry.contrastCounterpartOrNull(ColorTokenId("module.tint")))
    }

    @Test
    fun `contrast counterpart is null without a namespace separator`() {
        assertNull(registry.contrastCounterpartOrNull(ColorTokenId("accent")))
    }

    @Test
    fun `contrast counterpart is null for an empty suffix`() {
        assertNull(registry.contrastCounterpartOrNull(ColorTokenId("module.")))
    }

    @Test
    fun `contrast counterpart stays within the token's own namespace`() {
        assertNull(registry.contrastCounterpartOrNull(ColorTokenId("global.accent")))
    }
}
