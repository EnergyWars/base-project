package com.wafflehq.lib.pdf.edit

import java.io.File

sealed interface PdfPageContent {
    data class Image(val file: File) : PdfPageContent
    data class PdfPage(val file: File, val pageIndex: Int) : PdfPageContent
}

data class PdfEditPage(
    val id: Long,
    val content: PdfPageContent,
    val rotationDegrees: Int = 0,
    val enhanceContrast: Boolean = false
)
