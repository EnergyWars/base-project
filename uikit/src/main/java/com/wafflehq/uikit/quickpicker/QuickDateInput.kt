package com.wafflehq.uikit.quickpicker

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.uikit.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

val LocalQuickPickerShowWeekday = staticCompositionLocalOf { true }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickDateInputDialog(
    label: String,
    format: DateDisplayFormat = DateDisplayFormat.GERMAN,
    showWeekday: Boolean = LocalQuickPickerShowWeekday.current,
    initialDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onNotNow: (() -> Unit)? = null,
    onConfirm: (LocalDate) -> Unit,
) {
    val today = remember { LocalDate.now() }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden },
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
    ) {
        BackHandler(onBack = onDismiss)
        QuickDateInputSheetContent(
            label = label,
            format = format,
            showWeekday = showWeekday,
            today = today,
            initialDate = initialDate,
            onDismiss = onDismiss,
            onNotNow = onNotNow,
            onConfirm = onConfirm,
        )
    }
}

private enum class ExpandedWeek { LAST, NEXT }

@Composable
fun QuickDateInputSheetContent(
    label: String,
    format: DateDisplayFormat,
    showWeekday: Boolean,
    today: LocalDate,
    initialDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onNotNow: (() -> Unit)?,
    onConfirm: (LocalDate) -> Unit,
) {
    var raw by remember { mutableStateOf("") }
    var relativeMode by remember { mutableStateOf(false) }
    var relativeSign by remember { mutableStateOf(1L) }
    var relativeBase by remember { mutableStateOf(today) }
    var expandedWeek by remember { mutableStateOf<ExpandedWeek?>(null) }
    val locale = LocalConfiguration.current.locales[0]
    val dateFmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

    val absoluteParsed = QuickDateInputLogic.parseDate(raw, today, format)
    val parsed = if (relativeMode) QuickDateInputLogic.relativeDate(relativeBase, raw, relativeSign)
    else absoluteParsed
    val canConfirm = parsed != null
    val toggleRelativeMode = {
        if (relativeMode) {
            raw = QuickDateInputLogic.formatForInput(
                QuickDateInputLogic.relativeDate(relativeBase, raw, relativeSign),
                format,
            )
        } else {
            relativeBase = absoluteParsed ?: initialDate ?: today
            raw = ""
        }
        relativeMode = !relativeMode
        relativeSign = 1L
    }
    val restoreInitialDate: () -> Unit = {
        initialDate?.let {
            relativeMode = false
            raw = QuickDateInputLogic.formatForInput(it, format)
        }
    }
    val shiftByDays: (Long) -> Unit = { delta ->
        absoluteParsed?.let { raw = QuickDateInputLogic.formatForInput(it.plusDays(delta), format) }
    }
    val selectDay: (LocalDate) -> Unit = { day ->
        relativeMode = false
        raw = QuickDateInputLogic.formatForInput(day, format)
        expandedWeek = null
    }
    val previewText = parsed?.let { it.withOptionalWeekdayPrefix(it.format(dateFmt), showWeekday, locale) } ?: " "
    val datePreview: @Composable (Modifier) -> Unit = { modifier ->
        Text(
            text = previewText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.testTag(QuickPickerTestTags.DATE_PREVIEW),
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .dismissSheetOnDownwardSwipe(onSwipeDown = onNotNow ?: onDismiss)
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 32.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = expandedWeek == ExpandedWeek.LAST,
                onClick = {
                    expandedWeek = if (expandedWeek == ExpandedWeek.LAST) null else ExpandedWeek.LAST
                },
                label = { Text(stringResource(R.string.quickpicker_last_week)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag(QuickPickerTestTags.LAST_WEEK_BUTTON),
            )
            Surface(
                onClick = { selectDay(today) },
                shape = MaterialTheme.shapes.small,
                color = if (!relativeMode && parsed == today) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (!relativeMode && parsed == today) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .weight(1f)
                    .testTag(QuickPickerTestTags.TODAY_BUTTON),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(stringResource(R.string.quickpicker_today), style = MaterialTheme.typography.labelLarge)
                }
            }
            FilterChip(
                selected = expandedWeek == ExpandedWeek.NEXT,
                onClick = {
                    expandedWeek = if (expandedWeek == ExpandedWeek.NEXT) null else ExpandedWeek.NEXT
                },
                label = { Text(stringResource(R.string.quickpicker_next_week)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag(QuickPickerTestTags.NEXT_WEEK_BUTTON),
            )
        }
        if (expandedWeek != null) {
            DayChipRow(
                days = if (expandedWeek == ExpandedWeek.LAST) QuickDateInputLogic.lastWeek(today)
                else QuickDateInputLogic.nextWeek(today),
                locale = locale,
                onDayClick = selectDay,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp),
        ) {
            if (initialDate != null) {
                AssistChip(
                    onClick = restoreInitialDate,
                    label = { Text(stringResource(R.string.quickpicker_restore_date)) },
                    modifier = Modifier.testTag(QuickPickerTestTags.RESTORE_BUTTON),
                )
            }
            FilterChip(
                selected = relativeMode,
                onClick = toggleRelativeMode,
                label = { Text(stringResource(R.string.quickpicker_relative_toggle)) },
                modifier = Modifier.testTag(QuickPickerTestTags.RELATIVE_TOGGLE_BUTTON),
            )
        }
        if (relativeMode) {
            datePreview(Modifier.padding(bottom = 4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    onClick = { relativeSign = -relativeSign },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(QuickPickerTestTags.TODAY_SIGN_TOGGLE),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (relativeSign < 0) "−" else "+",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
                Text(
                    text = raw.ifEmpty { "0" },
                    style = MaterialTheme.typography.displaySmall,
                    color = if (raw.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag(QuickPickerTestTags.DATE_DISPLAY),
                )
                Text(
                    text = stringResource(R.string.quickpicker_days_unit),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (absoluteParsed != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    onClick = { shiftByDays(-1) },
                    modifier = Modifier.testTag(QuickPickerTestTags.PREVIOUS_DAY_ARROW),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.quickpicker_previous_day),
                    )
                }
                Text(
                    text = raw,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag(QuickPickerTestTags.DATE_DISPLAY),
                )
                IconButton(
                    onClick = { shiftByDays(1) },
                    modifier = Modifier.testTag(QuickPickerTestTags.NEXT_DAY_ARROW),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.quickpicker_next_day),
                    )
                }
            }
            datePreview(Modifier.padding(top = 4.dp))
        } else {
            Text(
                text = raw.ifEmpty { "−" },
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.testTag(QuickPickerTestTags.DATE_DISPLAY),
            )
            datePreview(Modifier.padding(top = 4.dp))
        }
        Spacer(Modifier.height(16.dp))
        DateNumericKeypad(
            separator = QuickDateInputLogic.separatorFor(format),
            separatorEnabled = !relativeMode,
            onDigit = { d ->
                raw = if (relativeMode) QuickDateInputLogic.appendRelativeDigit(raw, d)
                else QuickDateInputLogic.appendDigit(raw, d, format)
            },
            onSeparator = { if (!relativeMode) raw = QuickDateInputLogic.appendSeparator(raw, format) },
            onBackspace = { raw = QuickDateInputLogic.backspace(raw) },
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag(QuickPickerTestTags.CANCEL_BUTTON),
            ) {
                Text(stringResource(R.string.quickpicker_cancel))
            }
            if (onNotNow != null) {
                OutlinedButton(
                    onClick = onNotNow,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag(QuickPickerTestTags.NOT_NOW_BUTTON),
                ) {
                    Text(stringResource(R.string.quickpicker_not_now))
                }
            }
            Button(
                onClick = { parsed?.let(onConfirm) },
                enabled = canConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag(QuickPickerTestTags.CONFIRM_BUTTON),
            ) {
                Text(stringResource(R.string.quickpicker_ok))
            }
        }
    }
}

