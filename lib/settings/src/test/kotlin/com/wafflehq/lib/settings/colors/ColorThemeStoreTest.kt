package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ColorThemeStoreTest {

    private var now = 1_000_000L
    private var nextId = 0
    private lateinit var store: ColorOverrideStore

    private val tokenA = ColorTokenId("test.tokenA")

    @Before
    fun setUp() {
        now = 1_000_000L
        nextId = 0
        store = ColorOverrideStore(
            FakePreferencesDataStore(),
            clock = { now },
            confirmWindowMillis = WINDOW,
            idGenerator = { "id${nextId++}" }
        )
    }

    private suspend fun activeName(): String? {
        val id = store.activeThemeIdFlow().first() ?: return null
        return store.themesFlow().first().first { it.id == id }.name
    }

    @Test
    fun `a fresh store has the standard theme active and no themes`() = runTest {
        assertNull(store.activeThemeIdFlow().first())
        assertTrue(store.themesFlow().first().isEmpty())
    }

    @Test
    fun `createTheme adds the theme and activates it`() = runTest {
        val info = store.createTheme("Sunset")

        assertEquals("Sunset", info.name)
        assertEquals(info.id, store.activeThemeIdFlow().first())
        assertEquals(listOf(info), store.themesFlow().first())
    }

    @Test
    fun `createTheme makes names unique`() = runTest {
        store.createTheme("Sunset")
        val second = store.createTheme("Sunset")
        val third = store.createTheme(" Sunset ")

        assertEquals("Sunset (2)", second.name)
        assertEquals("Sunset (3)", third.name)
    }

    @Test
    fun `a new theme starts empty even when another theme has customizations`() = runTest {
        store.createTheme("A")
        store.setOverride(tokenA, false, ColorValue.Custom(1))

        store.createTheme("B")

        assertTrue(store.overridesFlow(false).first().isEmpty())
    }

    @Test
    fun `createTheme can copy the active theme`() = runTest {
        store.createTheme("A")
        store.setOverride(tokenA, false, ColorValue.Custom(1))
        store.setBaseRampOverride(ColorRamp.GARNET, Color(0xFF112233))

        store.createTheme("B", copyFromActive = true)

        assertEquals(ColorValue.Custom(1), store.overridesFlow(false).first()[tokenA])
        assertEquals(Color(0xFF112233), store.baseRampOverridesFlow().first()[ColorRamp.GARNET])
    }

    @Test
    fun `themes keep their colors when the user switches between them`() = runTest {
        val a = store.createTheme("A")
        store.setOverride(tokenA, false, ColorValue.Custom(1))
        val b = store.createTheme("B")
        store.setOverride(tokenA, false, ColorValue.Custom(2))

        store.activateTheme(a.id)
        assertEquals(ColorValue.Custom(1), store.overridesFlow(false).first()[tokenA])
        store.activateTheme(b.id)
        assertEquals(ColorValue.Custom(2), store.overridesFlow(false).first()[tokenA])
    }

    @Test
    fun `activating the standard theme shows default colors without deleting any theme`() = runTest {
        val a = store.createTheme("A")
        store.setOverride(tokenA, false, ColorValue.Custom(1))

        store.activateTheme(null)

        assertTrue(store.overridesFlow(false).first().isEmpty())
        assertEquals(listOf(a), store.themesFlow().first())
        assertEquals(ColorValue.Custom(1), store.themeData(a.id)!!.overrides(false)[tokenA])
    }

    @Test
    fun `activateTheme ignores an unknown id`() = runTest {
        val a = store.createTheme("A")

        store.activateTheme("missing")

        assertEquals(a.id, store.activeThemeIdFlow().first())
    }

    @Test
    fun `renameTheme changes the name and keeps it unique`() = runTest {
        store.createTheme("A")
        val b = store.createTheme("B")

        store.renameTheme(b.id, "A")

        assertEquals("A (2)", store.themesFlow().first().first { it.id == b.id }.name)
    }

    @Test
    fun `renaming a theme to its own name keeps the name`() = runTest {
        val a = store.createTheme("A")

        store.renameTheme(a.id, "A")

        assertEquals("A", activeName())
    }

    @Test
    fun `deleting the active theme falls back to the standard theme`() = runTest {
        val a = store.createTheme("A")
        val b = store.createTheme("B")

        store.deleteTheme(b.id)

        assertNull(store.activeThemeIdFlow().first())
        assertEquals(listOf(a), store.themesFlow().first())
    }

    @Test
    fun `deleting an inactive theme keeps the active one`() = runTest {
        val a = store.createTheme("A")
        val b = store.createTheme("B")

        store.deleteTheme(a.id)

        assertEquals(b.id, store.activeThemeIdFlow().first())
    }

    @Test
    fun `importThemes adds themes with unique names and leaves the active theme untouched`() = runTest {
        val a = store.createTheme("A")
        val data = ColorThemeData(overridesLight = mapOf(tokenA.value to ColorValueCodec.encode(ColorValue.Custom(5))))

        val added = store.importThemes(listOf(ColorThemeExportEntry("A", data), ColorThemeExportEntry("Neu", data)))

        assertEquals(listOf("A (2)", "Neu"), added.map { it.name })
        assertEquals(a.id, store.activeThemeIdFlow().first())
        assertEquals(3, store.themesFlow().first().size)
        assertEquals(data, store.themeData(added.last().id))
    }

    @Test
    fun `an edit opens the confirmation window with the configured deadline`() = runTest {
        store.createTheme("A")
        store.confirmPending()

        store.setOverride(tokenA, false, ColorValue.Custom(1))

        assertEquals(now + WINDOW, store.pendingConfirmationFlow().first()!!.deadlineMillis)
    }

    @Test
    fun `a no-op edit does not open a confirmation window`() = runTest {
        store.createTheme("A")
        store.confirmPending()

        store.setOverride(tokenA, false, null)

        assertNull(store.pendingConfirmationFlow().first())
    }

    @Test
    fun `further edits extend the deadline but keep the first snapshot`() = runTest {
        store.createTheme("A")
        store.confirmPending()
        store.setOverride(tokenA, false, ColorValue.Custom(1))
        val first = store.pendingConfirmationFlow().first()!!
        now += 5_000

        store.setOverride(tokenA, false, ColorValue.Custom(2))

        val second = store.pendingConfirmationFlow().first()!!
        assertEquals(first.snapshot, second.snapshot)
        assertEquals(now + WINDOW, second.deadlineMillis)
    }

    @Test
    fun `confirmPending keeps the edits and closes the window`() = runTest {
        store.createTheme("A")
        store.setOverride(tokenA, false, ColorValue.Custom(1))

        store.confirmPending()

        assertNull(store.pendingConfirmationFlow().first())
        assertEquals(ColorValue.Custom(1), store.overridesFlow(false).first()[tokenA])
    }

    @Test
    fun `revertPending restores the state before the first unconfirmed edit`() = runTest {
        val a = store.createTheme("A")
        store.setOverride(tokenA, false, ColorValue.Custom(1))
        store.confirmPending()
        store.setOverride(tokenA, false, ColorValue.Custom(2))
        store.setBaseRampOverride(ColorRamp.SAPPHIRE, Color(0xFF112233))

        store.revertPending()

        assertEquals(ColorValue.Custom(1), store.overridesFlow(false).first()[tokenA])
        assertTrue(store.baseRampOverridesFlow().first().isEmpty())
        assertEquals(a.id, store.activeThemeIdFlow().first())
        assertNull(store.pendingConfirmationFlow().first())
    }

    @Test
    fun `revertPending also undoes an unconfirmed theme creation`() = runTest {
        store.createTheme("Old")
        store.confirmPending()
        val old = store.activeThemeIdFlow().first()
        store.activateTheme(null)
        store.createTheme("New")

        store.revertPending()

        assertNull(store.activeThemeIdFlow().first())
        assertEquals(listOf(old), store.themesFlow().first().map { it.id })
    }

    @Test
    fun `revertPending without an open window does nothing`() = runTest {
        store.createTheme("A")
        store.confirmPending()
        store.setOverride(tokenA, false, ColorValue.Custom(1))
        store.confirmPending()

        store.revertPending()

        assertEquals(ColorValue.Custom(1), store.overridesFlow(false).first()[tokenA])
    }

    @Test
    fun `revertIfExpired keeps the edits until the deadline has passed`() = runTest {
        store.createTheme("A")
        store.confirmPending()
        store.setOverride(tokenA, false, ColorValue.Custom(1))
        now += WINDOW - 1

        assertFalse(store.revertIfExpired())
        assertEquals(ColorValue.Custom(1), store.overridesFlow(false).first()[tokenA])
    }

    @Test
    fun `revertIfExpired rolls back once the deadline has passed`() = runTest {
        store.createTheme("A")
        store.confirmPending()
        store.setOverride(tokenA, false, ColorValue.Custom(1))
        now += WINDOW

        assertTrue(store.revertIfExpired())
        assertNull(store.overridesFlow(false).first()[tokenA])
        assertNull(store.pendingConfirmationFlow().first())
    }

    @Test
    fun `revertIfExpired without a window returns false`() = runTest {
        assertFalse(store.revertIfExpired())
    }

    @Test
    fun `switching to a theme opens the window and reverting returns to the previous theme`() = runTest {
        val a = store.createTheme("A")
        val b = store.createTheme("B")
        store.confirmPending()
        store.activateTheme(a.id)
        assertNotEquals(null, store.pendingConfirmationFlow().first())

        store.revertPending()

        assertEquals(b.id, store.activeThemeIdFlow().first())
    }

    @Test
    fun `switching to the standard theme needs no confirmation`() = runTest {
        store.createTheme("A")
        store.confirmPending()

        store.activateTheme(null)

        assertNull(store.pendingConfirmationFlow().first())
    }

    @Test
    fun `resetToStandardTheme activates the standard theme and cancels an open window`() = runTest {
        val a = store.createTheme("A")
        store.setOverride(tokenA, false, ColorValue.Custom(1))

        store.resetToStandardTheme()

        assertNull(store.activeThemeIdFlow().first())
        assertNull(store.pendingConfirmationFlow().first())
        assertEquals(ColorValue.Custom(1), store.themeData(a.id)!!.overrides(false)[tokenA])
    }

    @Test
    fun `renaming and deleting do not open a confirmation window`() = runTest {
        val a = store.createTheme("A")
        store.confirmPending()

        store.renameTheme(a.id, "B")
        store.deleteTheme(a.id)

        assertNull(store.pendingConfirmationFlow().first())
    }

    private companion object {
        const val WINDOW = 15_000L
    }
}
