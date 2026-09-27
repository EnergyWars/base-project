package com.wafflehq.lib.settings.colors.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.colors.ColorSettingsController
import com.wafflehq.lib.uicore.R as UiCoreR
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppBottomSheet
import com.wafflehq.lib.uicore.components.AppCheckbox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorThemeImportDialog(
    controller: ColorSettingsController,
    onDismiss: () -> Unit
) {
    val candidates by controller.importCandidates.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    val selectedCount = candidates.count { it.selected }
    val allSelected = candidates.isNotEmpty() && selectedCount == candidates.size

    AppBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
        ) {
            Text(
                text = stringResource(R.string.appsettings_color_theme_import_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.appsettings_color_theme_import_count, selectedCount, candidates.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = AppSpacing.xs, bottom = AppSpacing.sm)
            )

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AppCheckbox(
                    checked = allSelected,
                    onCheckedChange = { controller.setAllImportCandidatesSelected(!allSelected) }
                )
                Text(
                    text = stringResource(R.string.appsettings_color_theme_import_select_all),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            AppHorizontalDivider()

            LazyColumn(modifier = Modifier.height(280.dp)) {
                itemsIndexed(candidates) { index, candidate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { controller.toggleImportCandidate(index) }
                            .padding(vertical = AppSpacing.xs)
                            .testTag(ColorThemeTestTags.importEntry(index)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppCheckbox(checked = candidate.selected, onCheckedChange = null)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = candidate.entry.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = pluralStringResource(R.plurals.appsettings_color_theme_token_count, candidate.entry.tokenCount, candidate.entry.tokenCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    AppHorizontalDivider()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm),
                horizontalArrangement = Arrangement.End
            ) {
                AppButton(
                    text = stringResource(UiCoreR.string.uicore_cancel),
                    role = AppButtonRole.Neutral,
                    variant = AppButtonVariant.Text,
                    onClick = onDismiss,
                )
                AppButton(
                    text = stringResource(R.string.appsettings_color_theme_import_confirm),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    enabled = selectedCount > 0,
                    onClick = {
                        controller.confirmImport()
                        onDismiss()
                    },
                    modifier = Modifier.testTag(ColorThemeTestTags.IMPORT_CONFIRM),
                )
            }
        }
    }
}
