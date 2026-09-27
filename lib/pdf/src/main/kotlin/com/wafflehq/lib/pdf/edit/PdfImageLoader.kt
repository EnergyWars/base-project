package com.wafflehq.lib.pdf.edit

import android.graphics.Bitmap
import java.io.File

fun interface PdfImageLoader {
    fun load(file: File, maxDimension: Int): Bitmap?
}