@Composable
private fun DayChipRow(
    days: List<LocalDate>,
    locale: Locale,
    onDayClick: (LocalDate) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        days.forEach { day ->
            Surface(
                onClick = { onDayClick(day) },
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .weight(1f)
                    .testTag(QuickPickerTestTags.dayChip(day)),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                ) {
                    Text(
                        text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = day.dayOfMonth.toString(),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
fun DateNumericKeypad(
    onDigit: (Int) -> Unit,
    onSeparator: () -> Unit,
    onBackspace: () -> Unit,
    separator: Char = '.',
    separatorEnabled: Boolean = true,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { digit ->
                    FilledTonalButton(
                        onClick = { onDigit(digit) },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag(QuickPickerTestTags.digit(digit)),
                    ) {
                        Text(digit.toString(), style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalButton(
                onClick = onSeparator,
                enabled = separatorEnabled,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag(QuickPickerTestTags.SEPARATOR),
            ) {
                Text(separator.toString(), style = MaterialTheme.typography.titleLarge)
            }
            FilledTonalButton(
                onClick = { onDigit(0) },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag(QuickPickerTestTags.digit(0)),
            ) {
                Text("0", style = MaterialTheme.typography.titleLarge)
            }
            FilledTonalButton(
                onClick = onBackspace,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag(QuickPickerTestTags.BACKSPACE),
            ) {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null)
            }
        }
    }
}
