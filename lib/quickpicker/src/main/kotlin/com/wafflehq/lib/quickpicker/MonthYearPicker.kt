package com.wafflehq.lib.quickpicker

import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.R as UiCoreR
import androidx.compose.foundation.layout.Arrangement
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.components.AppDialogDefaults
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.button.AppIconButton
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthYearPickerDialog(
    initialYear: Int,
    initialMonth: Int,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int) -> Unit
) {
    var year by remember { mutableIntStateOf(initialYear) }
    var month by remember { mutableIntStateOf(initialMonth) }

    AppDialog(
        onDismissRequest = onDismiss,
        properties = AppDialogDefaults.properties,
        title = null,
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppIconButton(
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.quickpicker_previous_year),
                        role = AppButtonRole.Neutral,
                        onClick = { year-- },
                        modifier = Modifier.size(48.dp).testTag(QuickPickerTestTags.PREVIOUS_YEAR_ARROW),
                    )
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.testTag(QuickPickerTestTags.YEAR_LABEL),
                    )
                    AppIconButton(
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.quickpicker_next_year),
                        role = AppButtonRole.Neutral,
                        onClick = { year++ },
                        modifier = Modifier.size(48.dp).testTag(QuickPickerTestTags.NEXT_YEAR_ARROW),
                    )
                }
                Spacer(Modifier.height(AppSpacing.sm))
                (1..12).chunked(3).forEach { rowMonths ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        rowMonths.forEach { m ->
                            val label = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            AppButton(
                                text = label,
                                role = AppButtonRole.Primary,
                                variant = if (m == month) AppButtonVariant.Outlined else AppButtonVariant.Tonal,
                                onClick = { month = m },
                                modifier = Modifier.weight(1f).testTag(QuickPickerTestTags.monthButton(m)),
                                contentPadding = PaddingValues(4.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.quickpicker_ok),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Text,
                onClick = { onConfirm(year, month) },
                modifier = Modifier.testTag(QuickPickerTestTags.CONFIRM_BUTTON),
            )
        },
        dismissButton = {
            AppButton(
                text = stringResource(UiCoreR.string.uicore_cancel),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                onClick = onDismiss,
                modifier = Modifier.testTag(QuickPickerTestTags.CANCEL_BUTTON),
            )
        }
    )
}
