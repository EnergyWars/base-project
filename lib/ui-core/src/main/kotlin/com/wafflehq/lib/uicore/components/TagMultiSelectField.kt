package com.wafflehq.lib.uicore.components

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppIconButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagMultiSelectField(
    label: String,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    onAdd: (String) -> Unit,
    selectedValues: List<String>,
    onRemove: (String) -> Unit,
    allKnownValues: List<String>,
    suggestedValues: List<String>,
    addContentDescription: String,
    removeContentDescription: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppTextField(
                value = inputValue,
                onValueChange = onInputValueChange,
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            AppIconButton(
                icon = Icons.Default.Add,
                contentDescription = addContentDescription,
                role = AppButtonRole.Primary,
                onClick = { if (inputValue.isNotBlank()) onAdd(inputValue) },
            )
        }

        val completions = remember(inputValue, allKnownValues, selectedValues) {
            if (inputValue.isBlank()) {
                emptyList()
            } else {
                allKnownValues.filter { candidate ->
                    candidate.contains(inputValue, ignoreCase = true) &&
                        selectedValues.none { it.equals(candidate, ignoreCase = true) }
                }.take(5)
            }
        }
        if (completions.isNotEmpty()) {
            AppCard(
                variant = AppCardVariant.Filled,
            ) {
                Column {
                    completions.forEach { completion ->
                        Text(
                            text = completion,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAdd(completion) }
                                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                        )
                    }
                }
            }
        }

        val remainingSuggestions = remember(suggestedValues, selectedValues) {
            suggestedValues.filter { suggestion -> selectedValues.none { it.equals(suggestion, ignoreCase = true) } }
        }
        if (inputValue.isBlank() && remainingSuggestions.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                remainingSuggestions.forEach { suggestion ->
                    AppSuggestionChip(
                        onClick = { onAdd(suggestion) },
                        label = { Text(suggestion) }
                    )
                }
            }
        }

        if (selectedValues.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                selectedValues.forEach { value ->
                    AppInputChip(
                        selected = false,
                        onClick = { onRemove(value) },
                        label = { Text(value) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = removeContentDescription,
                                modifier = Modifier.width(16.dp).height(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}
