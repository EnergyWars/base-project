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
import com.wafflehq.base.ui.components.SettingsListContent
import com.wafflehq.base.ui.components.SettingsScaffold

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenDisplay: () -> Unit,
    onOpenFeatureFiles: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val featureFilesCount by viewModel.featureFilesCount.collectAsStateWithLifecycle()

    SettingsScaffold(
        title = stringResource(R.string.settings_title),
        onBack = onBack,
        backDescription = stringResource(R.string.label_back),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsListContent(
                showFeatures = featureFilesCount > 0,
                featuresLabel = stringResource(R.string.settings_row_features),
                displayLabel = stringResource(R.string.settings_display_title),
                displaySubtitle = stringResource(R.string.settings_display_sub),
                onOpenFeatures = onOpenFeatureFiles,
                onOpenDisplay = onOpenDisplay,
            )
        }
    }
}
