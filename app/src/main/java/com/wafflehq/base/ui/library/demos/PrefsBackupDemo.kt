package com.wafflehq.base.ui.library.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.toMutablePreferences
import com.wafflehq.base.R
import com.wafflehq.lib.prefsbackup.PreferencesBackupCodec
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppBanner
import com.wafflehq.lib.uicore.components.AppBannerRole
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.theme.AppSpacing

internal object PrefsBackupDemoLogic {

    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val COUNTER = intPreferencesKey("counter")
    val NAME = stringPreferencesKey("display_name")
    val TAGS = stringSetPreferencesKey("tags")
    val LAST_SYNC = longPreferencesKey("last_sync")
    val RATIO = floatPreferencesKey("ratio")
    val PRECISE = doublePreferencesKey("precise")

    const val DEFAULT_NAME = "Waffle"
    private const val SAMPLE_LAST_SYNC = 1_700_000_000_000L
    private const val SAMPLE_RATIO = 0.75f
    private const val SAMPLE_PRECISE = 3.14159
    private const val TAMPERED_VALUE = "s:tampered"

    fun preferences(darkMode: Boolean, counter: Int, name: String): Preferences = mutablePreferencesOf(
        DARK_MODE to darkMode,
        COUNTER to counter,
        NAME to name,
        TAGS to setOf("alpha", "beta"),
        LAST_SYNC to SAMPLE_LAST_SYNC,
        RATIO to SAMPLE_RATIO,
        PRECISE to SAMPLE_PRECISE,
    )

    fun encode(preferences: Preferences): Map<String, String> = PreferencesBackupCodec.encode(preferences)

    fun restore(snapshot: Map<String, String>): Preferences {
        val target = mutablePreferencesOf()
        PreferencesBackupCodec.decodeInto(target, snapshot)
        return target
    }

    fun roundTripMatches(original: Preferences, restored: Preferences): Boolean = original.asMap() == restored.asMap()

    fun tamperedSnapshot(snapshot: Map<String, String>): Map<String, String> = snapshot + (COUNTER.name to TAMPERED_VALUE)

    fun restoreOverExisting(existing: Preferences, snapshot: Map<String, String>): Preferences {
        val target = existing.toMutablePreferences()
        PreferencesBackupCodec.decodeInto(target, snapshot)
        return target
    }

    fun tamperingRejected(original: Preferences, snapshot: Map<String, String>): Boolean {
        val result = restoreOverExisting(original, tamperedSnapshot(snapshot))
        return result[COUNTER] == original[COUNTER]
    }
}

internal object PrefsBackupTags {
    const val NAME = "libex_prefsbackup_name"
    const val DARK = "libex_prefsbackup_dark"
    const val COUNTER = "libex_prefsbackup_counter"
    const val SNAPSHOT = "libex_prefsbackup_snapshot"
    const val RESTORE = "libex_prefsbackup_restore"
    const val RESTORE_RESULT = "libex_prefsbackup_restore_result"
    const val TAMPER = "libex_prefsbackup_tamper"
    const val TAMPER_RESULT = "libex_prefsbackup_tamper_result"
}

@Composable
internal fun PrefsBackupDemo() {
    var name by remember { mutableStateOf(PrefsBackupDemoLogic.DEFAULT_NAME) }
    var darkMode by remember { mutableStateOf(true) }
    var counter by remember { mutableIntStateOf(3) }
    var restoreOk by remember { mutableStateOf<Boolean?>(null) }
    var tamperRejected by remember { mutableStateOf<Boolean?>(null) }
    val preferences = remember(name, darkMode, counter) { PrefsBackupDemoLogic.preferences(darkMode, counter, name) }
    val snapshot = remember(preferences) { PrefsBackupDemoLogic.encode(preferences) }

    DemoSection(
        id = "prefsbackup",
        titleRes = R.string.libex_prefsbackup_title,
        descriptionRes = R.string.libex_prefsbackup_desc,
        moduleRes = R.string.libex_module_prefsbackup,
    ) {
        AppTextField(
            value = name,
            onValueChange = {
                name = it
                restoreOk = null
                tamperRejected = null
            },
            label = { Text(stringResource(R.string.libex_prefsbackup_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(PrefsBackupTags.NAME),
        )
        DemoSwitchRow(
            label = stringResource(R.string.libex_prefsbackup_dark),
            checked = darkMode,
            onCheckedChange = {
                darkMode = it
                restoreOk = null
                tamperRejected = null
            },
            tag = PrefsBackupTags.DARK,
        )
        DemoStepperRow(
            label = stringResource(R.string.libex_prefsbackup_counter),
            value = counter,
            range = 0..99,
            step = 1,
            onValueChange = {
                counter = it
                restoreOk = null
                tamperRejected = null
            },
            tag = PrefsBackupTags.COUNTER,
        )
        AppHorizontalDivider()
        DemoLabelText(stringResource(R.string.libex_prefsbackup_snapshot, snapshot.size))
        snapshot.entries.sortedBy { it.key }.forEach { (key, value) ->
            DemoKeyValue(
                label = key,
                value = value,
                modifier = Modifier.testTag(PrefsBackupTags.SNAPSHOT),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppButton(
                text = stringResource(R.string.libex_prefsbackup_restore),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                onClick = {
                    restoreOk = PrefsBackupDemoLogic.roundTripMatches(preferences, PrefsBackupDemoLogic.restore(snapshot))
                },
                modifier = Modifier.testTag(PrefsBackupTags.RESTORE),
            )
            AppButton(
                text = stringResource(R.string.libex_prefsbackup_tamper),
                role = AppButtonRole.Warning,
                variant = AppButtonVariant.Outlined,
                onClick = { tamperRejected = PrefsBackupDemoLogic.tamperingRejected(preferences, snapshot) },
                modifier = Modifier.testTag(PrefsBackupTags.TAMPER),
            )
        }
        restoreOk?.let {
            AppBanner(
                text = stringResource(if (it) R.string.libex_prefsbackup_restore_ok else R.string.libex_prefsbackup_restore_mismatch),
                role = if (it) AppBannerRole.Secondary else AppBannerRole.Error,
                modifier = Modifier.fillMaxWidth().testTag(PrefsBackupTags.RESTORE_RESULT),
            )
        }
        tamperRejected?.let {
            AppBanner(
                text = stringResource(if (it) R.string.libex_prefsbackup_tamper_rejected else R.string.libex_prefsbackup_tamper_accepted),
                role = if (it) AppBannerRole.Secondary else AppBannerRole.Error,
                modifier = Modifier.fillMaxWidth().testTag(PrefsBackupTags.TAMPER_RESULT),
            )
        }
    }
}
