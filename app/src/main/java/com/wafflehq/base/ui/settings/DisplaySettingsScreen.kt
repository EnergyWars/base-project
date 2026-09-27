package com.wafflehq.base.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.base.R
import com.wafflehq.base.data.model.ThemeMode
import com.wafflehq.lib.navigation.settings.SettingsDropdownField
import com.wafflehq.lib.navigation.settings.SettingsGroup
import com.wafflehq.lib.navigation.settings.SettingsScaffold

@Composable
fun DisplaySettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val modes = ThemeMode.entries
    val labels = modes.map { stringResource(it.labelRes) }

    SettingsScaffold(
        title = stringResource(R.string.settings_display_title),
        onBack = onBack,
        backDescription = stringResource(R.string.label_back),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsGroup(label = stringResource(R.string.settings_group_general)) {
                SettingsDropdownField(
                    label = stringResource(R.string.settings_design_label),
                    value = labels[modes.indexOf(themeMode)],
                    options = labels,
                    selectedIndex = modes.indexOf(themeMode),
                    onSelect = { index -> viewModel.onThemeModeSelected(modes[index]) },
                )
            }
        }
    }
}
