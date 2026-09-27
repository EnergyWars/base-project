package com.wafflehq.lib.uicore.button

import android.view.MotionEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

fun isObscuredTouch(motionEventFlags: Int): Boolean =
    motionEventFlags and (MotionEvent.FLAG_WINDOW_IS_OBSCURED or MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED) != 0

fun Modifier.filterObscuredTouches(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val motionEvent = event.motionEvent ?: continue
            if (isObscuredTouch(motionEvent.flags)) {
                event.changes.forEach { it.consume() }
            }
        }
    }
}
