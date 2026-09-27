package com.wafflehq.lib.uicore.render

data class OffscreenRenderSpec(
    val widthPx: Int,
    val maxHeightPx: Int,
    val densityDpi: Int
) {
    init {
        require(widthPx > 0) { "widthPx must be positive" }
        require(maxHeightPx > 0) { "maxHeightPx must be positive" }
        require(densityDpi > 0) { "densityDpi must be positive" }
    }
}
