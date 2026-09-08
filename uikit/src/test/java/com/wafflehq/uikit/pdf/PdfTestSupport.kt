package com.wafflehq.uikit.pdf

import android.graphics.Bitmap
import android.graphics.Color

internal object PdfTestSupport {

    fun hasNonWhitePixel(bitmap: Bitmap, left: Int, top: Int, right: Int, bottom: Int): Boolean {
        for (y in top until bottom) {
            for (x in left until right) {
                if (bitmap.getPixel(x, y) != Color.WHITE) return true
            }
        }
        return false
    }

    fun hasWatermarkPixels(bitmap: Bitmap, pageWidth: Int, pageHeight: Int, margin: Float): Boolean =
        hasNonWhitePixel(bitmap, pageWidth / 2, pageHeight - margin.toInt(), pageWidth, pageHeight)
}
