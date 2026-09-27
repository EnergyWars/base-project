package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.R
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.button.AppIconButtonVariant
import com.wafflehq.lib.uicore.theme.AppSpacing
import kotlinx.coroutines.delay

object AppStepperDefaults {
    const val HOLD_DELAY_MILLIS: Long = 400
    const val REPEAT_INTERVAL_MILLIS: Long = 80
    val buttonSize: Dp = 36.dp
}

@Composable
fun AppRepeatingIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    role: AppButtonRole = AppButtonRole.Neutral,
    variant: AppIconButtonVariant = AppIconButtonVariant.Standard,
    iconSize: Dp? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val currentOnClick by rememberUpdatedState(onClick)
    LaunchedEffect(isPressed, enabled) {
        if (isPressed && enabled) {
            delay(AppStepperDefaults.HOLD_DELAY_MILLIS)
            while (true) {
                currentOnClick()
                delay(AppStepperDefaults.REPEAT_INTERVAL_MILLIS)
            }
        }
    }
    AppIconButton(
        icon = icon,
        contentDescription = contentDescription,
        role = role,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        variant = variant,
        iconSize = iconSize,
        interactionSource = interactionSource,
    )
}

@Composable
fun AppStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    step: Int = 1,
    repeatOnHold: Boolean = false,
    valueText: String = value.toString(),
    valueStyle: TextStyle = MaterialTheme.typography.titleMedium,
    valueModifier: Modifier = Modifier,
    minValueWidth: Dp = 0.dp,
    buttonRole: AppButtonRole = AppButtonRole.Neutral,
    buttonVariant: AppIconButtonVariant = AppIconButtonVariant.Standard,
    buttonSize: Dp = AppStepperDefaults.buttonSize,
    decreaseContentDescription: String = stringResource(R.string.uicore_stepper_decrease),
    increaseContentDescription: String = stringResource(R.string.uicore_stepper_increase),
) {
    val canDecrease = enabled && value - step >= range.first
    val canIncrease = enabled && value + step <= range.last
    val decrease = { onValueChange((value - step).coerceIn(range)) }
    val increase = { onValueChange((value + step).coerceIn(range)) }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        StepperButton(Icons.Default.Remove, decreaseContentDescription, decrease, canDecrease, repeatOnHold, buttonRole, buttonVariant, buttonSize)
        Text(
            text = valueText,
            style = valueStyle,
            color = if (enabled) LocalContentColor.current else MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_ALPHA),
            textAlign = TextAlign.Center,
            modifier = valueModifier.widthIn(min = minValueWidth),
        )
        StepperButton(Icons.Default.Add, increaseContentDescription, increase, canIncrease, repeatOnHold, buttonRole, buttonVariant, buttonSize)
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean,
    repeatOnHold: Boolean,
    role: AppButtonRole,
    variant: AppIconButtonVariant,
    size: Dp,
) {
    if (repeatOnHold) {
        AppRepeatingIconButton(icon, contentDescription, onClick, Modifier.size(size), enabled, role, variant)
    } else {
        AppIconButton(icon, contentDescription, role, onClick, Modifier.size(size), variant, enabled)
    }
}

private const val DISABLED_ALPHA = 0.38f
