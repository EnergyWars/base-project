package com.wafflehq.lib.prefsbackup

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class PreferencesBackupCodecTest {

    private fun emptyMutable() = emptyPreferences().toMutablePreferences()

    @Test
    fun `round trips every supported preference type`() {
        val source = emptyMutable().apply {
            set(booleanPreferencesKey("bool_key"), true)
            set(intPreferencesKey("int_key"), 42)
            set(longPreferencesKey("long_key"), 123456789012345L)
            set(floatPreferencesKey("float_key"), 3.14f)
            set(stringPreferencesKey("string_key"), "hello world")
            set(stringSetPreferencesKey("set_key"), setOf("a", "b", "c"))
        }.toPreferences()

        val snapshot = PreferencesBackupCodec.encode(source)
        val target = emptyMutable()
        PreferencesBackupCodec.decodeInto(target, snapshot)

        assertEquals(true, target[booleanPreferencesKey("bool_key")])
        assertEquals(42, target[intPreferencesKey("int_key")])
        assertEquals(123456789012345L, target[longPreferencesKey("long_key")])
        assertEquals(3.14f, target[floatPreferencesKey("float_key")])
        assertEquals("hello world", target[stringPreferencesKey("string_key")])
        assertEquals(setOf("a", "b", "c"), target[stringSetPreferencesKey("set_key")])
    }

    @Test
    fun `round trips an empty string set`() {
        val source: Preferences = emptyMutable().apply {
            set(stringSetPreferencesKey("empty_set"), emptySet())
        }.toPreferences()

        val snapshot = PreferencesBackupCodec.encode(source)
        val target = emptyMutable()
        PreferencesBackupCodec.decodeInto(target, snapshot)

        assertEquals(emptySet<String>(), target[stringSetPreferencesKey("empty_set")])
    }

    @Test
    fun `skips entries with an unrecognised type tag instead of throwing`() {
        val target = emptyMutable()

        PreferencesBackupCodec.decodeInto(target, mapOf("bad_key" to "z:whatever"))

        assertTrue(target.asMap().isEmpty())
    }

    @Test
    fun `skips entries with an unparsable value instead of throwing`() {
        val target = emptyMutable()

        PreferencesBackupCodec.decodeInto(target, mapOf("bad_int" to "i:not-a-number"))

        assertTrue(target.asMap().isEmpty())
    }

    @Test
    fun `skips entries missing a type tag separator instead of throwing`() {
        val target = emptyMutable()

        PreferencesBackupCodec.decodeInto(target, mapOf("bad_key" to "novalueseparator"))

        assertTrue(target.asMap().isEmpty())
    }

    @Test
    fun `decodeInto adds new keys without touching existing unrelated ones`() {
        val target = emptyMutable().apply { set(stringPreferencesKey("existing"), "kept") }

        PreferencesBackupCodec.decodeInto(target, mapOf("new_key" to "s:added"))

        assertEquals("kept", target[stringPreferencesKey("existing")])
        assertEquals("added", target[stringPreferencesKey("new_key")])
    }

    @Test
    fun `does not overwrite an existing preference with a value of a different type`() {
        val target = emptyMutable().apply { set(intPreferencesKey("week_view_day_count"), 7) }

        PreferencesBackupCodec.decodeInto(target, mapOf("week_view_day_count" to "s:abc"))

        assertEquals(7, target[intPreferencesKey("week_view_day_count")])
    }

    @Test
    fun `overwrites an existing preference when the type matches`() {
        val target = emptyMutable().apply { set(intPreferencesKey("week_view_day_count"), 7) }

        PreferencesBackupCodec.decodeInto(target, mapOf("week_view_day_count" to "i:5"))

        assertEquals(5, target[intPreferencesKey("week_view_day_count")])
    }

    @Test
    fun `skips non finite floating point values`() {
        val target = emptyMutable()

        PreferencesBackupCodec.decodeInto(
            target,
            mapOf("f_nan" to "f:NaN", "f_inf" to "f:Infinity", "d_nan" to "d:NaN", "d_neg_inf" to "d:-Infinity")
        )

        assertTrue(target.asMap().isEmpty())
    }

    @Test
    fun `skips entries with an overlong name or value`() {
        val target = emptyMutable()

        PreferencesBackupCodec.decodeInto(
            target,
            mapOf("n".repeat(201) to "s:x", "long_value" to "s:" + "v".repeat(500_001), "ok" to "s:fine")
        )

        assertEquals(setOf("ok"), target.asMap().keys.map { it.name }.toSet())
    }

    @Test
    fun `ignores a snapshot with an absurd number of entries`() {
        val target = emptyMutable()
        val snapshot = (0..5_000).associate { "key_$it" to "s:$it" }

        PreferencesBackupCodec.decodeInto(target, snapshot)

        assertTrue(target.asMap().isEmpty())
    }

    @Test
    fun `accepts a snapshot exactly at the entry limit`() {
        val target = emptyMutable()
        val snapshot = (0 until 5_000).associate { "key_$it" to "s:$it" }

        PreferencesBackupCodec.decodeInto(target, snapshot)

        assertEquals(5_000, target.asMap().size)
    }
}
