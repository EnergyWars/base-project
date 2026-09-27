package com.wafflehq.lib.uicore.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppDividerTone { Standard, Subtle }

object AppDividerDefaults {
    val thickness: Dp = 1.dp
    const val SUBTLE_ALPHA: Float = AppCardDefaults.BORDER_ALPHA

    @Composable
    fun color(tone: AppDividerTone): Color = when (tone) {
        AppDividerTone.Standard -> MaterialTheme.colorScheme.outlineVariant
        AppDividerTone.Subtle -> MaterialTheme.colorScheme.outline.copy(alpha = SUBTLE_ALPHA)
    }
}

@Composable
fun AppHorizontalDivider(
    modifier: Modifier = Modifier,
    tone: AppDividerTone = AppDividerTone.Standard,
    thickness: Dp = AppDividerDefaults.thickness,
) {
    HorizontalDivider(modifier = modifier, thickness = thickness, color = AppDividerDefaults.color(tone))
}

@Composable
fun AppHorizontalDivider(
    color: Color,
    modifier: Modifier = Modifier,
    thickness: Dp = AppDividerDefaults.thickness,
) {
    HorizontalDivider(modifier = modifier, thickness = thickness, color = color)
}

@Composable
fun AppVerticalDivider(
    modifier: Modifier = Modifier,
    tone: AppDividerTone = AppDividerTone.Standard,
    thickness: Dp = AppDividerDefaults.thickness,
) {
    VerticalDivider(modifier = modifier, thickness = thickness, color = AppDividerDefaults.color(tone))
}

@Composable
fun AppVerticalDivider(
    color: Color,
    modifier: Modifier = Modifier,
    thickness: Dp = AppDividerDefaults.thickness,
) {
    VerticalDivider(modifier = modifier, thickness = thickness, color = color)
}
