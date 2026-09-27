package com.wafflehq.lib.uicore.csv

object CsvFormulaGuard {

    private const val ESCAPE_PREFIX = '\''
    private val TRIGGER_CHARACTERS = charArrayOf('=', '+', '-', '@', '\t', '\r')
    private val PLAIN_NUMBER = Regex("^[+-]?\\d+([.,]\\d+)?$")

    fun neutralize(value: String): String =
        if (needsGuard(value)) "$ESCAPE_PREFIX$value" else value

    fun restore(value: String): String =
        if (value.length > 1 && value[0] == ESCAPE_PREFIX && needsGuard(value.substring(1))) value.substring(1) else value

    private fun needsGuard(value: String): Boolean =
        value.isNotEmpty() && value[0] in TRIGGER_CHARACTERS && !PLAIN_NUMBER.matches(value)
}
