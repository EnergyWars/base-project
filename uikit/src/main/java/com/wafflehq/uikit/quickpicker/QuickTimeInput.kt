package com.wafflehq.uikit.quickpicker

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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

@Composable
fun QuickTimeDisplayField(
    time: LocalTime,
    label: String,
    timeFmt: DateTimeFormatter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = time.format(timeFmt),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
        modifier = modifier
            .testTag(QuickPickerTestTags.TIME_FIELD)
            .clickable { onClick() },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTimeInputDialog(
    label: String,
    prefillNow: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    onAllDay: (() -> Unit)? = null,
) {
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
        QuickTimeInputSheetContent(
            label = label,
            prefillNow = prefillNow,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            onAllDay = onAllDay,
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
) {
    val now = remember { LocalTime.now() }
    val nowTimeFmt = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val nowDigits = remember { "%02d%02d".format(now.hour, now.minute) }
    var rawDigits by remember { mutableStateOf(if (prefillNow) nowDigits else "") }
    var userEdited by remember { mutableStateOf(!prefillNow) }

    LaunchedEffect(rawDigits) {
        if (userEdited && rawDigits.length == QuickTimeInputLogic.MAX_DIGITS) {
            val parsed = QuickTimeInputLogic.parseTime(rawDigits)
            if (parsed == null) rawDigits = "" else onConfirm(parsed)
        }
    }

    val canConfirm = QuickTimeInputLogic.parseTime(rawDigits) != null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .dismissSheetOnDownwardSwipe(onSwipeDown = onDismiss)
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 32.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Surface(
            onClick = {
                userEdited = false
                rawDigits = nowDigits
            },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .testTag(QuickPickerTestTags.NOW_CHIP),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.quickpicker_now_with, now.format(nowTimeFmt)),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        TimeDigitDisplay(rawDigits)
        Spacer(Modifier.height(16.dp))
        NumericKeypad(
            onDigit = { d -> userEdited = true; rawDigits = QuickTimeInputLogic.appendDigit(rawDigits, d) },
            onBackspace = { userEdited = true; rawDigits = QuickTimeInputLogic.backspace(rawDigits) },
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
            if (onAllDay != null) {
                Button(
                    onClick = onAllDay,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag(QuickPickerTestTags.ALL_DAY_BUTTON),
                ) {
                    Text(stringResource(R.string.quickpicker_all_day))
                }
            }
            Button(
                onClick = {
                    val time = QuickTimeInputLogic.parseTime(rawDigits) ?: return@Button
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

@Composable
fun TimeDigitDisplay(rawDigits: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(QuickPickerTestTags.TIME_DIGIT_DISPLAY),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
    ) {
        DigitSlot(rawDigits.getOrNull(0))
        DigitSlot(rawDigits.getOrNull(1))
        Text(
            text = ":",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp),
        )
        DigitSlot(rawDigits.getOrNull(2))
        DigitSlot(rawDigits.getOrNull(3))
    }
}

@Composable
private fun DigitSlot(char: Char?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp),
    ) {
        Text(
            text = char?.toString() ?: "−",
            style = MaterialTheme.typography.displayMedium,
            color = if (char != null) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
        )
        HorizontalDivider(
            thickness = 2.dp,
            color = if (char != null) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.width(36.dp),
        )
    }
}

@Composable
fun NumericKeypad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
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
            Spacer(Modifier.weight(1f))
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
