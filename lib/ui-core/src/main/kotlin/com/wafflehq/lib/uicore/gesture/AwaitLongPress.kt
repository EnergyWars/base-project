package com.wafflehq.lib.uicore.gesture

import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange

suspend fun AwaitPointerEventScope.awaitLongPress(
    down: PointerInputChange,
    timeoutMillis: Long = viewConfiguration.longPressTimeoutMillis,
    touchSlop: Float = viewConfiguration.touchSlop
): Boolean {
    val interrupted = withTimeoutOrNull(timeoutMillis) {
        var holding = true
        while (holding) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id }
            holding = change != null && change.pressed &&
                (change.position - down.position).getDistance() <= touchSlop
        }
        true
    }
    return interrupted == null
}
