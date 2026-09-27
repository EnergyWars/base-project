package com.wafflehq.lib.uicore.csv

object CsvEncoding {

    fun encode(header: List<String>, rows: List<List<String>>): String {
        val builder = StringBuilder()
        builder.append(encodeLine(header))
        rows.forEach { builder.append(encodeLine(it)) }
        return builder.toString()
    }

    private fun encodeLine(values: List<String>): String = values.joinToString(",") { escape(it) } + "\n"

    private fun escape(raw: String): String {
        val value = CsvFormulaGuard.neutralize(raw)
        return if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
