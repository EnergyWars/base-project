package com.wafflehq.lib.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadow.api.Shadow
import java.io.ByteArrayInputStream
import java.io.OutputStream

@Implements(PdfDocument::class)
class ShadowPdfDocument {

    private var closed = false
    private var currentPage: PdfDocument.Page? = null
    private var currentBitmap: Bitmap? = null
    private val finishedPages = mutableListOf<PdfDocument.PageInfo>()
    private val finishedBitmaps = mutableListOf<Bitmap>()

    val finishedPageBitmaps: List<Bitmap> get() = finishedBitmaps.toList()

    @Implementation
    fun startPage(pageInfo: PdfDocument.PageInfo): PdfDocument.Page {
        check(!closed) { "document is closed!" }
        check(currentPage == null) { "Previous page not finished" }
        val bitmap = Bitmap.createBitmap(pageInfo.pageWidth, pageInfo.pageHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        val page = Shadow.newInstance(
            PdfDocument.Page::class.java,
            arrayOf(Canvas::class.java, PdfDocument.PageInfo::class.java),
            arrayOf(Canvas(bitmap), pageInfo)
        )
        currentPage = page
        currentBitmap = bitmap
        return page
    }

    @Implementation
    fun finishPage(page: PdfDocument.Page) {
        check(!closed) { "document is closed!" }
        check(page === currentPage) { "Page not started or already finished" }
        finishedPages += page.info
        currentBitmap?.let { finishedBitmaps += it }
        currentPage = null
        currentBitmap = null
    }

    @Implementation
    fun getPages(): List<PdfDocument.PageInfo> = finishedPages.toList()

    @Implementation
    fun writeTo(out: OutputStream) {
        check(!closed) { "document is closed!" }
        PDDocument().use { document ->
            finishedPages.forEachIndexed { index, info ->
                val bitmap = finishedBitmaps[index]
                val page = PDPage(PDRectangle(info.pageWidth.toFloat(), info.pageHeight.toFloat()))
                document.addPage(page)
                val image = PDImageXObject(
                    document,
                    ByteArrayInputStream(bitmap.toRgbBytes()),
                    null,
                    bitmap.width,
                    bitmap.height,
                    8,
                    PDDeviceRGB.INSTANCE
                )
                PDPageContentStream(document, page).use { stream ->
                    stream.drawImage(image, 0f, 0f, info.pageWidth.toFloat(), info.pageHeight.toFloat())
                }
            }
            document.save(out)
        }
    }

    @Implementation
    fun close() {
        closed = true
    }

    private fun Bitmap.toRgbBytes(): ByteArray {
        val pixels = IntArray(width * height)
        getPixels(pixels, 0, width, 0, 0, width, height)
        val bytes = ByteArray(pixels.size * 3)
        pixels.forEachIndexed { i, pixel ->
            bytes[i * 3] = ((pixel shr 16) and 0xFF).toByte()
            bytes[i * 3 + 1] = ((pixel shr 8) and 0xFF).toByte()
            bytes[i * 3 + 2] = (pixel and 0xFF).toByte()
        }
        return bytes
    }
}
