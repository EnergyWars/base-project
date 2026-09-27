package com.wafflehq.lib.settings.colors

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyColorThemeMigrationTest {

    private val migration = LegacyColorThemeMigration("Mein Theme")

    private fun prefsOf(vararg pairs: Pair<String, String>): Preferences =
        emptyPreferences().toMutablePreferences().apply {
            pairs.forEach { (name, value) -> set(stringPreferencesKey(name), value) }
        }.toPreferences()

    private fun libraryOf(prefs: Preferences) = decodeLibrary(prefs[LIBRARY_KEY])!!

    @Test
    fun `nothing to migrate for an empty store`() = runTest {
        assertFalse(migration.shouldMigrate(emptyPreferences()))
    }

    @Test
    fun `mode scoped keys trigger the migration`() = runTest {
        assertTrue(migration.shouldMigrate(prefsOf("override_light_a.b" to "C|1")))
        assertTrue(migration.shouldMigrate(prefsOf("base_ramp_SAPPHIRE" to "5")))
    }

    @Test
    fun `existing customizations become one active theme`() = runTest {
        val prefs = prefsOf(
            "override_light_a.b" to "C|1",
            "override_dark_a.b" to "C|2",
            "base_ramp_SAPPHIRE" to "-16711936"
        )

        val library = libraryOf(migration.migrate(prefs))

        val theme = library.themes.single()
        assertEquals("Mein Theme", theme.name)
        assertEquals(theme.id, library.activeId)
        assertEquals(mapOf("a.b" to "C|1"), theme.data.overridesLight)
        assertEquals(mapOf("a.b" to "C|2"), theme.data.overridesDark)
        assertEquals(mapOf("SAPPHIRE" to -16711936), theme.data.baseRamps)
    }

    @Test
    fun `the legacy keys are removed after migrating`() = runTest {
        val migrated = migration.migrate(prefsOf("override_light_a.b" to "C|1", "base_ramp_SAPPHIRE" to "5"))

        assertNull(migrated[stringPreferencesKey("override_light_a.b")])
        assertNull(migrated[stringPreferencesKey("base_ramp_SAPPHIRE")])
    }

    @Test
    fun `unrelated preferences survive the migration`() = runTest {
        val migrated = migration.migrate(prefsOf("override_light_a.b" to "C|1", "other" to "x"))

        assertEquals("x", migrated[stringPreferencesKey("other")])
    }

    @Test
    fun `a corrupt base ramp value is dropped instead of failing`() = runTest {
        val library = libraryOf(migration.migrate(prefsOf("base_ramp_GARNET" to "abc", "override_light_a.b" to "C|1")))

        assertTrue(library.themes.single().data.baseRamps.isEmpty())
    }

    @Test
    fun `an existing library keeps its themes and gets the migrated one appended`() = runTest {
        val existing = ColorThemeLibrary(activeId = "x", themes = listOf(StoredColorTheme("x", "Mein Theme")))
        val prefs = emptyPreferences().toMutablePreferences().apply {
            set(LIBRARY_KEY, colorJson.encodeToString(existing))
            set(stringPreferencesKey("override_light_a.b"), "C|1")
        }.toPreferences()

        val library = libraryOf(migration.migrate(prefs))

        assertEquals(listOf("Mein Theme", "Mein Theme (2)"), library.themes.map { it.name })
        assertEquals("x", library.activeId)
    }
}
