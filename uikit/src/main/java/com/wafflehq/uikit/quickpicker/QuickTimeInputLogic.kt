package com.wafflehq.uikit.quickpicker

import java.time.LocalTime

object QuickTimeInputLogic {

    const val MAX_DIGITS = 4

    fun appendDigit(current: String, digit: Int): String {
        require(digit in 0..9) { "digit must be 0..9" }
        if (current.length >= MAX_DIGITS) return current
        if (current.isEmpty() && digit > 2) return "0$digit"
        return current + digit.toString()
    }

    fun backspace(current: String): String =
        if (current.isEmpty()) current else current.dropLast(1)

    fun parseTime(digits: String): LocalTime? {
        return when (digits.length) {
            1 -> {
                val h = digits.toIntOrNull() ?: return null
                if (h !in 0..9) return null
                LocalTime.of(h, 0)
            }
            2 -> {
                val h = digits.toIntOrNull() ?: return null
                if (h !in 0..23) return null
                LocalTime.of(h, 0)
            }
            3 -> {
                val h = digits.take(2).toIntOrNull() ?: return null
                val mTens = digits.drop(2).toIntOrNull() ?: return null
                if (h !in 0..23 || mTens !in 0..5) return null
                LocalTime.of(h, mTens * 10)
            }
            MAX_DIGITS -> {
                val h = digits.take(2).toIntOrNull() ?: return null
                val m = digits.drop(2).toIntOrNull() ?: return null
                if (h !in 0..23 || m !in 0..59) return null
                LocalTime.of(h, m)
            }
            else -> null
        }
    }
}
