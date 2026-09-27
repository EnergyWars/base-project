package com.wafflehq.lib.uicore.io

import java.time.Instant
import java.time.format.DateTimeFormatter

fun jsonExportFileName(prefix: String): String = timestampedFileName(prefix, "json")

fun csvExportFileName(prefix: String): String = timestampedFileName(prefix, "csv")

private fun timestampedFileName(prefix: String, extension: String): String =
    "${prefix}_${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}.$extension"
