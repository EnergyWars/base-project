package com.wafflehq.lib.quickpicker

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.components.disabledPickerFieldColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun QuickDateField(
    date: LocalDate,
    label: String,
    useQuickDateInput: Boolean,
    dateDisplayFormat: DateDisplayFormat,
    accent: Color,
    showWeekday: Boolean,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()) }

    AppTextField(
        value = date.withOptionalWeekdayPrefix(date.format(formatter), showWeekday),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier
            .testTag(QuickPickerTestTags.DATE_FIELD)
            .clickable { showDatePicker = true },
        enabled = false,
        colors = disabledPickerFieldColors(isEmpty = false, borderColor = accent.copy(alpha = 0.5f))
    )

    if (showDatePicker) {
        QuickAwareDatePickerDialog(
            date = date,
            label = label,
            useQuickDateInput = useQuickDateInput,
            dateDisplayFormat = dateDisplayFormat,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                onDateChange(it)
                showDatePicker = false
            }
        )
    }
}
