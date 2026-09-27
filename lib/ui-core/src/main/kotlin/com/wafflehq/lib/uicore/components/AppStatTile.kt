package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

enum class AppStatTileEmphasis { Compact, Prominent }

@Composable
fun AppStatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    emphasis: AppStatTileEmphasis = AppStatTileEmphasis.Compact,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    labelFirst: Boolean = false,
) {
    val valueStyle = when (emphasis) {
        AppStatTileEmphasis.Compact -> MaterialTheme.typography.titleMedium
        AppStatTileEmphasis.Prominent -> MaterialTheme.typography.headlineSmall
    }
    val labelStyle = when (emphasis) {
        AppStatTileEmphasis.Compact -> MaterialTheme.typography.labelSmall
        AppStatTileEmphasis.Prominent -> MaterialTheme.typography.labelMedium
    }
    val valueText: @Composable () -> Unit = {
        Text(
            text = value,
            style = valueStyle,
            fontWeight = if (emphasis == AppStatTileEmphasis.Prominent) FontWeight.Bold else null,
            color = valueColor,
        )
    }
    val labelText: @Composable () -> Unit = {
        Text(text = label, style = labelStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (labelFirst) {
            labelText()
            valueText()
        } else {
            valueText()
            labelText()
        }
    }
}
