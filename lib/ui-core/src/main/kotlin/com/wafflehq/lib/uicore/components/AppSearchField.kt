package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.uicore.R
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.theme.AppRadius
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun AppSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    clearContentDescription: String = stringResource(R.string.uicore_search_clear),
    accentColor: Color? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val leadingTint = accentColor ?: MaterialTheme.colorScheme.onSurfaceVariant
    AppTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = leadingTint) },
        trailingIcon = {
            if (query.isNotBlank()) {
                AppIconButton(
                    icon = Icons.Default.Clear,
                    contentDescription = clearContentDescription,
                    role = AppButtonRole.Neutral,
                    onClick = { onQueryChange("") },
                )
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        shape = if (accentColor != null) RoundedCornerShape(AppRadius.pill) else AppTextFieldDefaults.shape,
        colors = if (accentColor != null) {
            AppTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                focusedLabelColor = accentColor,
                cursorColor = accentColor,
            )
        } else {
            AppTextFieldDefaults.colors()
        },
    )
}
