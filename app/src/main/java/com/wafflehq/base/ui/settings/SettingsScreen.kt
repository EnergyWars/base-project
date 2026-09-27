package com.wafflehq.base.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.base.R
import com.wafflehq.lib.navigation.settings.SettingsHomeEntry
import com.wafflehq.lib.navigation.settings.SettingsHomePage

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenDisplay: () -> Unit,
    onOpenFeatureFiles: () -> Unit,
    onOpenColors: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val featureFilesCount by viewModel.featureFilesCount.collectAsStateWithLifecycle()
    val entries = buildList {
        add(
            SettingsHomeEntry(
                title = stringResource(R.string.settings_display_title),
                subtitle = stringResource(R.string.settings_display_sub),
                onClick = onOpenDisplay
            )
        )
        add(
            SettingsHomeEntry(
                title = stringResource(R.string.settings_row_colors),
                subtitle = stringResource(R.string.settings_row_colors_sub),
                onClick = onOpenColors
            )
        )
        if (featureFilesCount > 0) {
            add(
                SettingsHomeEntry(
                    title = stringResource(R.string.settings_row_features),
                    subtitle = stringResource(R.string.settings_feature_files_count, featureFilesCount),
                    onClick = onOpenFeatureFiles
                )
            )
        }
    }
    SettingsHomePage(
        title = stringResource(R.string.settings_title),
        entries = entries,
        onBack = onBack,
        backDescription = stringResource(R.string.label_back)
    )
}
