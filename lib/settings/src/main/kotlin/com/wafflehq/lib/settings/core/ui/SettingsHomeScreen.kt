package com.wafflehq.lib.settings.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.lib.navigation.settings.SettingsHomeEntry
import com.wafflehq.lib.navigation.settings.SettingsHomePage
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.core.SettingsSection

@Composable
fun SettingsHomeScreen(
    sections: List<SettingsSection>,
    title: String = stringResource(R.string.appsettings_title),
    onBack: (() -> Unit)? = null
) {
    val entries = sections.sortedBy { it.order }.mapNotNull { section ->
        key(section.id) {
            val visible by section.isVisible.collectAsStateWithLifecycle(initialValue = true)
            if (visible) {
                SettingsHomeEntry(
                    title = stringResource(section.titleRes),
                    subtitle = stringResource(section.descriptionRes),
                    highlighted = section.highlighted,
                    onClick = section.onOpen
                )
            } else {
                null
            }
        }
    }
    SettingsHomePage(title = title, entries = entries, onBack = onBack)
}
