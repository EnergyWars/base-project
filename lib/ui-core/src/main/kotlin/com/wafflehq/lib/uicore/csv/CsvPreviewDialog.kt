package com.wafflehq.lib.uicore.csv

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.horizontalScroll
import com.wafflehq.lib.uicore.components.AppDialogDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wafflehq.lib.uicore.R
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.scaffold.AppScaffold

@Composable
fun CsvPreviewDialog(
    header: List<String>,
    rows: List<List<String>>,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = AppDialogDefaults.fullWidthProperties
    ) {
        AppScaffold(
            title = stringResource(R.string.uicore_csv_preview_title),
            onBack = onDismiss,
            backDescription = stringResource(R.string.uicore_csv_preview_close),
            actions = {
                AppIconButton(
                    icon = Icons.Default.Share,
                    contentDescription = stringResource(R.string.uicore_csv_preview_share),
                    role = AppButtonRole.Neutral,
                    onClick = onShare,
                )
            },
        ) { padding ->
            if (rows.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(AppSpacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.uicore_csv_preview_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val horizontalScrollState = rememberScrollState()
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    CsvTableRow(header, isHeader = true, modifier = Modifier.horizontalScroll(horizontalScrollState))
                    AppHorizontalDivider()
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(rows) { index, row ->
                            CsvTableRow(row, isHeader = false, modifier = Modifier.horizontalScroll(horizontalScrollState))
                            if (index != rows.lastIndex) AppHorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CsvTableRow(cells: List<String>, isHeader: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(vertical = AppSpacing.sm)) {
        cells.forEach { cell ->
            Text(
                text = cell,
                style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
                fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.widthIn(min = 96.dp).padding(horizontal = AppSpacing.sm)
            )
        }
    }
}
