package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import com.wafflehq.lib.uicore.theme.AppSpacing
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

object AppWeekdayChipRowTestTags {
    fun chip(day: DayOfWeek) = "weekday_chip_${day.name}"
}

fun isWeekdayToggleBlocked(selected: Set<DayOfWeek>, day: DayOfWeek, minSelected: Int): Boolean =
    day in selected && selected.size <= minSelected

@Composable
fun AppWeekdayChipRow(
    selected: Set<DayOfWeek>,
    onToggle: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
    minSelected: Int = 1,
    onMinReached: (() -> Unit)? = null,
    chipSize: Dp? = null,
) {
    val rowModifier = if (chipSize == null) modifier.fillMaxWidth() else modifier
    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        DayOfWeek.entries.forEach { day ->
            val chipModifier = if (chipSize == null) Modifier.weight(1f) else Modifier.size(chipSize)
            AppFilterChip(
                selected = day in selected,
                onClick = {
                    if (isWeekdayToggleBlocked(selected, day, minSelected)) onMinReached?.invoke()
                    else onToggle(day)
                },
                label = {
                    Text(
                        text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                modifier = chipModifier.testTag(AppWeekdayChipRowTestTags.chip(day))
            )
        }
    }
}
