package com.wafflehq.lib.uicore.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.wafflehq.lib.uicore.testing.exposeTestTagsInDebugBuilds
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.theme.AppSpacing

object AppAnchoredPopupMenuDefaults {
    val gap: Dp = AppSpacing.sm
}

internal class StartOfAnchorPositionProvider(private val gapPx: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            anchorBounds.left - popupContentSize.width - gapPx
        } else {
            anchorBounds.right + gapPx
        }
        val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)
        return IntOffset(x, anchorBounds.top.coerceIn(0, maxY))
    }
}

@Composable
fun AppAnchoredPopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    gap: Dp = AppAnchoredPopupMenuDefaults.gap,
    anchor: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box {
        if (expanded) {
            val gapPx = with(LocalDensity.current) { gap.roundToPx() }
            val positionProvider = remember(gapPx) { StartOfAnchorPositionProvider(gapPx) }
            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = onDismissRequest,
                properties = PopupProperties(focusable = true),
            ) {
                Surface(
                    shape = RoundedCornerShape(AppRadius.card),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = AppMenuTonalElevation,
                    shadowElevation = AppMenuShadowElevation,
                    border = BorderStroke(AppMenuBorderWidth, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.exposeTestTagsInDebugBuilds(),
                ) {
                    Column(
                        modifier = modifier
                            .width(IntrinsicSize.Max)
                            .padding(vertical = AppSpacing.xs),
                        content = content,
                    )
                }
            }
        }
        anchor()
    }
}
