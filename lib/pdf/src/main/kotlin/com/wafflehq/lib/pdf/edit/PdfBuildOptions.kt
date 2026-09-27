package com.wafflehq.lib.pdf.edit

enum class PdfPageSizeMode { A4, MATCH_CONTENT }

enum class PdfImageQuality(val maxImageSidePx: Int, val pdfRenderScale: Int) {
    HIGH(maxImageSidePx = 2480, pdfRenderScale = 3),
    MEDIUM(maxImageSidePx = 1754, pdfRenderScale = 2),
    LOW(maxImageSidePx = 1240, pdfRenderScale = 1)
}

data class PdfBuildOptions(
    val pageSizeMode: PdfPageSizeMode = PdfPageSizeMode.A4,
    val quality: PdfImageQuality = PdfImageQuality.MEDIUM
)
