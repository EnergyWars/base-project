package com.wafflehq.lib.uicore.menu

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wafflehq.lib.uicore.components.AppTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDropdownField(
    value: String,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    menuContent: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val open = expanded && enabled
    ExposedDropdownMenuBox(
        expanded = open,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        AppTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            isError = isError,
            singleLine = singleLine,
            label = label,
            leadingIcon = leadingIcon,
            supportingText = supportingText,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .then(fieldModifier),
        )
        AppExposedDropdownMenu(
            expanded = open,
            onDismissRequest = { expanded = false },
        ) {
            menuContent { expanded = false }
        }
    }
}

@Composable
fun <T> AppOptionDropdownField(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    emptyValue: String = "",
    optionIcon: (@Composable (T) -> Unit)? = null,
    optionModifier: (T) -> Modifier = { Modifier },
) {
    AppDropdownField(
        value = selected?.let { optionLabel(it) } ?: emptyValue,
        modifier = modifier,
        fieldModifier = fieldModifier,
        label = label,
        enabled = enabled,
        isError = isError,
        singleLine = singleLine,
        supportingText = supportingText,
    ) { dismiss ->
        options.forEach { option ->
            AppDropdownMenuItem(
                text = { Text(optionLabel(option)) },
                selected = option == selected,
                leadingIcon = if (optionIcon != null) {
                    { optionIcon(option) }
                } else null,
                modifier = optionModifier(option),
                onClick = {
                    onSelect(option)
                    dismiss()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    hasSuggestions: Boolean,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = true,
    supportingText: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    menuContent: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val open = expanded && hasSuggestions && enabled
    ExposedDropdownMenuBox(
        expanded = open,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        AppTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            enabled = enabled,
            isError = isError,
            singleLine = singleLine,
            label = label,
            supportingText = supportingText,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .then(fieldModifier),
        )
        AppExposedDropdownMenu(
            expanded = open,
            onDismissRequest = { expanded = false },
        ) {
            menuContent { expanded = false }
        }
    }
}
