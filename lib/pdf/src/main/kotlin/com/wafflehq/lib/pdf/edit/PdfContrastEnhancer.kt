package com.wafflehq.lib.pdf.edit

import android.graphics.Bitmap

object PdfContrastEnhancer {

    fun enhance(bitmap: Bitmap, recycleSource: Boolean = false): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        DocumentContrastFilter.apply(pixels, width, height)
        val enhanced = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        enhanced.setPixels(pixels, 0, width, 0, 0, width, height)
        if (recycleSource) bitmap.recycle()
        return enhanced
    }
}
