package com.wafflehq.base.ui.features

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.wafflehq.base.R
import com.wafflehq.lib.navigation.settings.SettingsScaffold
import com.wafflehq.lib.uicore.theme.AppSpacing

@Composable
fun FeatureFileDetailScreen(
    onBack: () -> Unit,
    viewModel: FeatureFileDetailViewModel = hiltViewModel(),
) {
    val featureFile = viewModel.featureFile

    SettingsScaffold(
        title = featureFile?.title ?: stringResource(R.string.feature_files_title),
        onBack = onBack,
        backDescription = stringResource(R.string.label_back),
    ) { padding ->
        Text(
            text = featureFile?.content ?: stringResource(R.string.feature_files_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppSpacing.lg)
                .verticalScroll(rememberScrollState()),
        )
    }
}
