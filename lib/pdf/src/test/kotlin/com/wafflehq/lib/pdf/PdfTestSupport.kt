package com.wafflehq.lib.pdf

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import org.robolectric.shadow.api.Shadow
import java.io.File
import java.io.FileOutputStream

internal object PdfTestSupport {

    fun writeToTempFile(document: PdfDocument): File {
        val file = File.createTempFile("pdf-test", ".pdf")
        file.deleteOnExit()
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun pageBitmap(document: PdfDocument, index: Int): Bitmap =
        Shadow.extract<ShadowPdfDocument>(document).finishedPageBitmaps[index]

    fun finishedPageCount(document: PdfDocument): Int =
        Shadow.extract<ShadowPdfDocument>(document).finishedPageBitmaps.size

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
