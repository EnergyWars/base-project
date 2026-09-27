package com.wafflehq.lib.quickpicker

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.components.AppTextFieldDefaults
import com.wafflehq.lib.uicore.components.disabledPickerFieldColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ConfigurableDateField(
    date: LocalDate?,
    label: String,
    useQuickDateInput: Boolean,
    dateDisplayFormat: DateDisplayFormat,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    showWeekday: Boolean = LocalQuickPickerShowWeekday.current,
    formatter: DateTimeFormatter? = null,
    emptyText: String = "",
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = false,
    shape: Shape? = null,
    colors: TextFieldColors? = null,
    labelContent: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    testTag: String? = null,
    pickerDate: LocalDate? = date
) {
    var showPicker by remember { mutableStateOf(false) }
    val defaultFormatter = remember(dateDisplayFormat) {
        DateTimeFormatter.ofPattern(dateDisplayFormat.datePattern())
    }
    val resolvedFormatter = formatter ?: defaultFormatter
    val scheme = MaterialTheme.colorScheme

    AppTextField(
        value = date?.let { it.withOptionalWeekdayPrefix(it.format(resolvedFormatter), showWeekday) } ?: emptyText,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        isError = isError,
        singleLine = singleLine,
        label = { if (labelContent != null) labelContent() else Text(label) },
        supportingText = supportingText?.let { { Text(it) } },
        trailingIcon = trailingIcon,
        shape = shape ?: AppTextFieldDefaults.shape,
        modifier = modifier
            .let { if (testTag != null) it.testTag(testTag) else it }
            .clickable { showPicker = true },
        colors = colors ?: disabledPickerFieldColors(
            isEmpty = false,
            borderColor = if (isError) scheme.error else scheme.outline,
            supportingTextColor = if (isError) scheme.error else scheme.onSurfaceVariant
        )
    )

    if (showPicker) {
        QuickAwareDatePickerDialog(
            date = pickerDate,
            label = label,
            useQuickDateInput = useQuickDateInput,
            dateDisplayFormat = dateDisplayFormat,
            onDismiss = { showPicker = false },
            onConfirm = {
                onDateChange(it)
                showPicker = false
            }
        )
    }
}

@Composable
fun QuickAwareDatePickerDialog(
    date: LocalDate?,
    label: String,
    useQuickDateInput: Boolean,
    dateDisplayFormat: DateDisplayFormat,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    if (useQuickDateInput) {
        QuickDateInputDialog(
            label = label,
            format = dateDisplayFormat,
            initialDate = date,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    } else {
        SystemDatePickerDialog(
            initialDate = date,
            onDismiss = onDismiss,
            onConfirm = {
                onConfirm(it)
                onDismiss()
            },
        )
    }
}
