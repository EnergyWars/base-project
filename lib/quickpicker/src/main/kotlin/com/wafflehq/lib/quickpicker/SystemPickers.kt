package com.wafflehq.lib.quickpicker

import com.wafflehq.lib.uicore.R as UiCoreR
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppDialog
import com.wafflehq.lib.uicore.components.AppDialogDefaults
import java.time.LocalDate
import java.time.LocalTime

private const val PICKER_MILLIS_PER_DAY = 86_400_000L

internal fun LocalDate.toPickerMillis(): Long = toEpochDay() * PICKER_MILLIS_PER_DAY

internal fun Long.fromPickerMillis(): LocalDate = LocalDate.ofEpochDay(Math.floorDiv(this, PICKER_MILLIS_PER_DAY))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemDatePickerDialog(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    isDateSelectable: (LocalDate) -> Boolean = { true },
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = (initialDate ?: LocalDate.now()).toPickerMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                isDateSelectable(utcTimeMillis.fromPickerMillis())
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        properties = AppDialogDefaults.fullWidthProperties,
        shape = AppDialogDefaults.shape,
        tonalElevation = AppDialogDefaults.tonalElevation,
        colors = DatePickerDefaults.colors(containerColor = AppDialogDefaults.containerColor),
        confirmButton = {
            AppButton(
                text = stringResource(R.string.quickpicker_ok),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Text,
                enabled = state.selectedDateMillis != null,
                onClick = { state.selectedDateMillis?.let { onConfirm(it.fromPickerMillis()) } },
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
        },
    ) { DatePicker(state = state) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemTimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    is24Hour: Boolean = true,
) {
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = is24Hour,
    )
    AppDialog(
        onDismissRequest = onDismiss,
        properties = AppDialogDefaults.properties,
        title = null,
        text = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            AppButton(
                text = stringResource(R.string.quickpicker_ok),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Text,
                onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) },
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
        },
    )
}
