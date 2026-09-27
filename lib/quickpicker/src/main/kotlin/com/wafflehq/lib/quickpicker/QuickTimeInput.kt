package com.wafflehq.lib.quickpicker

import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.R as UiCoreR
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.components.disabledPickerFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppBottomSheet
import com.wafflehq.lib.uicore.gesture.dismissSheetOnDownwardSwipe
import com.wafflehq.lib.uicore.time.TimeFormats

@Composable
fun QuickTimeDisplayField(
    time: LocalTime,
    label: String,
    timeFmt: DateTimeFormatter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppTextField(
        value = time.format(timeFmt),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier
            .testTag(QuickPickerTestTags.TIME_FIELD)
            .clickable { onClick() },
        enabled = false,
        colors = disabledPickerFieldColors(isEmpty = false)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTimeInputDialog(
    label: String,
    prefillNow: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    onAllDay: (() -> Unit)? = null
) {
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
        QuickTimeInputSheetContent(
            label = label,
            prefillNow = prefillNow,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            onAllDay = onAllDay
        )
    }
}

@Composable
fun QuickTimeInputSheetContent(
    label: String,
    prefillNow: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    onAllDay: (() -> Unit)? = null,
    now: LocalTime = remember { LocalTime.now() }
) {
    val nowTimeFmt = TimeFormats.HOUR_MINUTE
    val nowDigits = QuickTimeInputLogic.formatForInput(now)
    var rawDigits by remember { mutableStateOf(if (prefillNow) nowDigits else "") }
    var userEdited by remember { mutableStateOf(!prefillNow) }
    val relative = rememberRelativeTimeInputState(now)

    LaunchedEffect(rawDigits, relative.active) {
        if (!relative.active && userEdited && rawDigits.length == QuickTimeInputLogic.MAX_DIGITS) {
            val parsed = QuickTimeInputLogic.parseTime(rawDigits)
            if (parsed == null) rawDigits = "" else onConfirm(parsed)
        }
    }

    val absoluteParsed = QuickTimeInputLogic.parseTime(rawDigits)
    val canConfirm = relative.active || absoluteParsed != null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .dismissSheetOnDownwardSwipe(onSwipeDown = onDismiss)
            .padding(horizontal = AppSpacing.lg)
            .padding(top = AppSpacing.md, bottom = AppSpacing.xxl)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = AppSpacing.md)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            modifier = Modifier.padding(bottom = AppSpacing.md)
        ) {
            AppButton(
                text = stringResource(R.string.quickpicker_now_with, now.format(nowTimeFmt)),
                role = AppButtonRole.Secondary,
                variant = AppButtonVariant.Tonal,
                leadingIcon = Icons.Filled.Schedule,
                onClick = {
                    relative.deactivate()
                    userEdited = false
                    rawDigits = nowDigits
                },
                modifier = Modifier.testTag(QuickPickerTestTags.NOW_CHIP)
            )
            RelativeTimeToggleChip(
                state = relative,
                onToggle = {
                    relative.toggle()?.let { result ->
                        userEdited = false
                        rawDigits = QuickTimeInputLogic.formatForInput(result)
                    }
                }
            )
        }
        if (relative.active) {
            RelativeTimeDisplay(state = relative, timeFormat = nowTimeFmt)
        } else {
            TimeDigitDisplay(rawDigits)
        }
        Spacer(Modifier.height(AppSpacing.lg))
        NumericKeypad(
            onDigit = { d ->
                if (relative.active) {
                    relative.appendDigit(d)
                } else {
                    userEdited = true
                    rawDigits = QuickTimeInputLogic.appendDigit(rawDigits, d)
                }
            },
            onBackspace = {
                if (relative.active) {
                    relative.backspace()
                } else {
                    userEdited = true
                    rawDigits = QuickTimeInputLogic.backspace(rawDigits)
                }
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
            if (onAllDay != null) {
                AppButton(
                    text = stringResource(R.string.quickpicker_all_day),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Outlined,
                    onClick = onAllDay,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag(QuickPickerTestTags.ALL_DAY_BUTTON),
                )
            }
            AppButton(
                text = stringResource(R.string.quickpicker_ok),
                role = AppButtonRole.Primary,
                enabled = canConfirm,
                onClick = {
                    val time = if (relative.active) relative.result else absoluteParsed ?: return@AppButton
                    onConfirm(time)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag(QuickPickerTestTags.CONFIRM_BUTTON),
            )
        }
    }
}

@Composable
fun TimeDigitDisplay(rawDigits: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(QuickPickerTestTags.TIME_DIGIT_DISPLAY),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        DigitSlot(rawDigits.getOrNull(0))
        DigitSlot(rawDigits.getOrNull(1))
        Text(
            text = ":",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = AppSpacing.sm)
        )
        DigitSlot(rawDigits.getOrNull(2))
        DigitSlot(rawDigits.getOrNull(3))
    }
}

@Composable
private fun DigitSlot(char: Char?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = AppSpacing.xs)
    ) {
        Text(
            text = char?.toString() ?: "−",
            style = MaterialTheme.typography.displayMedium,
            color = if (char != null) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        )
        AppHorizontalDivider(
            thickness = 2.dp,
            color = if (char != null) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.width(36.dp)
        )
    }
}
