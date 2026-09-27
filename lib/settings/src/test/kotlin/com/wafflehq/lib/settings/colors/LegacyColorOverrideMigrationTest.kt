package com.wafflehq.lib.settings.colors

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class LegacyColorOverrideMigrationTest {

    private fun prefsOf(vararg pairs: Pair<String, String>): Preferences =
        emptyPreferences().toMutablePreferences().apply {
            pairs.forEach { (name, value) -> set(stringPreferencesKey(name), value) }
        }.toPreferences()

    @Test
    fun `shouldMigrate is true when a legacy key is present`() = runTest {
        val prefs = prefsOf("override_test.token" to "C|-16776961")
        assertTrue(LegacyColorOverrideMigration.shouldMigrate(prefs))
    }

    @Test
    fun `shouldMigrate is false once only mode-scoped keys exist`() = runTest {
        val prefs = prefsOf(
            "override_light_test.token" to "C|-16776961",
            "override_dark_test.token" to "C|-16776961"
        )
        assertFalse(LegacyColorOverrideMigration.shouldMigrate(prefs))
    }

    @Test
    fun `shouldMigrate is false when nothing is stored yet`() = runTest {
        assertFalse(LegacyColorOverrideMigration.shouldMigrate(emptyPreferences()))
    }

    @Test
    fun `migrate copies a legacy value into both light and dark keys and removes the legacy key`() = runTest {
        val prefs = prefsOf("override_test.token" to "C|-16776961")

        val migrated = LegacyColorOverrideMigration.migrate(prefs)

        assertEquals("C|-16776961", migrated[stringPreferencesKey("override_light_test.token")])
        assertEquals("C|-16776961", migrated[stringPreferencesKey("override_dark_test.token")])
        assertNull(migrated[stringPreferencesKey("override_test.token")])
    }

    @Test
    fun `migrate leaves already mode-scoped keys and unrelated data untouched`() = runTest {
        val prefs = prefsOf(
            "override_light_other.token" to "P|SAPPHIRE|40|1.0",
            "override_dark_other.token" to "P|SAPPHIRE|80|1.0",
            "override_legacy.token" to "C|-1"
        )

        val migrated = LegacyColorOverrideMigration.migrate(prefs)

        assertEquals("P|SAPPHIRE|40|1.0", migrated[stringPreferencesKey("override_light_other.token")])
        assertEquals("P|SAPPHIRE|80|1.0", migrated[stringPreferencesKey("override_dark_other.token")])
        assertEquals("C|-1", migrated[stringPreferencesKey("override_light_legacy.token")])
        assertEquals("C|-1", migrated[stringPreferencesKey("override_dark_legacy.token")])
        assertNull(migrated[stringPreferencesKey("override_legacy.token")])
    }
}
