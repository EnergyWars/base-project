package com.wafflehq.lib.quickpicker

import com.wafflehq.lib.uicore.R as UiCoreR
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.button.AppIconButtonVariant
import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import com.wafflehq.lib.uicore.components.AppBottomSheet
import com.wafflehq.lib.uicore.gesture.dismissSheetOnDownwardSwipe
import com.wafflehq.lib.uicore.components.AppFilterChip
import com.wafflehq.lib.uicore.components.AppAssistChip

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
    onConfirm: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    AppBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)
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
            onConfirm = onConfirm
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
    onConfirm: (LocalDate) -> Unit
) {
    var raw by remember { mutableStateOf("") }
    var relativeMode by remember { mutableStateOf(false) }
    var relativeSign by remember { mutableStateOf(1L) }
    var relativeBase by remember { mutableStateOf(today) }
    var expandedWeek by remember { mutableStateOf<ExpandedWeek?>(null) }
    val locale = Locale.getDefault()
    val dateFmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

    val absoluteParsed = QuickDateInputLogic.parseDate(raw, today, format)
    val parsed = if (relativeMode) QuickDateInputLogic.relativeDate(relativeBase, raw, relativeSign)
    else absoluteParsed
    val canConfirm = parsed != null
    val toggleRelativeMode = {
        if (relativeMode) {
            raw = QuickDateInputLogic.formatForInput(
                QuickDateInputLogic.relativeDate(relativeBase, raw, relativeSign),
                format
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
            modifier = modifier.testTag(QuickPickerTestTags.DATE_PREVIEW)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .dismissSheetOnDownwardSwipe(onSwipeDown = onNotNow ?: onDismiss)
            .padding(horizontal = AppSpacing.lg)
            .padding(top = AppSpacing.md, bottom = AppSpacing.xxl)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = AppSpacing.md)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            AppButton(
                text = stringResource(R.string.quickpicker_last_week),
                role = AppButtonRole.Secondary,
                variant = if (expandedWeek == ExpandedWeek.LAST) AppButtonVariant.Outlined else AppButtonVariant.Tonal,
                onClick = {
                    expandedWeek = if (expandedWeek == ExpandedWeek.LAST) null else ExpandedWeek.LAST
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag(QuickPickerTestTags.LAST_WEEK_BUTTON)
            )
            AppButton(
                text = stringResource(R.string.quickpicker_today),
                role = AppButtonRole.Secondary,
                variant = if (!relativeMode && parsed == today) AppButtonVariant.Outlined else AppButtonVariant.Tonal,
                onClick = { selectDay(today) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag(QuickPickerTestTags.TODAY_BUTTON)
            )
            AppButton(
                text = stringResource(R.string.quickpicker_next_week),
                role = AppButtonRole.Secondary,
                variant = if (expandedWeek == ExpandedWeek.NEXT) AppButtonVariant.Outlined else AppButtonVariant.Tonal,
                onClick = {
                    expandedWeek = if (expandedWeek == ExpandedWeek.NEXT) null else ExpandedWeek.NEXT
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag(QuickPickerTestTags.NEXT_WEEK_BUTTON)
            )
        }
        DayChipRow(
            days = when (expandedWeek) {
                ExpandedWeek.LAST -> QuickDateInputLogic.lastWeek(today)
                ExpandedWeek.NEXT -> QuickDateInputLogic.nextWeek(today)
                null -> QuickDateInputLogic.lastWeek(today)
            },
            locale = locale,
            visible = expandedWeek != null,
            onDayClick = selectDay
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            modifier = Modifier.padding(bottom = AppSpacing.md)
        ) {
            if (initialDate != null) {
                AppAssistChip(
                    onClick = restoreInitialDate,
                    label = { Text(stringResource(R.string.quickpicker_restore_date)) },
                    modifier = Modifier.testTag(QuickPickerTestTags.RESTORE_BUTTON)
                )
            }
            AppFilterChip(
                selected = relativeMode,
                onClick = toggleRelativeMode,
                label = { Text(stringResource(R.string.quickpicker_relative_toggle)) },
                modifier = Modifier.testTag(QuickPickerTestTags.RELATIVE_TOGGLE_BUTTON)
            )
        }
        if (relativeMode) {
            datePreview(Modifier.padding(bottom = AppSpacing.xs))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                AppIconButton(
                    icon = if (relativeSign < 0) Icons.Filled.Remove else Icons.Filled.Add,
                    contentDescription = stringResource(R.string.quickpicker_toggle_sign),
                    role = AppButtonRole.Primary,
                    variant = AppIconButtonVariant.Tonal,
                    shape = CircleShape,
                    onClick = { relativeSign = -relativeSign },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(QuickPickerTestTags.TODAY_SIGN_TOGGLE)
                )
                Text(
                    text = raw.ifEmpty { "0" },
                    style = MaterialTheme.typography.displaySmall,
                    color = if (raw.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag(QuickPickerTestTags.DATE_DISPLAY)
                )
                Text(
                    text = stringResource(R.string.quickpicker_days_unit),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (absoluteParsed != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                AppIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.quickpicker_previous_day),
                    role = AppButtonRole.Neutral,
                    onClick = { shiftByDays(-1) },
                    modifier = Modifier.testTag(QuickPickerTestTags.PREVIOUS_DAY_ARROW),
                )
                Text(
                    text = raw,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag(QuickPickerTestTags.DATE_DISPLAY)
                )
                AppIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.quickpicker_next_day),
                    role = AppButtonRole.Neutral,
                    onClick = { shiftByDays(1) },
                    modifier = Modifier.testTag(QuickPickerTestTags.NEXT_DAY_ARROW),
                )
            }
            datePreview(Modifier.padding(top = AppSpacing.xs))
        } else {
            Text(
                text = raw.ifEmpty { "−" },
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.testTag(QuickPickerTestTags.DATE_DISPLAY)
            )
            datePreview(Modifier.padding(top = AppSpacing.xs))
        }
        Spacer(Modifier.height(AppSpacing.lg))
        NumericKeypad(
            onDigit = { d ->
                raw = if (relativeMode) QuickDateInputLogic.appendRelativeDigit(raw, d)
                else QuickDateInputLogic.appendDigit(raw, d, format)
            },
            onBackspace = { raw = QuickDateInputLogic.backspace(raw) },
            leadingKey = {
                AppButton(
                    text = QuickDateInputLogic.separatorFor(format).toString(),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Tonal,
                    textStyle = MaterialTheme.typography.titleLarge,
                    enabled = !relativeMode,
                    onClick = { if (!relativeMode) raw = QuickDateInputLogic.appendSeparator(raw, format) },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .testTag(QuickPickerTestTags.SEPARATOR),
                )
            }
        )
        Spacer(Modifier.height(AppSpacing.lg))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            AppButton(
                text = stringResource(UiCoreR.string.uicore_cancel),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Outlined,
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag(QuickPickerTestTags.CANCEL_BUTTON),
            )
            if (onNotNow != null) {
                AppButton(
                    text = stringResource(R.string.quickpicker_not_now),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Outlined,
                    onClick = onNotNow,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag(QuickPickerTestTags.NOT_NOW_BUTTON),
                )
            }
            AppButton(
                text = stringResource(R.string.quickpicker_ok),
                role = AppButtonRole.Primary,
                enabled = canConfirm,
                onClick = { parsed?.let(onConfirm) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag(QuickPickerTestTags.CONFIRM_BUTTON),
            )
        }
    }
}

@Composable
private fun DayChipRow(
    days: List<LocalDate>,
    locale: Locale,
    visible: Boolean,
    onDayClick: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppSpacing.md)
            .alpha(if (visible) 1f else 0f),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        days.forEach { day ->
            AppButton(
                role = AppButtonRole.Secondary,
                variant = AppButtonVariant.Tonal,
                enabled = visible,
                onClick = { onDayClick(day) },
                contentPadding = PaddingValues(vertical = AppSpacing.sm, horizontal = AppSpacing.xs),
                modifier = Modifier
                    .weight(1f)
                    .let { if (visible) it.testTag(QuickPickerTestTags.dayChip(day)) else it }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = day.dayOfMonth.toString(),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
