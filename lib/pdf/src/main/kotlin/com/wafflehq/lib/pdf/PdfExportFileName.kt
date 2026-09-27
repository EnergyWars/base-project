package com.wafflehq.lib.pdf

private val UNSAFE_FILENAME_CHARS = Regex("[\\\\/:*?\"<>|\\p{Cntrl}]")
private val WHITESPACE_RUN = Regex("\\s+")
private const val MAX_TITLE_LENGTH = 80

fun pdfExportFileName(prefix: String, title: String): String {
    val sanitizedTitle = title
        .replace(UNSAFE_FILENAME_CHARS, "")
        .trim()
        .replace(WHITESPACE_RUN, " ")
        .take(MAX_TITLE_LENGTH)
    val base = if (sanitizedTitle.isBlank()) prefix else "$prefix - $sanitizedTitle"
    return "$base.pdf"
}
