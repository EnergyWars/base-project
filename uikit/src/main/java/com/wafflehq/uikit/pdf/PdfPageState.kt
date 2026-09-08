package com.wafflehq.uikit.pdf

import android.graphics.Canvas
import android.graphics.pdf.PdfDocument

class PdfPageState(
    val document: PdfDocument,
    val pageWidth: Int = PdfExportUtils.PAGE_WIDTH,
    val pageHeight: Int = PdfExportUtils.PAGE_HEIGHT,
    val margin: Float = PdfExportUtils.MARGIN,
    val watermarkText: String? = null,
) {
    var pageNumber = 1
    var page: PdfDocument.Page = PdfExportUtils.startPage(document, pageNumber, pageWidth, pageHeight)
    var canvas: Canvas = page.canvas
    var y: Float = margin

    fun newPage() {
        finishCurrentPage()
        pageNumber++
        page = PdfExportUtils.startPage(document, pageNumber, pageWidth, pageHeight)
        canvas = page.canvas
        y = margin
    }

    fun ensureSpace(needed: Float) {
        if (y + needed > pageHeight - margin) newPage()
    }

    fun finish() {
        finishCurrentPage()
    }

    private fun finishCurrentPage() {
        watermarkText?.let { PdfWatermark.draw(canvas, it, pageWidth, pageHeight, margin) }
        document.finishPage(page)
    }
}
