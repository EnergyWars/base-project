package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.theme.AppRadius

private val RailHeight = 6.dp
private val ThumbWidth = 7.dp
private val ThumbHeight = 22.dp
private val TrackHeight = 24.dp
private val TickWidth = 2.dp
private val TickHeight = 11.dp
private val TickInset = 4.dp
private const val DisabledAlpha = 0.38f
private const val TickAlpha = 0.26f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.alpha(if (enabled) 1f else DisabledAlpha),
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        interactionSource = interaction,
        thumb = { AppSliderThumb(colors) },
        track = { state ->
            val span = valueRange.endInclusive - valueRange.start
            val fraction = if (span <= 0f) 0f else ((state.value - valueRange.start) / span).coerceIn(0f, 1f)
            AppSliderTrack(fraction = fraction, steps = steps, colors = colors)
        },
    )
}

@Composable
private fun AppSliderThumb(colors: ColorScheme) {
    val shape = RoundedCornerShape(AppRadius.pill)
    Box(
        modifier = Modifier
            .size(width = ThumbWidth, height = ThumbHeight)
            .shadow(2.dp, shape, clip = false)
            .background(colors.primary, shape)
            .border(2.dp, colors.surface, shape),
    )
}

@Composable
private fun AppSliderTrack(fraction: Float, steps: Int, colors: ColorScheme) {
    val rail = colors.surfaceVariant
    val fill = colors.primary
    val tick = colors.onSurface.copy(alpha = TickAlpha)
    Canvas(modifier = Modifier.fillMaxWidth().height(TrackHeight)) {
        val centerY = size.height / 2f
        val railPx = RailHeight.toPx()
        val radius = railPx / 2f
        val top = centerY - radius
        val corner = CornerRadius(radius, radius)
        drawRoundRect(color = rail, topLeft = Offset(0f, top), size = Size(size.width, railPx), cornerRadius = corner)
        val fillWidth = size.width * fraction
        if (fillWidth > 0f) {
            drawRoundRect(color = fill, topLeft = Offset(0f, top), size = Size(fillWidth, railPx), cornerRadius = corner)
        }
        if (steps > 0) {
            val count = steps + 2
            val tickW = TickWidth.toPx()
            val tickH = TickHeight.toPx()
            val tickCorner = CornerRadius(tickW / 2f, tickW / 2f)
            val inset = TickInset.toPx()
            val usable = size.width - inset * 2f
            for (i in 1 until count - 1) {
                val x = inset + usable * (i / (count - 1f))
                drawRoundRect(
                    color = tick,
                    topLeft = Offset(x - tickW / 2f, centerY - tickH / 2f),
                    size = Size(tickW, tickH),
                    cornerRadius = tickCorner,
                )
            }
        }
    }
}
