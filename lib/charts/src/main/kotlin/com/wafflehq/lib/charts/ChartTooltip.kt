package com.wafflehq.lib.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

private val CHART_TOOLTIP_GAP: Dp = 6.dp

internal data class ChartTooltipAnchor(
    val index: Int,
    val localX: Float,
    val avoidTopY: Float,
    val avoidBottomY: Float
)

private class ChartTooltipPositionProvider(
    private val anchor: ChartTooltipAnchor,
    private val gapPx: Float
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val pointX = anchorBounds.left + anchor.localX
        val avoidTop = anchorBounds.top + anchor.avoidTopY
        val avoidBottom = anchorBounds.top + anchor.avoidBottomY

        val minX = anchorBounds.left
        val maxX = (anchorBounds.right - popupContentSize.width).coerceAtLeast(minX)
        val x = (pointX - popupContentSize.width / 2f).roundToInt().coerceIn(minX, maxX)

        val above = avoidTop - gapPx - popupContentSize.height
        val y = if (above >= 0f) above else avoidBottom + gapPx
        val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)

        return IntOffset(x, y.roundToInt().coerceIn(0, maxY))
    }
}

@Composable
internal fun ChartTooltipPopup(
    anchor: ChartTooltipAnchor,
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    style: TextStyle,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val gapPx = with(density) { CHART_TOOLTIP_GAP.toPx() }
    Popup(
        popupPositionProvider = ChartTooltipPositionProvider(anchor, gapPx),
        properties = PopupProperties(focusable = false, dismissOnClickOutside = true),
        onDismissRequest = onDismiss
    ) {
        Text(
            text = text,
            style = style.copy(color = contentColor, fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
