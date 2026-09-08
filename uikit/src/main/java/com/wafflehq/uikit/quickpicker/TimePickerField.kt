package com.wafflehq.uikit.quickpicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import com.wafflehq.uikit.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val fieldTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun ConfigurableTimeField(
    time: LocalTime,
    label: String,
    useQuickTimeInput: Boolean,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = time.format(fieldTimeFormatter),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
        modifier = modifier
            .testTag(QuickPickerTestTags.TIME_FIELD)
            .clickable { showPicker = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
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
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAwareTimePickerDialog(
    time: LocalTime,
    label: String,
    useQuickTimeInput: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    if (useQuickTimeInput) {
        QuickTimeFieldDialog(
            label = label,
            prefillTime = time,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
    } else {
        val state = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = true,
        )
        TimePickerDialog(
            state = state,
            onDismiss = onDismiss,
            onConfirm = { onConfirm(LocalTime.of(state.hour, state.minute)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTimeFieldDialog(
    label: String,
    prefillTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        QuickTimeFieldSheetContent(
            label = label,
            prefillTime = prefillTime,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
    }
}

@Composable
fun QuickTimeFieldSheetContent(
    label: String,
    prefillTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    var rawDigits by remember { mutableStateOf("") }
    var isPrefillMode by remember { mutableStateOf(true) }

    LaunchedEffect(rawDigits) {
        if (!isPrefillMode && rawDigits.length == QuickTimeInputLogic.MAX_DIGITS) {
            val parsed = QuickTimeInputLogic.parseTime(rawDigits)
            if (parsed == null) rawDigits = "" else onConfirm(parsed)
        }
    }

    val displayDigits = if (isPrefillMode)
        "%02d%02d".format(prefillTime.hour, prefillTime.minute)
    else rawDigits

    val canConfirm = isPrefillMode || QuickTimeInputLogic.parseTime(rawDigits) != null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        TimeDigitDisplay(displayDigits)
        Spacer(Modifier.height(16.dp))
        NumericKeypad(
            onDigit = { d ->
                if (isPrefillMode) {
                    isPrefillMode = false
                    rawDigits = QuickTimeInputLogic.appendDigit("", d)
                } else {
                    rawDigits = QuickTimeInputLogic.appendDigit(rawDigits, d)
                }
            },
            onBackspace = {
                if (isPrefillMode) {
                    isPrefillMode = false
                    rawDigits = ""
                } else {
                    rawDigits = QuickTimeInputLogic.backspace(rawDigits)
                }
            },
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
            Button(
                onClick = {
                    if (!canConfirm) return@Button
                    val time = if (isPrefillMode) prefillTime
                    else QuickTimeInputLogic.parseTime(rawDigits) ?: return@Button
                    onConfirm(time)
                },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    state: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag(QuickPickerTestTags.CONFIRM_BUTTON),
            ) { Text(stringResource(R.string.quickpicker_ok)) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(QuickPickerTestTags.CANCEL_BUTTON),
            ) { Text(stringResource(R.string.quickpicker_cancel)) }
        },
    )
}
