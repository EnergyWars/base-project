package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalReservedBottomInset = compositionLocalOf { 0.dp }

@Composable
fun Modifier.reportReservedBottomInset(onReservedInsetChanged: (Dp) -> Unit): Modifier {
    val density = LocalDensity.current
    val safeDrawingBottomPx = WindowInsets.safeDrawing.getBottom(density)
    return this.onGloballyPositioned { coordinates ->
        val root = coordinates.findRootCoordinates()
        val reservedPx = reservedBottomInsetPx(
            root.size.height,
            root.localPositionOf(coordinates, Offset.Zero).y,
            safeDrawingBottomPx,
        )
        onReservedInsetChanged(with(density) { reservedPx.toDp() })
    }
}

internal fun reservedBottomInsetPx(rootHeightPx: Int, topInRootPx: Float, safeDrawingBottomPx: Int): Float =
    ((rootHeightPx - safeDrawingBottomPx) - topInRootPx).coerceAtLeast(0f)
