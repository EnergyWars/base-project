package com.wafflehq.lib.pdf

import kotlin.math.roundToInt
import kotlin.math.sqrt

internal object PdfRenderSize {

    const val MAX_DIMENSION_PX = 4096
    const val MAX_PIXELS = 12_000_000L

    data class Size(val width: Int, val height: Int)

    fun fitWidth(pageWidth: Int, pageHeight: Int, targetWidthPx: Int): Size {
        val width = pageWidth.coerceAtLeast(1)
        val height = pageHeight.coerceAtLeast(1)
        val requestedScale = targetWidthPx.coerceAtLeast(1).toDouble() / width
        val dimensionScale = minOf(MAX_DIMENSION_PX.toDouble() / width, MAX_DIMENSION_PX.toDouble() / height)
        val pixelScale = sqrt(MAX_PIXELS.toDouble() / (width.toDouble() * height))
        val scale = minOf(requestedScale, dimensionScale, pixelScale)
        return Size(
            width = (width * scale).roundToInt().coerceIn(1, MAX_DIMENSION_PX),
            height = (height * scale).roundToInt().coerceIn(1, MAX_DIMENSION_PX)
        )
    }
}
