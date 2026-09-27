package com.wafflehq.lib.navigation.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

object SettingsHomePageTestTags {
    const val LIST = "settings_home_list"
    fun entry(title: String) = "settings_home_entry_$title"
}

@Immutable
data class SettingsHomeEntry(
    val title: String,
    val subtitle: String? = null,
    val highlighted: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun SettingsHomePage(
    title: String,
    entries: List<SettingsHomeEntry>,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backDescription: String? = null
) {
    SettingsScaffold(
        title = title,
        onBack = onBack,
        backDescription = backDescription,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag(SettingsHomePageTestTags.LIST)
        ) {
            item {
                SettingsSurfaceList {
                    entries.forEachIndexed { index, entry ->
                        SettingsNavRow(
                            title = entry.title,
                            subtitle = entry.subtitle,
                            onClick = entry.onClick,
                            highlighted = entry.highlighted,
                            modifier = Modifier.testTag(SettingsHomePageTestTags.entry(entry.title))
                        )
                        if (index < entries.lastIndex) SettingsRowDivider()
                    }
                }
            }
        }
    }
}
