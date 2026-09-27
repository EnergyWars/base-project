package com.wafflehq.lib.quickpicker

import java.time.LocalDate

object QuickDateInputLogic {

    const val MAX_LENGTH = 10
    const val RELATIVE_DAYS_MAX_LENGTH = 4
    const val WEEK_LENGTH = 7

    fun nextWeek(today: LocalDate): List<LocalDate> =
        (1..WEEK_LENGTH).map { today.plusDays(it.toLong()) }

    fun lastWeek(today: LocalDate): List<LocalDate> =
        (WEEK_LENGTH downTo 1).map { today.minusDays(it.toLong()) }

    fun appendRelativeDigit(current: String, digit: Int): String {
        require(digit in 0..9) { "digit must be 0..9" }
        return (current + digit.toString()).trimStart('0').take(RELATIVE_DAYS_MAX_LENGTH)
    }

    fun relativeDate(today: LocalDate, current: String, sign: Long): LocalDate =
        today.plusDays((current.toLongOrNull() ?: 0L) * sign)

    fun separatorFor(format: DateDisplayFormat): Char =
        if (format == DateDisplayFormat.INTERNATIONAL) '-' else '.'

    fun formatForInput(date: LocalDate, format: DateDisplayFormat): String {
        val separator = separatorFor(format)
        return when (format) {
            DateDisplayFormat.GERMAN ->
                "%02d%c%02d%c%04d".format(date.dayOfMonth, separator, date.monthValue, separator, date.year)
            DateDisplayFormat.INTERNATIONAL ->
                "%04d%c%02d%c%02d".format(date.year, separator, date.monthValue, separator, date.dayOfMonth)
        }
    }

    fun appendDigit(current: String, digit: Int, format: DateDisplayFormat = DateDisplayFormat.GERMAN): String {
        require(digit in 0..9) { "digit must be 0..9" }
        if (current.length >= MAX_LENGTH) return current
        val separator = separatorFor(format)
        val result = current + digit.toString()
        val segmentIndex = current.count { it == separator }
        return when (format) {
            DateDisplayFormat.GERMAN -> when (segmentIndex) {
                0 -> applyDayRules(current, digit, result, separator)
                1 -> applyMonthRules(current, digit, result, separator)
                else -> result
            }
            DateDisplayFormat.INTERNATIONAL -> when (segmentIndex) {
                0 -> applyYearRules(current, digit, result, separator)
                1 -> applyMonthRules(current, digit, result, separator)
                else -> result
            }
        }
    }

    private fun applyDayRules(current: String, digit: Int, result: String, separator: Char): String {
        val daySoFar = current.substringAfterLast(separator)
        return when (daySoFar.length + 1) {
            1 -> if (digit >= 4) "$result$separator" else result
            2 -> {
                val v = (daySoFar + digit).toIntOrNull() ?: return current
                if (v !in 1..31) current else "$result$separator"
            }
            else -> result
        }
    }

    private fun applyMonthRules(current: String, digit: Int, result: String, separator: Char): String {
        val monthSoFar = current.substringAfterLast(separator)
        return when (monthSoFar.length + 1) {
            1 -> if (digit >= 2) "$result$separator" else result
            2 -> {
                val v = (monthSoFar + digit).toIntOrNull() ?: return current
                if (v !in 1..12) current else "$result$separator"
            }
            else -> result
        }
    }

    private fun applyYearRules(current: String, digit: Int, result: String, separator: Char): String {
        val yearSoFar = current.substringAfterLast(separator)
        return if (yearSoFar.length + 1 >= 4) "$result$separator" else result
    }

    fun appendSeparator(current: String, format: DateDisplayFormat = DateDisplayFormat.GERMAN): String {
        val separator = separatorFor(format)
        if (current.isEmpty()) return current
        if (current.length >= MAX_LENGTH) return current
        if (current.endsWith(separator)) return current
        if (current.count { it == separator } >= 2) return current
        return "$current$separator"
    }

    fun backspace(current: String): String =
        if (current.isEmpty()) current else current.dropLast(1)

    private fun expandYear(yearStr: String, reference: LocalDate): Int? = when (yearStr.length) {
        1 -> (reference.year / 10) * 10 + (yearStr.toIntOrNull() ?: return null)
        2 -> 2000 + (yearStr.toIntOrNull() ?: return null)
        3 -> 1000 + (yearStr.toIntOrNull() ?: return null)
        else -> yearStr.toIntOrNull()
    }

    fun parseDate(input: String, reference: LocalDate, format: DateDisplayFormat = DateDisplayFormat.GERMAN): LocalDate? {
        if (input.isEmpty()) return null
        val separator = separatorFor(format)
        val parts = input.split(separator)
        if (parts.size > 3) return null

        val firstStr = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return null
        val secondStr = parts.getOrNull(1)?.takeIf { it.isNotBlank() }
        val second = secondStr?.toIntOrNull()
        if (secondStr != null && second == null) return null
        val thirdStr = parts.getOrNull(2)?.takeIf { it.isNotBlank() }
        val third = thirdStr?.toIntOrNull()
        if (thirdStr != null && third == null) return null

        val (day, month, year) = when (format) {
            DateDisplayFormat.GERMAN -> {
                val day = firstStr.toIntOrNull() ?: return null
                val month = second ?: reference.monthValue
                val year = thirdStr?.let { expandYear(it, reference) ?: return null } ?: reference.year
                Triple(day, month, year)
            }
            DateDisplayFormat.INTERNATIONAL -> {
                val year = expandYear(firstStr, reference) ?: return null
                val month = second ?: reference.monthValue
                val day = third ?: reference.dayOfMonth
                Triple(day, month, year)
            }
        }

        if (month !in 1..12) return null
        return runCatching { LocalDate.of(year, month, day) }.getOrNull()
    }
}
