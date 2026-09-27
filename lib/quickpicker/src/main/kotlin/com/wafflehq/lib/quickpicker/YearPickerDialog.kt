package com.wafflehq.lib.quickpicker

import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.R as UiCoreR
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.components.AppHorizontalDivider

internal object YearPickerDefaults {
    const val YEARS_BEFORE = 5
    const val YEARS_AFTER = 10
    const val INITIAL_SCROLL_INDEX = 4

    fun years(currentYear: Int): List<Int> =
        (currentYear - YEARS_BEFORE..currentYear + YEARS_AFTER).toList()
}

@Composable
fun YearPickerDialog(
    currentYear: Int,
    onDismiss: () -> Unit,
    onYearSelected: (Int) -> Unit,
) {
    val years = remember(currentYear) { YearPickerDefaults.years(currentYear) }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = YearPickerDefaults.INITIAL_SCROLL_INDEX,
    )
    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.quickpicker_year_picker_title)) },
        text = {
            LazyColumn(state = listState, modifier = Modifier.heightIn(max = 300.dp)) {
                items(years) { year ->
                    val isSelected = year == currentYear
                    Text(
                        text = year.toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onYearSelected(year) }
                            .padding(vertical = AppSpacing.md),
                        textAlign = TextAlign.Center,
                        style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                    if (year != years.last()) AppHorizontalDivider()
                }
            }
        },
        confirmButton = {
            AppButton(
                text = stringResource(UiCoreR.string.uicore_cancel),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                onClick = onDismiss,
            )
        },
    )
}
