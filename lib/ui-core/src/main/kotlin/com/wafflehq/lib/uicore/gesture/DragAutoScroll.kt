package com.wafflehq.lib.uicore.gesture

fun dragAutoScrollSpeed(
    positionY: Float,
    containerTop: Float,
    containerBottom: Float,
    edgePx: Float,
    maxSpeedPx: Float
): Float {
    if (edgePx <= 0f) return 0f
    val distanceFromTop = positionY - containerTop
    val distanceFromBottom = containerBottom - positionY
    return when {
        distanceFromTop < edgePx ->
            -maxSpeedPx * (1f - (distanceFromTop / edgePx).coerceIn(0f, 1f))
        distanceFromBottom < edgePx ->
            maxSpeedPx * (1f - (distanceFromBottom / edgePx).coerceIn(0f, 1f))
        else -> 0f
    }
}
