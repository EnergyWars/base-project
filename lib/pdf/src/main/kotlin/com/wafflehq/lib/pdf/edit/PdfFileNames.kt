package com.wafflehq.lib.pdf.edit

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PdfFileNames {

    const val EXTENSION = ".pdf"
    const val MAX_BASE_NAME_LENGTH = 80

    private val forbiddenCharacters = Regex("[\\\\/:*?\"<>|\\p{Cntrl}]")
    private val whitespaceRun = Regex("\\s+")
    private val timestampFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")

    fun sanitize(input: String, fallbackBaseName: String): String {
        val cleaned = input
            .replace(forbiddenCharacters, " ")
            .replace(whitespaceRun, " ")
            .trim()
            .removeSuffixIgnoreCase(EXTENSION)
            .trim()
            .trimEnd('.')
            .trim()
            .take(MAX_BASE_NAME_LENGTH)
            .trim()
            .trimEnd('.')
        val base = cleaned.ifBlank { fallbackBaseName.replace(forbiddenCharacters, " ").trim().ifBlank { "document" } }
        return base + EXTENSION
    }

    fun suggest(prefix: String, time: LocalDateTime): String = "${prefix.trim()}-${timestampFormat.format(time)}"

    private fun String.removeSuffixIgnoreCase(suffix: String): String =
        if (endsWith(suffix, ignoreCase = true)) dropLast(suffix.length) else this
}
