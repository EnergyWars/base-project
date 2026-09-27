package com.wafflehq.lib.quickpicker

import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.R as UiCoreR
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
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
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.components.disabledPickerFieldColors
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.wafflehq.lib.uicore.components.AppBottomSheet
import com.wafflehq.lib.uicore.time.TimeFormats

private val fieldTimeFormatter: DateTimeFormatter = TimeFormats.HOUR_MINUTE

@Composable
fun ConfigurableTimeField(
    time: LocalTime,
    label: String,
    useQuickTimeInput: Boolean,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    AppTextField(
        value = time.format(fieldTimeFormatter),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier
            .testTag(QuickPickerTestTags.TIME_FIELD)
            .clickable { showPicker = true },
        enabled = false,
        colors = disabledPickerFieldColors(isEmpty = false)
    )

    if (showPicker) {
        QuickAwareTimePickerDialog(
            time = time,
            label = label,
            useQuickTimeInput = useQuickTimeInput,
            onDismiss = { showPicker = false },
            onConfirm = {
                onTimeChange(it)
                showPicker = false
            }
        )
    }
}

@Composable
fun QuickAwareTimePickerDialog(
    time: LocalTime,
    label: String,
    useQuickTimeInput: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    if (useQuickTimeInput) {
        QuickTimeFieldDialog(
            label = label,
            prefillTime = time,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    } else {
        SystemTimePickerDialog(
            initialTime = time,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTimeFieldDialog(
    label: String,
    prefillTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    AppBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        QuickTimeFieldSheetContent(
            label = label,
            prefillTime = prefillTime,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
fun QuickTimeFieldSheetContent(
    label: String,
    prefillTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    now: LocalTime = remember { LocalTime.now() }
) {
    var rawDigits by remember { mutableStateOf("") }
    var isPrefillMode by remember { mutableStateOf(true) }
    var userEdited by remember { mutableStateOf(false) }
    val relative = rememberRelativeTimeInputState(now)
    val timeFmt = TimeFormats.HOUR_MINUTE

    LaunchedEffect(rawDigits, relative.active) {
        if (!relative.active && userEdited && rawDigits.length == QuickTimeInputLogic.MAX_DIGITS) {
            val parsed = QuickTimeInputLogic.parseTime(rawDigits)
            if (parsed == null) rawDigits = "" else onConfirm(parsed)
        }
    }

    val displayDigits = if (isPrefillMode)
        "%02d%02d".format(prefillTime.hour, prefillTime.minute)
    else rawDigits

    val canConfirm = relative.active || isPrefillMode || QuickTimeInputLogic.parseTime(rawDigits) != null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg)
            .padding(bottom = AppSpacing.xxl)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = AppSpacing.md)
        )
        RelativeTimeToggleChip(
            state = relative,
            onToggle = {
                relative.toggle()?.let { result ->
                    isPrefillMode = false
                    userEdited = false
                    rawDigits = QuickTimeInputLogic.formatForInput(result)
                }
            },
            modifier = Modifier.padding(bottom = AppSpacing.md)
        )
        if (relative.active) {
            RelativeTimeDisplay(state = relative, timeFormat = timeFmt)
        } else {
            TimeDigitDisplay(displayDigits)
        }
        Spacer(Modifier.height(AppSpacing.lg))
        NumericKeypad(
            onDigit = { d ->
                if (relative.active) {
                    relative.appendDigit(d)
                } else if (isPrefillMode) {
                    isPrefillMode = false
                    userEdited = true
                    rawDigits = QuickTimeInputLogic.appendDigit("", d)
                } else {
                    userEdited = true
                    rawDigits = QuickTimeInputLogic.appendDigit(rawDigits, d)
                }
            },
            onBackspace = {
                if (relative.active) {
                    relative.backspace()
                } else if (isPrefillMode) {
                    isPrefillMode = false
                    userEdited = true
                    rawDigits = ""
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
            AppButton(
                text = stringResource(R.string.quickpicker_ok),
                role = AppButtonRole.Primary,
                enabled = canConfirm,
                onClick = {
                    if (!canConfirm) return@AppButton
                    val time = if (relative.active) relative.result
                    else if (isPrefillMode) prefillTime
                    else QuickTimeInputLogic.parseTime(rawDigits) ?: return@AppButton
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
