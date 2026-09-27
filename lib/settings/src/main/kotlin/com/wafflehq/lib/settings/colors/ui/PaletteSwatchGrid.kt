package com.wafflehq.lib.settings.colors.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.settings.colors.ColorRamp
import com.wafflehq.lib.settings.colors.ColorRampSteps
import com.wafflehq.lib.settings.colors.ColorRampTable

object PaletteSwatchGridTestTag {
    fun swatch(ramp: ColorRamp, step: Int) = "palette_swatch_${ramp.name}_$step"
}

@Composable
fun PaletteSwatchGrid(
    onSwatchSelected: (ColorRamp, Int) -> Unit,
    modifier: Modifier = Modifier,
    baseRampOverrides: Map<ColorRamp, Color> = emptyMap()
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val swatches = ColorRampTable.allSwatches(baseRampOverrides)
    LazyVerticalGrid(
        columns = GridCells.Fixed(ColorRampSteps.size),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.height(240.dp)
    ) {
        items(swatches, key = { (ramp, step, _) -> "${ramp.name}_$step" }) { (ramp, step, color) ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, outlineColor.copy(alpha = 0.4f), CircleShape)
                    .testTag(PaletteSwatchGridTestTag.swatch(ramp, step))
                    .clickable { onSwatchSelected(ramp, step) }
            )
        }
    }
}
