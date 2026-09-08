package com.wafflehq.uikit.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadow.api.Shadow

// Robolectric has no native backend for PdfDocument (native methods return a zeroed handle),
// so the real class always fails with "document is closed!" on startPage. This fakes the
// document/page lifecycle with a plain Bitmap-backed Canvas; the written bytes are not a real
// parseable PDF, so tests reading the file back via PdfRenderer are out of scope here.
@Implements(PdfDocument::class)
class ShadowPdfDocument {

    private var closed = false
    private var currentPage: PdfDocument.Page? = null
    private val finishedPages = mutableListOf<PdfDocument.PageInfo>()

    @Implementation
    fun startPage(pageInfo: PdfDocument.PageInfo): PdfDocument.Page {
        check(!closed) { "document is closed!" }
        check(currentPage == null) { "Previous page not finished" }
        val bitmap = Bitmap.createBitmap(pageInfo.pageWidth, pageInfo.pageHeight, Bitmap.Config.ARGB_8888)
        val page = Shadow.newInstance(
            PdfDocument.Page::class.java,
            arrayOf(Canvas::class.java, PdfDocument.PageInfo::class.java),
            arrayOf(Canvas(bitmap), pageInfo),
        )
        currentPage = page
        return page
    }

    @Implementation
    fun finishPage(page: PdfDocument.Page) {
        check(!closed) { "document is closed!" }
        check(page === currentPage) { "Page not started or already finished" }
        finishedPages += page.info
        currentPage = null
    }

    @Implementation
    fun getPages(): List<PdfDocument.PageInfo> = finishedPages.toList()

    @Implementation
    fun writeTo(out: OutputStream) {
        check(!closed) { "document is closed!" }
        out.write(PLACEHOLDER_PDF_BYTES)
    }

    @Implementation
    fun close() {
        closed = true
    }

    private companion object {
        val PLACEHOLDER_PDF_BYTES = "%PDF-1.4\n%%EOF".toByteArray()
    }
}
