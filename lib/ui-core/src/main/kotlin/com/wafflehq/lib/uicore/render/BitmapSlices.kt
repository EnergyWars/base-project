package com.wafflehq.lib.uicore.render

import android.graphics.Bitmap

data class VerticalSlice(val top: Int, val height: Int)

fun verticalSlices(totalHeight: Int, maxSliceHeight: Int): List<VerticalSlice> {
    require(maxSliceHeight > 0) { "maxSliceHeight must be positive" }
    if (totalHeight <= 0) return emptyList()
    return (0 until totalHeight step maxSliceHeight).map { top ->
        VerticalSlice(top = top, height = minOf(maxSliceHeight, totalHeight - top))
    }
}

fun Bitmap.sliceVertically(maxSliceHeight: Int): List<Bitmap> {
    val slices = verticalSlices(height, maxSliceHeight)
    if (slices.size <= 1) return listOf(this)
    return slices.map { slice -> Bitmap.createBitmap(this, 0, slice.top, width, slice.height) }
}
