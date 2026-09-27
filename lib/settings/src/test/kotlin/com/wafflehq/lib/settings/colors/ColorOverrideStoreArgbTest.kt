package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class ColorOverrideStoreArgbTest {

    private lateinit var store: ColorOverrideStore
    private val registry = TestColorTokens.registry

    @Before
    fun setUp() = kotlinx.coroutines.runBlocking {
        store = ColorOverrideStore(FakePreferencesDataStore())
        store.createTheme("Test")
        store.confirmPending()
    }

    @Test
    fun `token without override resolves to its default for the requested mode`() = runTest {
        val token = registry.byId.getValue(TestColorTokens.alphaOnAccent)
        assertEquals(token.defaultLight.toArgb(), store.resolveArgb(registry, token.id, isDark = false))
        assertEquals(token.defaultDark.toArgb(), store.resolveArgb(registry, token.id, isDark = true))
    }

    @Test
    fun `custom override wins over the default`() = runTest {
        val custom = Color(0xFF123456).toArgb()
        store.setOverride(TestColorTokens.alphaOnAccent, isDark = true, ColorValue.Custom(custom))
        assertEquals(custom, store.resolveArgb(registry, TestColorTokens.alphaOnAccent, isDark = true))
        assertEquals(
            registry.byId.getValue(TestColorTokens.alphaOnAccent).defaultLight.toArgb(),
            store.resolveArgb(registry, TestColorTokens.alphaOnAccent, isDark = false)
        )
    }

    @Test
    fun `resolveArgbs returns one entry per requested token`() = runTest {
        val custom = Color(0xFF654321).toArgb()
        store.setOverride(TestColorTokens.betaAccent, isDark = false, ColorValue.Custom(custom))
        val result = store.resolveArgbs(
            registry,
            listOf(TestColorTokens.alphaAccent, TestColorTokens.betaAccent),
            isDark = false
        )
        assertEquals(setOf(TestColorTokens.alphaAccent, TestColorTokens.betaAccent), result.keys)
        assertEquals(custom, result.getValue(TestColorTokens.betaAccent))
    }

    @Test
    fun `unknown token id is rejected`() {
        assertThrows(NoSuchElementException::class.java) {
            kotlinx.coroutines.runBlocking { store.resolveArgb(registry, ColorTokenId("missing"), isDark = false) }
        }
    }
}
