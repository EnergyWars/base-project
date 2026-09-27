package com.wafflehq.lib.settings.colors

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream

class ColorThemeExportRepositoryTest {

    private lateinit var store: ColorOverrideStore
    private lateinit var repository: ColorThemeExportRepository

    private val tokenA = TestColorTokens.alphaAccent
    private val tokenB = TestColorTokens.betaAccent

    @Before
    fun setUp() {
        store = ColorOverrideStore(FakePreferencesDataStore())
        repository = ColorThemeExportRepository(store, TestColorTokens.registry)
    }

    private suspend fun export(vararg ids: String): ByteArray =
        ByteArrayOutputStream().also { repository.exportToStream(it, ids.toList()).getOrThrow() }.toByteArray()

    private suspend fun parse(json: String) = repository.parseImport(json.byteInputStream(), FALLBACK).getOrThrow()

    @Test
    fun `a single theme round trips light dark and base colors`() = runTest {
        val theme = store.createTheme("Sonnenuntergang")
        store.setOverride(tokenA, false, ColorValue.Palette(ColorRamp.GARNET, 40))
        store.setOverride(tokenB, false, ColorValue.Custom(0xFF112233.toInt()))
        store.setOverride(tokenA, true, ColorValue.Custom(0xFF445566.toInt()))
        store.setBaseRampOverride(ColorRamp.SAPPHIRE, androidx.compose.ui.graphics.Color(0xFF998877))

        val entries = repository.parseImport(export(theme.id).inputStream(), FALLBACK).getOrThrow()

        val entry = entries.single()
        assertEquals("Sonnenuntergang", entry.name)
        assertEquals(store.themeData(theme.id), entry.data)
    }

    @Test
    fun `several themes export into one file and only the selected ones`() = runTest {
        val a = store.createTheme("A")
        store.setOverride(tokenA, false, ColorValue.Custom(1))
        val b = store.createTheme("B")
        store.setOverride(tokenA, false, ColorValue.Custom(2))
        val c = store.createTheme("C")
        store.setOverride(tokenA, false, ColorValue.Custom(3))

        val entries = repository.parseImport(export(a.id, c.id).inputStream(), FALLBACK).getOrThrow()

        assertEquals(listOf("A", "C"), entries.map { it.name })
        assertTrue(entries.none { it.name == b.name })
    }

    @Test
    fun `exporting an unknown theme id writes an empty file that imports nothing`() = runTest {
        store.createTheme("A")

        assertTrue(repository.parseImport(export("missing").inputStream(), FALLBACK).getOrThrow().isEmpty())
    }

    @Test
    fun `applyImport stores every entry as its own theme`() = runTest {
        val data = ColorThemeData(overridesLight = mapOf(tokenA.value to ColorValueCodec.encode(ColorValue.Custom(7))))

        val added = repository.applyImport(listOf(ColorThemeExportEntry("X", data), ColorThemeExportEntry("Y", data)))

        assertEquals(listOf("X", "Y"), added.map { it.name })
        assertEquals(data, store.themeData(added.first().id))
        assertEquals(2, store.themesFlow().first().size)
    }

    @Test
    fun `parseImport skips token ids the registry does not know`() = runTest {
        val json = """{"schemaVersion":3,"themes":[{"name":"X","overridesLight":{"unknown.token.id":"C|-16776961"}}]}"""

        assertTrue(parse(json).isEmpty())
    }

    @Test
    fun `parseImport skips entries with an invalid encoded value`() = runTest {
        val json = """{"schemaVersion":3,"themes":[{"name":"X","overridesLight":{"${tokenA.value}":"garbage"}}]}"""

        assertTrue(parse(json).isEmpty())
    }

    @Test
    fun `parseImport keeps a theme that consists of base colors only`() = runTest {
        val json = """{"schemaVersion":3,"themes":[{"name":"X","baseRamps":{"GARNET":-16776961,"NOPE":5}}]}"""

        val entry = parse(json).single()

        assertEquals(mapOf("GARNET" to -16776961), entry.data.baseRamps)
    }

    @Test
    fun `parseImport names an unnamed theme after the fallback`() = runTest {
        val json = """{"schemaVersion":3,"themes":[{"name":"  ","baseRamps":{"GARNET":-16776961}}]}"""

        assertEquals("$FALLBACK 1", parse(json).single().name)
    }

    @Test
    fun `parseImport truncates overlong theme names`() = runTest {
        val longName = "N".repeat(MAX_THEME_NAME_LENGTH * 50)
        val json = """{"schemaVersion":3,"themes":[{"name":"$longName","baseRamps":{"GARNET":-16776961}}]}"""

        assertEquals("N".repeat(MAX_THEME_NAME_LENGTH), parse(json).single().name)
    }

    @Test
    fun `parseImport ignores themes beyond the import limit`() = runTest {
        val themes = (1..MAX_IMPORTED_THEMES + 25).joinToString(",") { """{"name":"T$it","baseRamps":{"GARNET":-16776961}}""" }
        val json = """{"schemaVersion":3,"themes":[$themes]}"""

        val entries = parse(json)

        assertEquals(MAX_IMPORTED_THEMES, entries.size)
        assertEquals("T$MAX_IMPORTED_THEMES", entries.last().name)
    }

    @Test
    fun `parseImport fails on malformed json`() = runTest {
        assertTrue(repository.parseImport("not json".byteInputStream(), FALLBACK).isFailure)
    }

    @Test
    fun `a legacy schemaVersion 1 export becomes one theme with light and dark`() = runTest {
        val json = """{"schemaVersion":1,"overrides":{"${tokenA.value}":"C|-16776961"}}"""

        val entry = parse(json).single()

        assertEquals(FALLBACK, entry.name)
        assertEquals(mapOf(tokenA.value to "C|-16776961"), entry.data.overridesLight)
        assertEquals(mapOf(tokenA.value to "C|-16776961"), entry.data.overridesDark)
    }

    @Test
    fun `a legacy schemaVersion 2 export keeps light and dark separate`() = runTest {
        val json = """{"schemaVersion":2,"overridesLight":{"${tokenA.value}":"C|1"},"overridesDark":{"${tokenB.value}":"C|2"}}"""

        val entry = parse(json).single()

        assertEquals(mapOf(tokenA.value to "C|1"), entry.data.overridesLight)
        assertEquals(mapOf(tokenB.value to "C|2"), entry.data.overridesDark)
    }

    @Test
    fun `tokenCount sums light and dark overrides`() {
        val data = ColorThemeData(overridesLight = mapOf("a" to "C|1", "b" to "C|2"), overridesDark = mapOf("a" to "C|3"))

        assertEquals(3, ColorThemeExportEntry("X", data).tokenCount)
    }

    private companion object {
        const val FALLBACK = "Theme"
    }
}
