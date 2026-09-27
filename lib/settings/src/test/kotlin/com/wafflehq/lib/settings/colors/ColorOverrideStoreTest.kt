package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class ColorOverrideStoreTest {

    private lateinit var dataStore: FakePreferencesDataStore
    private lateinit var store: ColorOverrideStore

    private val tokenA = ColorTokenId("test.tokenA")
    private val tokenB = ColorTokenId("test.tokenB")

    @Before
    fun setUp() = kotlinx.coroutines.runBlocking {
        dataStore = FakePreferencesDataStore()
        store = ColorOverrideStore(dataStore)
        store.createTheme("Test")
        store.confirmPending()
    }

    @Test
    fun `overrideFlow is null for a token that was never set`() = runTest {
        assertNull(store.overrideFlow(tokenA, isDark = false).first())
        assertNull(store.overrideFlow(tokenA, isDark = true).first())
    }

    @Test
    fun `setOverride then read returns the same palette value`() = runTest {
        val value = ColorValue.Palette(ColorRamp.GARNET, 40)
        store.setOverride(tokenA, isDark = false, value)
        assertEquals(value, store.overrideFlow(tokenA, isDark = false).first())
    }

    @Test
    fun `setOverride then read returns the same custom value`() = runTest {
        val value = ColorValue.Custom(0x80112233.toInt())
        store.setOverride(tokenA, isDark = true, value)
        assertEquals(value, store.overrideFlow(tokenA, isDark = true).first())
    }

    @Test
    fun `light and dark overrides are stored independently`() = runTest {
        store.setOverride(tokenA, isDark = false, ColorValue.Custom(1))
        assertNull(store.overrideFlow(tokenA, isDark = true).first())
    }

    @Test
    fun `setOverride with null clears a previously set value`() = runTest {
        store.setOverride(tokenA, isDark = false, ColorValue.Custom(0xFF111111.toInt()))
        store.setOverride(tokenA, isDark = false, null)
        assertNull(store.overrideFlow(tokenA, isDark = false).first())
    }

    @Test
    fun `clearTokens removes only the given tokens of one mode`() = runTest {
        store.setOverride(tokenA, isDark = false, ColorValue.Custom(1))
        store.setOverride(tokenB, isDark = false, ColorValue.Custom(2))
        store.setOverride(tokenA, isDark = true, ColorValue.Custom(3))

        store.clearTokens(listOf(tokenA), isDark = false)

        assertEquals(mapOf(tokenB to ColorValue.Custom(2)), store.overridesFlow(isDark = false).first())
        assertEquals(ColorValue.Custom(3), store.overrideFlow(tokenA, isDark = true).first())
    }

    @Test
    fun `clearAll removes overrides of both modes`() = runTest {
        store.setOverride(tokenA, isDark = false, ColorValue.Custom(0xFF111111.toInt()))
        store.setOverride(tokenB, isDark = true, ColorValue.Custom(0xFF222222.toInt()))

        store.clearAll()

        assertTrue(store.overridesFlow(isDark = false).first().isEmpty())
        assertTrue(store.overridesFlow(isDark = true).first().isEmpty())
    }

    @Test
    fun `a corrupt raw value for one token is silently omitted without affecting others`() = runTest {
        val theme = store.storedThemes().single()
        val corrupt = theme.copy(
            data = ColorThemeData(
                overridesLight = mapOf(
                    tokenA.value to "totally-garbage-value",
                    tokenB.value to ColorValueCodec.encode(ColorValue.Custom(0xFF333333.toInt()))
                )
            )
        )
        dataStore.edit { it[LIBRARY_KEY] = colorJson.encodeToString(ColorThemeLibrary(theme.id, listOf(corrupt))) }

        val all = store.overridesFlow(isDark = false).first()

        assertNull(all[tokenA])
        assertEquals(ColorValue.Custom(0xFF333333.toInt()), all[tokenB])
        assertEquals(1, all.size)
    }

    @Test
    fun `a corrupt library falls back to the standard theme`() = runTest {
        dataStore.edit { it[stringPreferencesKey("theme_library")] = "{ not json" }

        assertNull(store.activeThemeIdFlow().first())
        assertTrue(store.themesFlow().first().isEmpty())
    }

    @Test
    fun `edits while the standard theme is active change nothing`() = runTest {
        store.activateTheme(null)

        store.setOverride(tokenA, isDark = false, ColorValue.Custom(1))
        store.setBaseRampOverride(ColorRamp.SAPPHIRE, Color(0xFF112233))

        assertTrue(store.overridesFlow(isDark = false).first().isEmpty())
        assertTrue(store.baseRampOverridesFlow().first().isEmpty())
        assertNull(store.pendingConfirmationFlow().first())
    }

    @Test
    fun `baseRampOverridesFlow is empty when nothing is stored`() = runTest {
        assertTrue(store.baseRampOverridesFlow().first().isEmpty())
    }

    @Test
    fun `setBaseRampOverride then read returns the same color`() = runTest {
        val color = Color(0xFF112233)
        store.setBaseRampOverride(ColorRamp.SAPPHIRE, color)
        assertEquals(color, store.baseRampOverridesFlow().first()[ColorRamp.SAPPHIRE])
    }

    @Test
    fun `setBaseRampOverride with null clears a previously set ramp`() = runTest {
        store.setBaseRampOverride(ColorRamp.SAPPHIRE, Color(0xFF112233))
        store.setBaseRampOverride(ColorRamp.SAPPHIRE, null)
        assertTrue(store.baseRampOverridesFlow().first().isEmpty())
    }

    @Test
    fun `base ramp overrides for different ramps are stored independently`() = runTest {
        val sapphire = Color(0xFF112233)
        val garnet = Color(0xFF445566)
        store.setBaseRampOverride(ColorRamp.SAPPHIRE, sapphire)
        store.setBaseRampOverride(ColorRamp.GARNET, garnet)

        val all = store.baseRampOverridesFlow().first()
        assertEquals(sapphire, all[ColorRamp.SAPPHIRE])
        assertEquals(garnet, all[ColorRamp.GARNET])
        assertEquals(2, all.size)
    }

    @Test
    fun `clearAll also removes stored base ramp overrides`() = runTest {
        store.setBaseRampOverride(ColorRamp.SAPPHIRE, Color(0xFF112233))
        store.clearAll()
        assertTrue(store.baseRampOverridesFlow().first().isEmpty())
    }

    @Test
    fun `simplifiedColorViewActiveFlow defaults to true when nothing is stored`() = runTest {
        assertTrue(store.simplifiedColorViewActiveFlow().first())
    }

    @Test
    fun `setSimplifiedColorViewActive then read returns the same value`() = runTest {
        store.setSimplifiedColorViewActive(false)
        assertEquals(false, store.simplifiedColorViewActiveFlow().first())

        store.setSimplifiedColorViewActive(true)
        assertEquals(true, store.simplifiedColorViewActiveFlow().first())
    }
}
