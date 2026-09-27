package com.wafflehq.base.ui.features

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.base.R
import com.wafflehq.lib.navigation.settings.SettingsScaffold
import com.wafflehq.lib.navigation.settings.SettingsSwitchRow
import com.wafflehq.lib.uicore.components.AppCheckbox
import com.wafflehq.lib.uicore.theme.AppSpacing

@Composable
fun FeatureFilesListScreen(
    onBack: () -> Unit,
    onOpenFile: (String) -> Unit,
    viewModel: FeatureFilesViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val showHidden by viewModel.showHidden.collectAsStateWithLifecycle()

    SettingsScaffold(
        title = stringResource(R.string.feature_files_title),
        onBack = onBack,
        backDescription = stringResource(R.string.label_back),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppSpacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            SettingsSwitchRow(
                title = stringResource(R.string.feature_files_show_hidden),
                subtitle = stringResource(
                    R.string.settings_feature_files_count,
                    viewModel.totalFileCount,
                ),
                checked = showHidden,
                onCheckedChange = viewModel::onShowHiddenChange,
            )
            when {
                viewModel.totalFileCount == 0 -> Text(
                    text = stringResource(R.string.feature_files_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                items.isEmpty() -> Text(
                    text = stringResource(R.string.feature_files_all_hidden),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenFile(item.fileName) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        AppCheckbox(
                            checked = item.checked,
                            onCheckedChange = { checked -> viewModel.onCheckedChange(item.fileName, checked) },
                        )
                        Text(text = item.title, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
