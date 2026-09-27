package com.wafflehq.lib.pdf.edit

import com.wafflehq.lib.pdf.PdfExportUtils
import kotlin.math.min
import kotlin.math.roundToInt

data class PdfPageSize(val width: Int, val height: Int)

data class PdfFitRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

object PdfPageGeometry {

    fun imagePageSize(imageWidth: Int, imageHeight: Int, mode: PdfPageSizeMode): PdfPageSize {
        val longSide = PdfExportUtils.PAGE_HEIGHT
        val shortSide = PdfExportUtils.PAGE_WIDTH
        val landscape = imageWidth > imageHeight
        return when (mode) {
            PdfPageSizeMode.A4 ->
                if (landscape) PdfPageSize(longSide, shortSide) else PdfPageSize(shortSide, longSide)
            PdfPageSizeMode.MATCH_CONTENT -> {
                val ratio = min(imageWidth, imageHeight).toFloat() / maxOf(imageWidth, imageHeight)
                val shortScaled = (longSide * ratio).roundToInt().coerceAtLeast(1)
                if (landscape) PdfPageSize(longSide, shortScaled) else PdfPageSize(shortScaled, longSide)
            }
        }
    }

    fun renderedPageSize(bitmapWidth: Int, bitmapHeight: Int, renderScale: Int): PdfPageSize {
        val scale = renderScale.coerceAtLeast(1)
        return PdfPageSize((bitmapWidth / scale).coerceAtLeast(1), (bitmapHeight / scale).coerceAtLeast(1))
    }

    fun fitInside(sourceWidth: Int, sourceHeight: Int, page: PdfPageSize): PdfFitRect {
        val scale = min(page.width.toFloat() / sourceWidth, page.height.toFloat() / sourceHeight)
        val width = sourceWidth * scale
        val height = sourceHeight * scale
        val left = (page.width - width) / 2f
        val top = (page.height - height) / 2f
        return PdfFitRect(left, top, left + width, top + height)
    }
}
