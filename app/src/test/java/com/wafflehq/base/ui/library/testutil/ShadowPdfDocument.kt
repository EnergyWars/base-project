package com.wafflehq.base.ui.library.testutil

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadow.api.Shadow
import java.io.OutputStream

@Implements(PdfDocument::class)
class ShadowPdfDocument {

    private var closed = false
    private var currentPage: PdfDocument.Page? = null
    private val finishedPages = mutableListOf<PdfDocument.PageInfo>()

    @Implementation
    fun startPage(pageInfo: PdfDocument.PageInfo): PdfDocument.Page {
        check(!closed) { "document is closed" }
        check(currentPage == null) { "previous page not finished" }
        val canvas = Canvas(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
        val page = Shadow.newInstance(
            PdfDocument.Page::class.java,
            arrayOf(Canvas::class.java, PdfDocument.PageInfo::class.java),
            arrayOf(canvas, pageInfo),
        )
        currentPage = page
        return page
    }

    @Implementation
    fun finishPage(page: PdfDocument.Page) {
        check(!closed) { "document is closed" }
        check(page === currentPage) { "page not started or already finished" }
        finishedPages += page.info
        currentPage = null
    }

    @Implementation
    fun getPages(): List<PdfDocument.PageInfo> = finishedPages.toList()

    @Implementation
    fun writeTo(out: OutputStream) {
        check(!closed) { "document is closed" }
        out.write("%PDF-1.4\n% pages ${finishedPages.size}\n%%EOF\n".toByteArray())
    }

    @Implementation
    fun close() {
        closed = true
    }
}
