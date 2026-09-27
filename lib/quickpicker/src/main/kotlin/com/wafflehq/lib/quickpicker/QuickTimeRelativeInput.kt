package com.wafflehq.lib.quickpicker

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.button.AppIconButtonVariant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.wafflehq.lib.uicore.components.AppFilterChip

@Stable
class RelativeTimeInputState(val base: LocalTime) {
    var active by mutableStateOf(false)
        private set
    var amount by mutableStateOf("")
        private set
    var sign by mutableStateOf(1L)
        private set
    var unit by mutableStateOf(RelativeTimeUnit.MINUTES)
        private set

    val result: LocalTime
        get() = QuickTimeInputLogic.relativeTime(base, amount, sign, unit)

    fun toggle(): LocalTime? {
        val leaving = active
        val leftResult = if (leaving) result else null
        amount = ""
        sign = 1L
        unit = RelativeTimeUnit.MINUTES
        active = !active
        return leftResult
    }

    fun deactivate() {
        active = false
    }

    fun flipSign() {
        sign = -sign
    }

    fun selectUnit(newUnit: RelativeTimeUnit) {
        unit = newUnit
    }

    fun appendDigit(digit: Int) {
        amount = QuickTimeInputLogic.appendRelativeDigit(amount, digit)
    }

    fun backspace() {
        amount = QuickTimeInputLogic.backspace(amount)
    }
}

@Composable
fun rememberRelativeTimeInputState(base: LocalTime): RelativeTimeInputState =
    remember(base) { RelativeTimeInputState(base) }

@Composable
fun RelativeTimeToggleChip(
    state: RelativeTimeInputState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppFilterChip(
        selected = state.active,
        onClick = onToggle,
        label = { Text(stringResource(R.string.quickpicker_relative_time_toggle)) },
        modifier = modifier.testTag(QuickPickerTestTags.TIME_RELATIVE_TOGGLE_BUTTON)
    )
}

@Composable
fun RelativeTimeDisplay(
    state: RelativeTimeInputState,
    timeFormat: DateTimeFormatter,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.quickpicker_relative_result, state.result.format(timeFormat)),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(bottom = AppSpacing.xs)
                .testTag(QuickPickerTestTags.TIME_RELATIVE_PREVIEW)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            AppIconButton(
                icon = if (state.sign < 0) Icons.Filled.Remove else Icons.Filled.Add,
                contentDescription = stringResource(R.string.quickpicker_toggle_sign),
                role = AppButtonRole.Primary,
                variant = AppIconButtonVariant.Tonal,
                shape = CircleShape,
                onClick = state::flipSign,
                modifier = Modifier
                    .size(48.dp)
                    .testTag(QuickPickerTestTags.TIME_RELATIVE_SIGN)
            )
            Text(
                text = state.amount.ifEmpty { "0" },
                style = MaterialTheme.typography.displaySmall,
                color = if (state.amount.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag(QuickPickerTestTags.TIME_RELATIVE_AMOUNT)
            )
            AppFilterChip(
                selected = state.unit == RelativeTimeUnit.MINUTES,
                onClick = { state.selectUnit(RelativeTimeUnit.MINUTES) },
                label = { Text(stringResource(R.string.quickpicker_minutes_unit)) },
                modifier = Modifier.testTag(QuickPickerTestTags.TIME_UNIT_MINUTES)
            )
            AppFilterChip(
                selected = state.unit == RelativeTimeUnit.HOURS,
                onClick = { state.selectUnit(RelativeTimeUnit.HOURS) },
                label = { Text(stringResource(R.string.quickpicker_hours_unit)) },
                modifier = Modifier.testTag(QuickPickerTestTags.TIME_UNIT_HOURS)
            )
        }
    }
}
