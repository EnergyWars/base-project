package com.wafflehq.lib.uicore.csv

object CsvParser {

    fun parse(content: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var field = StringBuilder()
        var row = mutableListOf<String>()
        var inQuotes = false

        fun endField() {
            row.add(field.toString())
            field = StringBuilder()
        }

        fun endRow() {
            endField()
            if (row.any { it.isNotBlank() }) rows.add(row)
            row = mutableListOf()
        }

        var i = 0
        while (i < content.length) {
            val c = content[i]
            when {
                inQuotes && c == '"' && i + 1 < content.length && content[i + 1] == '"' -> {
                    field.append('"')
                    i++
                }
                inQuotes && c == '"' -> inQuotes = false
                !inQuotes && c == '"' -> inQuotes = true
                !inQuotes && c == ',' -> endField()
                !inQuotes && c == '\n' -> endRow()
                !inQuotes && c == '\r' -> {}
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) endRow()
        return rows
    }
}
