package com.wafflehq.lib.uicore.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private const val EMPTY_DISABLED_FIELD_ALPHA = 0.5f

@Composable
fun emptyAwareTextFieldColors(isEmpty: Boolean): TextFieldColors {
    val base = AppTextFieldDefaults.colors()
    if (!isEmpty) return base
    return base.copy(
        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = EMPTY_DISABLED_FIELD_ALPHA),
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = EMPTY_DISABLED_FIELD_ALPHA),
    )
}

@Composable
fun disabledPickerFieldColors(
    isEmpty: Boolean,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    trailingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    supportingTextColor: Color? = null,
): TextFieldColors {
    val alpha = if (isEmpty) EMPTY_DISABLED_FIELD_ALPHA else 1f
    return AppTextFieldDefaults.colors(
        disabledTextColor = textColor.copy(alpha = alpha),
        disabledBorderColor = borderColor.copy(alpha = alpha * borderColor.alpha),
        disabledLabelColor = labelColor.copy(alpha = alpha),
        disabledTrailingIconColor = trailingIconColor.copy(alpha = alpha),
        disabledSupportingTextColor = (supportingTextColor ?: trailingIconColor).copy(alpha = alpha),
    )
}
