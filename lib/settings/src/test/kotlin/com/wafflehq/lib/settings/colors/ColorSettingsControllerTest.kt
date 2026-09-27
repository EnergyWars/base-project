package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class ColorSettingsControllerTest {

    private lateinit var store: ColorOverrideStore
    private lateinit var exportRepository: ColorThemeExportRepository
    private val scopes = mutableListOf<CoroutineScope>()

    private val registry = TestColorTokens.registry
    private val tokenA = TestColorTokens.alphaAccent
    private val tokenB = TestColorTokens.alphaOnAccent
    private val tokenC = TestColorTokens.betaAccent

    @Before
    fun setUp() = kotlinx.coroutines.runBlocking {
        store = ColorOverrideStore(FakePreferencesDataStore())
        exportRepository = ColorThemeExportRepository(store, registry)
        store.createTheme("Test")
        store.confirmPending()
    }

    @After
    fun tearDown() {
        scopes.forEach { it.cancel() }
        scopes.clear()
    }

    private fun controller(): ColorSettingsController {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scopes += scope
        return ColorSettingsController(store, exportRepository, registry, scope)
    }

    private suspend fun awaitOverrides(
        isDark: Boolean,
        predicate: (Map<ColorTokenId, ColorValue>) -> Boolean
    ): Map<ColorTokenId, ColorValue> = store.overridesFlow(isDark).first(predicate)

    private suspend fun awaitBaseRamps(
        predicate: (Map<ColorRamp, Color>) -> Boolean
    ): Map<ColorRamp, Color> = store.baseRampOverridesFlow().first(predicate)

    private suspend fun overridesInStore(isDark: Boolean): Map<ColorTokenId, ColorValue> =
        store.overridesFlow(isDark).first()

    @Test
    fun `overrides start empty for both modes`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()

        assertEquals(emptyMap<ColorTokenId, ColorValue>(), controller.overrides(isDark = false).value)
        assertEquals(emptyMap<ColorTokenId, ColorValue>(), controller.overrides(isDark = true).value)
    }

    @Test
    fun `the exposed flow mirrors what the store holds`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        val value = ColorValue.Custom(0xFF112233.toInt())

        controller.setOverride(tokenA, isDark = false, value)

        assertEquals(value, controller.overrides(isDark = false).first { it[tokenA] != null }[tokenA])
    }

    @Test
    fun `setOverride is persisted for exactly the requested mode`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        val value = ColorValue.Custom(0xFF112233.toInt())

        controller.setOverride(tokenA, isDark = true, value)

        assertEquals(value, awaitOverrides(isDark = true) { it[tokenA] == value }[tokenA])
        assertNull(overridesInStore(isDark = false)[tokenA])
    }

    @Test
    fun `setOverride with null clears a previously stored override`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        val value = ColorValue.Palette(ColorRamp.GARNET, 40)
        controller.setOverride(tokenA, isDark = false, value)
        awaitOverrides(isDark = false) { it[tokenA] == value }

        controller.setOverride(tokenA, isDark = false, null)

        assertNull(awaitOverrides(isDark = false) { it[tokenA] == null }[tokenA])
    }

    @Test
    fun `resetCategory clears only the tokens of that category and only that mode`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        val value = ColorValue.Custom(0xFF112233.toInt())
        controller.setOverride(tokenA, isDark = false, value)
        controller.setOverride(tokenB, isDark = false, value)
        controller.setOverride(tokenC, isDark = false, value)
        controller.setOverride(tokenA, isDark = true, value)
        awaitOverrides(isDark = false) { it[tokenA] == value && it[tokenB] == value && it[tokenC] == value }
        awaitOverrides(isDark = true) { it[tokenA] == value }

        controller.resetCategory(TestColorTokens.ALPHA, isDark = false)

        val light = awaitOverrides(isDark = false) { it[tokenA] == null && it[tokenB] == null }
        assertEquals(value, light[tokenC])
        assertEquals(value, overridesInStore(isDark = true)[tokenA])
    }

    @Test
    fun `resetAll clears overrides of both modes and the base ramp overrides`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        controller.setOverride(tokenA, isDark = false, ColorValue.Custom(1))
        controller.setOverride(tokenA, isDark = true, ColorValue.Custom(2))
        controller.setBaseRampOverride(ColorRamp.SAPPHIRE, Color(0xFF224466))
        awaitOverrides(isDark = false) { it.isNotEmpty() }
        awaitOverrides(isDark = true) { it.isNotEmpty() }
        awaitBaseRamps { it.isNotEmpty() }

        controller.resetAll()

        assertNull(awaitOverrides(isDark = false) { it[tokenA] == null }[tokenA])
        assertNull(awaitOverrides(isDark = true) { it[tokenA] == null }[tokenA])
        assertNull(awaitBaseRamps { it[ColorRamp.SAPPHIRE] == null }[ColorRamp.SAPPHIRE])
    }

    @Test
    fun `setBaseRampOverride and resetBaseRamp affect only that ramp`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        val color = Color(0xFF224466)
        controller.setBaseRampOverride(ColorRamp.GARNET, color)
        controller.setBaseRampOverride(ColorRamp.EMERALD, color)
        awaitBaseRamps { it[ColorRamp.GARNET] == color && it[ColorRamp.EMERALD] == color }

        controller.resetBaseRamp(ColorRamp.GARNET)

        val after = awaitBaseRamps { it[ColorRamp.GARNET] == null }
        assertEquals(color, after[ColorRamp.EMERALD])
    }

    @Test
    fun `simplifiedViewActive defaults to true and follows the setter`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        assertTrue(store.simplifiedColorViewActiveFlow().first())

        controller.setSimplifiedViewActive(false)

        assertEquals(false, store.simplifiedColorViewActiveFlow().first { !it })
    }

    @Test
    fun `exportToStream writes the selected themes and reports success`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        val value = ColorValue.Custom(0xFF112233.toInt())
        controller.setOverride(tokenA, isDark = false, value)
        awaitOverrides(isDark = false) { it[tokenA] == value }
        val out = ByteArrayOutputStream()

        controller.exportToStream(out, listOf(store.activeThemeIdFlow().first()!!))

        assertEquals(ColorSettingsMessage.EXPORT_SUCCESS, controller.snackbarMessage.first { it != null })
        assertTrue(out.toString().contains(tokenA.value))
        assertTrue(out.toString().contains("Test"))
    }

    @Test
    fun `export then import offers every exported theme preselected`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        controller.setOverride(tokenA, isDark = false, ColorValue.Custom(0xFF112233.toInt()))
        awaitOverrides(isDark = false) { it[tokenA] != null }
        val second = store.createTheme("Zweites")
        store.setOverride(tokenC, false, ColorValue.Palette(ColorRamp.GARNET, 40))
        val out = ByteArrayOutputStream()
        controller.exportToStream(out, store.themesFlow().first().map { it.id })
        controller.snackbarMessage.first { it == ColorSettingsMessage.EXPORT_SUCCESS }

        controller.loadImportCandidates(out.toByteArray().inputStream(), FALLBACK_NAME)

        val candidates = controller.importCandidates.first { it.size == 2 }
        assertTrue(candidates.all { it.selected })
        assertEquals(listOf("Test", second.name), candidates.map { it.entry.name })
    }

    @Test
    fun `confirmImport imports only the selected themes as new themes and closes the dialog`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        val json = """{"schemaVersion":3,"themes":[
            {"name":"Eins","overridesLight":{"${tokenA.value}":"C|-16776961"}},
            {"name":"Zwei","overridesLight":{"${tokenC.value}":"C|-65536"}}]}"""
        controller.loadImportCandidates(json.byteInputStream(), FALLBACK_NAME)
        controller.importCandidates.first { it.size == 2 }
        controller.toggleImportCandidate(1)

        controller.confirmImport()

        assertEquals(ColorSettingsMessage.IMPORT_SUCCESS, controller.snackbarMessage.first { it != null })
        assertEquals(listOf("Test", "Eins"), store.themesFlow().first().map { it.name })
        assertTrue(controller.importCandidates.first { it.isEmpty() }.isEmpty())
    }

    @Test
    fun `importing never changes the active theme or its colors`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        val activeBefore = store.activeThemeIdFlow().first()
        val json = """{"schemaVersion":3,"themes":[{"name":"Eins","overridesLight":{"${tokenA.value}":"C|-16776961"}}]}"""
        controller.loadImportCandidates(json.byteInputStream(), FALLBACK_NAME)
        controller.importCandidates.first { it.isNotEmpty() }

        controller.confirmImport()
        controller.importCandidates.first { it.isEmpty() }

        assertEquals(activeBefore, store.activeThemeIdFlow().first())
        assertNull(overridesInStore(isDark = false)[tokenA])
    }

    @Test
    fun `setAllImportCandidatesSelected flips every candidate at once`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        val json = """{"schemaVersion":3,"themes":[
            {"name":"Eins","overridesLight":{"${tokenA.value}":"C|-16776961"}},
            {"name":"Zwei","overridesLight":{"${tokenC.value}":"C|-65536"}}]}"""
        controller.loadImportCandidates(json.byteInputStream(), FALLBACK_NAME)
        controller.importCandidates.first { it.size == 2 }

        controller.setAllImportCandidatesSelected(false)

        assertTrue(controller.importCandidates.value.none { it.selected })
    }

    @Test
    fun `dismissImport drops the candidates without importing them`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        val json = """{"schemaVersion":2,"overridesLight":{"${tokenA.value}":"C|-16776961"}}"""
        controller.loadImportCandidates(json.byteInputStream(), FALLBACK_NAME)
        controller.importCandidates.first { it.isNotEmpty() }

        controller.dismissImport()

        assertTrue(controller.importCandidates.value.isEmpty())
        assertEquals(1, store.themesFlow().first().size)
    }

    @Test
    fun `a file without a single known token id reports an empty import`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        val json = """{"schemaVersion":2,"overridesLight":{"unknown.token":"C|-16776961"}}"""

        controller.loadImportCandidates(json.byteInputStream(), FALLBACK_NAME)

        assertEquals(ColorSettingsMessage.IMPORT_EMPTY, controller.snackbarMessage.first { it != null })
        assertTrue(controller.importCandidates.value.isEmpty())
    }

    @Test
    fun `an unreadable file reports an import error`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()

        controller.loadImportCandidates("not json".byteInputStream(), FALLBACK_NAME)

        assertEquals(ColorSettingsMessage.IMPORT_ERROR, controller.snackbarMessage.first { it != null })
        assertTrue(controller.importCandidates.value.isEmpty())
    }

    @Test
    fun `an edit in the standard theme is held back until a theme name is given`() = runTest(timeout = TEST_TIMEOUT) {
        store.activateTheme(null)
        val controller = controller()

        controller.setOverride(tokenA, isDark = false, ColorValue.Custom(1))

        assertTrue(controller.themeNameRequested.first { it })
        assertTrue(store.themesFlow().first().size == 1)
        assertNull(overridesInStore(isDark = false)[tokenA])
    }

    @Test
    fun `createThemeForEdit creates and activates the theme and then applies the held edit`() = runTest(timeout = TEST_TIMEOUT) {
        store.activateTheme(null)
        val controller = controller()
        controller.setOverride(tokenA, isDark = false, ColorValue.Custom(1))
        controller.themeNameRequested.first { it }

        controller.createThemeForEdit("Mein Neues")

        assertEquals(ColorValue.Custom(1), awaitOverrides(isDark = false) { it[tokenA] != null }[tokenA])
        assertEquals("Mein Neues", store.themesFlow().first().last().name)
        assertEquals(false, controller.themeNameRequested.value)
    }

    @Test
    fun `dismissThemeNameRequest drops the held edit`() = runTest(timeout = TEST_TIMEOUT) {
        store.activateTheme(null)
        val controller = controller()
        controller.setBaseRampOverride(ColorRamp.GARNET, Color(0xFF112233))
        controller.themeNameRequested.first { it }

        controller.dismissThemeNameRequest()

        assertEquals(false, controller.themeNameRequested.value)
        assertTrue(store.baseRampOverridesFlow().first().isEmpty())
        assertEquals(1, store.themesFlow().first().size)
    }

    @Test
    fun `the standard theme itself is never modified by an edit`() = runTest(timeout = TEST_TIMEOUT) {
        store.activateTheme(null)
        val controller = controller()

        controller.resetAll()

        assertTrue(controller.themeNameRequested.first { it })
        assertNull(store.activeThemeIdFlow().first())
    }

    @Test
    fun `theme management actions reach the store`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()

        controller.createTheme("Zwei")
        val two = store.themesFlow().first { it.size == 2 }.last()
        controller.renameTheme(two.id, "Zwei!")
        assertEquals("Zwei!", store.themesFlow().first { list -> list.any { it.name == "Zwei!" } }.last().name)

        controller.activateTheme(null)
        assertNull(store.activeThemeIdFlow().first { id -> id == null })

        controller.deleteTheme(two.id)
        assertEquals(1, store.themesFlow().first { it.size == 1 }.size)
    }

    @Test
    fun `clearSnackbar removes the pending message`() = runTest(timeout = TEST_TIMEOUT) {
        val controller = controller()
        controller.loadImportCandidates("not json".byteInputStream(), FALLBACK_NAME)
        controller.snackbarMessage.first { it != null }

        controller.clearSnackbar()

        assertNull(controller.snackbarMessage.value)
    }

    @Test
    fun `every message maps to its own string resource`() {
        val ids = ColorSettingsMessage.entries.map { it.labelRes }
        assertEquals(ColorSettingsMessage.entries.size, ids.distinct().size)
    }

    @Test
    fun `the controller works with any coroutine scope, not just a view model scope`() = runTest(timeout = TEST_TIMEOUT) {
        val ownScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scopes += ownScope
        val controller = ColorSettingsController(store, exportRepository, registry, ownScope)
        val value = ColorValue.Custom(7)

        controller.setOverride(tokenA, isDark = false, value)

        assertEquals(value, awaitOverrides(isDark = false) { it[tokenA] == value }[tokenA])
    }

    private companion object {
        val TEST_TIMEOUT = 30.seconds
        const val FALLBACK_NAME = "Theme"
    }
}
