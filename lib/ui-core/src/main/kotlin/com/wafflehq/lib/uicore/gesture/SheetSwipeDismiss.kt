package com.wafflehq.lib.uicore.gesture

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.dp

private const val SWIPE_DISMISS_DRAG_DISTANCE_DP = 140
private const val SWIPE_DISMISS_FLING_DISTANCE_DP = 56
private const val SWIPE_DISMISS_FLING_VELOCITY_DP_PER_S = 900

fun Modifier.dismissSheetOnDownwardSwipe(onSwipeDown: () -> Unit): Modifier = this.pointerInput(Unit) {
    val dragDistancePx = SWIPE_DISMISS_DRAG_DISTANCE_DP.dp.toPx()
    val flingDistancePx = SWIPE_DISMISS_FLING_DISTANCE_DP.dp.toPx()
    val flingVelocityPx = SWIPE_DISMISS_FLING_VELOCITY_DP_PER_S.dp.toPx()

    val velocityTracker = VelocityTracker()
    var totalDrag = 0f

    detectVerticalDragGestures(
        onDragStart = {
            totalDrag = 0f
            velocityTracker.resetTracking()
        },
        onDragEnd = {
            val velocityY = velocityTracker.calculateVelocity().y
            val isDeliberateSwipeDown = totalDrag > dragDistancePx ||
                (totalDrag > flingDistancePx && velocityY > flingVelocityPx)
            if (isDeliberateSwipeDown) onSwipeDown()
        },
        onDragCancel = { totalDrag = 0f }
    ) { change, dragAmount ->
        change.consume()
        totalDrag += dragAmount
        velocityTracker.addPosition(change.uptimeMillis, change.position)
    }
}
