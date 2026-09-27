package com.wafflehq.base.ui.library.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.lib.quickpicker.ConfigurableDateField
import com.wafflehq.lib.quickpicker.ConfigurableTimeField
import com.wafflehq.lib.quickpicker.DateDisplayFormat
import com.wafflehq.lib.quickpicker.MonthYearPickerDialog
import com.wafflehq.lib.quickpicker.QuickDateInputLogic
import com.wafflehq.lib.quickpicker.QuickTimeInputLogic
import com.wafflehq.lib.quickpicker.YearPickerDialog
import com.wafflehq.lib.quickpicker.datePattern
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppPillSelector
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.time.TimeFormats
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

internal object QuickPickerDemoLogic {

    fun parseDate(raw: String, reference: LocalDate, format: DateDisplayFormat): LocalDate? =
        QuickDateInputLogic.parseDate(raw.trim(), reference, format)

    fun parseTime(raw: String): LocalTime? = QuickTimeInputLogic.parseTime(raw.filter { it.isDigit() })

    fun formatDate(date: LocalDate, format: DateDisplayFormat): String =
        date.format(DateTimeFormatter.ofPattern(format.datePattern()))

    fun monthLabel(year: Int, month: Int, locale: Locale = Locale.getDefault()): String =
        "${Month.of(month.coerceIn(1, 12)).getDisplayName(TextStyle.FULL, locale)} $year"
}

internal object QuickPickerDemoTags {
    const val QUICK_SWITCH = "libex_quickpicker_quick_switch"
    const val MONTH_BUTTON = "libex_quickpicker_month_button"
    const val YEAR_BUTTON = "libex_quickpicker_year_button"
    const val MONTH_RESULT = "libex_quickpicker_month_result"
    const val YEAR_RESULT = "libex_quickpicker_year_result"
    const val DATE_INPUT = "libex_quickpicker_date_input"
    const val DATE_PARSED = "libex_quickpicker_date_parsed"
    const val TIME_INPUT = "libex_quickpicker_time_input"
    const val TIME_PARSED = "libex_quickpicker_time_parsed"
    const val DATE_VALUE = "libex_quickpicker_date_value"
    fun format(format: DateDisplayFormat) = "libex_quickpicker_format_${format.name}"
}

@Composable
internal fun QuickPickerDemo(today: LocalDate = LocalDate.now(), initialTime: LocalTime = LocalTime.of(9, 30)) {
    var useQuick by remember { mutableStateOf(true) }
    var format by remember { mutableStateOf(DateDisplayFormat.GERMAN) }
    var date by remember { mutableStateOf<LocalDate?>(today) }
    var time by remember { mutableStateOf(initialTime) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var pickedMonth by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var pickedYear by remember { mutableStateOf<Int?>(null) }
    var dateInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }
    val invalid = stringResource(R.string.libex_quickpicker_invalid)
    val none = stringResource(R.string.libex_value_none)
    val parsedDate = QuickPickerDemoLogic.parseDate(dateInput, today, format)
    val parsedTime = QuickPickerDemoLogic.parseTime(timeInput)

    DemoSection(
        id = "quickpicker",
        titleRes = R.string.libex_quickpicker_title,
        descriptionRes = R.string.libex_quickpicker_desc,
        moduleRes = R.string.libex_module_quickpicker,
    ) {
        DemoSwitchRow(
            label = stringResource(R.string.libex_quickpicker_quick_input),
            checked = useQuick,
            onCheckedChange = { useQuick = it },
            tag = QuickPickerDemoTags.QUICK_SWITCH,
        )
        AppPillSelector(
            options = DateDisplayFormat.entries,
            selected = format,
            onSelect = { format = it },
            label = {
                stringResource(
                    if (it == DateDisplayFormat.GERMAN) R.string.libex_quickpicker_format_german else R.string.libex_quickpicker_format_international
                )
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = { QuickPickerDemoTags.format(it) },
        )
        ConfigurableDateField(
            date = date,
            label = stringResource(R.string.libex_quickpicker_date_label),
            useQuickDateInput = useQuick,
            dateDisplayFormat = format,
            onDateChange = { date = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            testTag = QuickPickerDemoTags.DATE_VALUE,
        )
        ConfigurableTimeField(
            time = time,
            label = stringResource(R.string.libex_quickpicker_time_label),
            useQuickTimeInput = useQuick,
            onTimeChange = { time = it },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppButton(
                text = stringResource(R.string.libex_quickpicker_pick_month),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                onClick = { showMonthPicker = true },
                modifier = Modifier.testTag(QuickPickerDemoTags.MONTH_BUTTON),
            )
            AppButton(
                text = stringResource(R.string.libex_quickpicker_pick_year),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Outlined,
                onClick = { showYearPicker = true },
                modifier = Modifier.testTag(QuickPickerDemoTags.YEAR_BUTTON),
            )
        }
        pickedMonth?.let { (year, month) ->
            DemoBodyText(
                text = stringResource(R.string.libex_quickpicker_month_result, QuickPickerDemoLogic.monthLabel(year, month)),
                modifier = Modifier.testTag(QuickPickerDemoTags.MONTH_RESULT),
            )
        }
        pickedYear?.let {
            DemoBodyText(
                text = stringResource(R.string.libex_quickpicker_year_result, it),
                modifier = Modifier.testTag(QuickPickerDemoTags.YEAR_RESULT),
            )
        }
        AppHorizontalDivider()
        DemoLabelText(stringResource(R.string.libex_quickpicker_parser_heading))
        AppTextField(
            value = dateInput,
            onValueChange = { dateInput = it },
            label = { Text(stringResource(R.string.libex_quickpicker_parser_date)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(QuickPickerDemoTags.DATE_INPUT),
        )
        DemoKeyValue(
            label = stringResource(R.string.libex_quickpicker_parsed),
            value = if (dateInput.isBlank()) none else parsedDate?.let { QuickPickerDemoLogic.formatDate(it, format) } ?: invalid,
            modifier = Modifier.testTag(QuickPickerDemoTags.DATE_PARSED),
        )
        AppTextField(
            value = timeInput,
            onValueChange = { timeInput = it },
            label = { Text(stringResource(R.string.libex_quickpicker_parser_time)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(QuickPickerDemoTags.TIME_INPUT),
        )
        DemoKeyValue(
            label = stringResource(R.string.libex_quickpicker_parsed),
            value = if (timeInput.isBlank()) none else parsedTime?.format(TimeFormats.HOUR_MINUTE) ?: invalid,
            modifier = Modifier.testTag(QuickPickerDemoTags.TIME_PARSED),
        )
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialYear = (date ?: today).year,
            initialMonth = (date ?: today).monthValue,
            onDismiss = { showMonthPicker = false },
            onConfirm = { year, month ->
                pickedMonth = year to month
                showMonthPicker = false
            },
        )
    }
    if (showYearPicker) {
        YearPickerDialog(
            currentYear = (date ?: today).year,
            onDismiss = { showYearPicker = false },
            onYearSelected = {
                pickedYear = it
                showYearPicker = false
            },
        )
    }
}
