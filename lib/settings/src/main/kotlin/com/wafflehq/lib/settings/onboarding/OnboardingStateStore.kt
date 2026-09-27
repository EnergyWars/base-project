package com.wafflehq.lib.settings.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal val Context.onboardingDataStore by preferencesDataStore("onboarding_prefs")

private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
private val KEY_STARTUP_PERMISSION_CHECK_DONE = booleanPreferencesKey("startup_permission_check_done")
private val KEY_XIAOMI_AUTOSTART_HINT_SHOWN = booleanPreferencesKey("xiaomi_autostart_hint_shown")
private val KEY_XIAOMI_AUTOSTART_OPENED = booleanPreferencesKey("xiaomi_autostart_opened")
private val KEY_SEEN_MODULE_ONBOARDING_IDS = stringSetPreferencesKey("seen_module_onboarding_ids")
private val KEY_SEEN_MODULES_MIGRATED = booleanPreferencesKey("seen_module_onboarding_migrated")

class OnboardingStateStore(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.onboardingDataStore)

    val onboardingCompleted: Flow<Boolean> = dataStore.data
        .map { it[KEY_ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    val startupPermissionCheckDone: Flow<Boolean> = dataStore.data
        .map { it[KEY_STARTUP_PERMISSION_CHECK_DONE] ?: false }

    suspend fun setStartupPermissionCheckDone(done: Boolean) {
        dataStore.edit { it[KEY_STARTUP_PERMISSION_CHECK_DONE] = done }
    }

    val xiaomiAutostartHintShown: Flow<Boolean> = dataStore.data
        .map { it[KEY_XIAOMI_AUTOSTART_HINT_SHOWN] ?: false }

    suspend fun setXiaomiAutostartHintShown(shown: Boolean) {
        dataStore.edit { it[KEY_XIAOMI_AUTOSTART_HINT_SHOWN] = shown }
    }

    val xiaomiAutostartOpened: Flow<Boolean> = dataStore.data
        .map { it[KEY_XIAOMI_AUTOSTART_OPENED] ?: false }

    suspend fun setXiaomiAutostartOpened(opened: Boolean) {
        dataStore.edit { it[KEY_XIAOMI_AUTOSTART_OPENED] = opened }
    }

    suspend fun consumeModuleOnboardingTrigger(moduleId: String): Boolean {
        var shouldShow = false
        dataStore.edit { prefs ->
            val seenIds = prefs[KEY_SEEN_MODULE_ONBOARDING_IDS] ?: emptySet()
            if (moduleId !in seenIds) {
                shouldShow = true
                prefs[KEY_SEEN_MODULE_ONBOARDING_IDS] = seenIds + moduleId
            }
        }
        return shouldShow
    }

    suspend fun markActiveModulesSeenForExistingUser(activeModuleIds: Collection<String>) {
        dataStore.edit { prefs ->
            if (prefs[KEY_SEEN_MODULES_MIGRATED] == true) return@edit
            if (prefs[KEY_ONBOARDING_COMPLETED] == true) {
                prefs[KEY_SEEN_MODULE_ONBOARDING_IDS] =
                    (prefs[KEY_SEEN_MODULE_ONBOARDING_IDS] ?: emptySet()) + activeModuleIds
            }
            prefs[KEY_SEEN_MODULES_MIGRATED] = true
        }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
