package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.theme.AppSpacing

data class IconPickerOption(
    val key: String,
    val icon: ImageVector,
    val label: String
)

object IconPickerDefaults {
    val cellSize: Dp = 52.dp
    val gridHeight: Dp = 280.dp
    val selectedBorderWidth: Dp = 1.5.dp
    const val idleAlpha: Float = 0.5f
}

object IconPickerTestTags {
    const val GRID = "icon_picker_grid"
    fun cell(key: String) = "icon_picker_cell_$key"
}

@Composable
fun IconPickerGrid(
    options: List<IconPickerOption>,
    selectedKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    cellSize: Dp = IconPickerDefaults.cellSize,
    gridHeight: Dp = IconPickerDefaults.gridHeight
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(cellSize),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        modifier = modifier.height(gridHeight).testTag(IconPickerTestTags.GRID)
    ) {
        items(options, key = { it.key }) { option ->
            IconPickerCell(
                option = option,
                selected = option.key == selectedKey,
                onSelect = { onSelect(option.key) },
                cellSize = cellSize
            )
        }
    }
}

@Composable
private fun IconPickerCell(
    option: IconPickerOption,
    selected: Boolean,
    onSelect: () -> Unit,
    cellSize: Dp
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(AppRadius.card)
    Box(
        modifier = Modifier
            .size(cellSize)
            .clip(shape)
            .background(
                if (selected) colors.primaryContainer else colors.surfaceVariant.copy(alpha = IconPickerDefaults.idleAlpha)
            )
            .border(
                if (selected) IconPickerDefaults.selectedBorderWidth else 0.dp,
                if (selected) colors.primary else Color.Transparent,
                shape
            )
            .selectable(selected = selected, onClick = onSelect)
            .testTag(IconPickerTestTags.cell(option.key)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            option.icon,
            contentDescription = option.label,
            tint = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
        )
    }
}
