package com.wafflehq.lib.pdf.edit

import android.graphics.Bitmap
import android.graphics.Matrix

object PdfBitmapRotation {

    fun rotate(bitmap: Bitmap, degrees: Int, recycleSource: Boolean = false): Bitmap {
        val normalized = PdfPageListEditor.normalizeRotation(degrees)
        if (normalized == 0) return bitmap
        val matrix = Matrix().apply { postRotate(normalized.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (recycleSource && rotated !== bitmap) bitmap.recycle()
        return rotated
    }
}
