package com.wafflehq.lib.pdf

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.shadow.api.Shadow
import java.io.FileInputStream

@Implements(PdfRenderer::class)
class ShadowPdfRenderer {

    @RealObject
    private lateinit var realRenderer: PdfRenderer

    internal lateinit var document: PDDocument
        private set
    internal lateinit var renderer: PDFRenderer
        private set
    private var closed = false
    private var openPage: PdfRenderer.Page? = null

    @Implementation
    fun __constructor__(input: ParcelFileDescriptor) {
        val bytes = FileInputStream(input.fileDescriptor).use { it.readBytes() }
        document = Loader.loadPDF(bytes)
        renderer = PDFRenderer(document)
    }

    @Implementation
    fun getPageCount(): Int {
        throwIfClosed()
        return document.numberOfPages
    }

    @Implementation
    fun shouldScaleForPrinting(): Boolean = false

    @Implementation
    fun openPage(index: Int): PdfRenderer.Page {
        throwIfClosed()
        check(openPage == null) { "Cannot open a new page before closing the current one" }
        require(index in 0 until document.numberOfPages) { "Invalid page index $index" }
        val page = Shadow.newInstance(
            PdfRenderer.Page::class.java,
            arrayOf(PdfRenderer::class.java, Int::class.javaPrimitiveType),
            arrayOf(realRenderer, index)
        )
        openPage = page
        return page
    }

    @Implementation
    fun close() {
        throwIfClosed()
        closed = true
        document.close()
    }

    internal fun onPageClosed(page: PdfRenderer.Page) {
        if (openPage === page) openPage = null
    }

    private fun throwIfClosed() = check(!closed) { "Renderer already closed" }
}
