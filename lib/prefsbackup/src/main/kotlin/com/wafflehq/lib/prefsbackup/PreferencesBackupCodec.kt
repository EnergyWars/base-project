package com.wafflehq.lib.prefsbackup

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object PreferencesBackupCodec {

    fun encode(preferences: Preferences): Map<String, String> =
        preferences.asMap().entries.associate { (key, value) -> key.name to encodeValue(value) }

    fun decodeInto(target: MutablePreferences, snapshot: Map<String, String>) {
        if (snapshot.size > MAX_ENTRIES) return
        val existingTags = target.asMap().entries.associate { (key, value) -> key.name to tagOf(value) }
        snapshot.forEach { (name, tagged) ->
            if (name.length > MAX_NAME_LENGTH || tagged.length > MAX_VALUE_LENGTH) return@forEach
            runCatching { applyEntry(target, name, tagged, existingTags[name]) }
        }
    }

    private fun tagOf(value: Any): String = when (value) {
        is Boolean -> TAG_BOOLEAN
        is Int -> TAG_INT
        is Long -> TAG_LONG
        is Float -> TAG_FLOAT
        is Double -> TAG_DOUBLE
        is Set<*> -> TAG_STRING_SET
        else -> TAG_STRING
    }

    private fun encodeValue(value: Any): String = when (value) {
        is Boolean -> "$TAG_BOOLEAN$SEPARATOR$value"
        is Int -> "$TAG_INT$SEPARATOR$value"
        is Long -> "$TAG_LONG$SEPARATOR$value"
        is Float -> "$TAG_FLOAT$SEPARATOR$value"
        is Double -> "$TAG_DOUBLE$SEPARATOR$value"
        is String -> "$TAG_STRING$SEPARATOR$value"
        is Set<*> -> "$TAG_STRING_SET$SEPARATOR" + value.filterIsInstance<String>().joinToString(SET_ITEM_SEPARATOR)
        else -> "$TAG_STRING$SEPARATOR$value"
    }

    private fun applyEntry(target: MutablePreferences, name: String, tagged: String, existingTag: String?) {
        val separatorIndex = tagged.indexOf(SEPARATOR)
        require(separatorIndex >= 0) { "Missing type tag for preference \"$name\"" }
        val tag = tagged.substring(0, separatorIndex)
        val raw = tagged.substring(separatorIndex + 1)
        require(existingTag == null || existingTag == tag) { "Type of preference \"$name\" does not match the stored type" }
        when (tag) {
            TAG_BOOLEAN -> target[booleanPreferencesKey(name)] = raw.toBoolean()
            TAG_INT -> target[intPreferencesKey(name)] = raw.toInt()
            TAG_LONG -> target[longPreferencesKey(name)] = raw.toLong()
            TAG_FLOAT -> target[floatPreferencesKey(name)] = raw.toFloat().also { require(it.isFinite()) }
            TAG_DOUBLE -> target[doublePreferencesKey(name)] = raw.toDouble().also { require(it.isFinite()) }
            TAG_STRING -> target[stringPreferencesKey(name)] = raw
            TAG_STRING_SET -> target[stringSetPreferencesKey(name)] =
                if (raw.isEmpty()) emptySet() else raw.split(SET_ITEM_SEPARATOR).toSet()
        }
    }

    private const val MAX_ENTRIES = 5_000
    private const val MAX_NAME_LENGTH = 200
    private const val MAX_VALUE_LENGTH = 500_000
    private const val SEPARATOR = ':'
    private const val SET_ITEM_SEPARATOR = ""
    private const val TAG_BOOLEAN = "b"
    private const val TAG_INT = "i"
    private const val TAG_LONG = "l"
    private const val TAG_FLOAT = "f"
    private const val TAG_DOUBLE = "d"
    private const val TAG_STRING = "s"
    private const val TAG_STRING_SET = "ss"
}
