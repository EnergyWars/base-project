package com.wafflehq.lib.quickpicker

import com.wafflehq.lib.uicore.serialization.enumFromNameOrDefault
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

enum class DateDisplayFormat {
    GERMAN, INTERNATIONAL;

    companion object {
        fun fromStored(raw: String?): DateDisplayFormat =
            enumFromNameOrDefault(raw, GERMAN)
    }
}

fun DateDisplayFormat.datePattern() =
    if (this == DateDisplayFormat.GERMAN) "dd.MM.yyyy" else "yyyy-MM-dd"

fun DateDisplayFormat.dateShortYearPattern() =
    if (this == DateDisplayFormat.GERMAN) "dd.MM.yy" else "yy-MM-dd"

fun DateDisplayFormat.dayMonthPattern() =
    if (this == DateDisplayFormat.GERMAN) "d.M." else "MM-dd"

fun DateDisplayFormat.dateTimePattern() =
    if (this == DateDisplayFormat.GERMAN) "dd.MM.yyyy HH:mm" else "yyyy-MM-dd HH:mm"

fun DateDisplayFormat.dayMonthTimePattern() =
    if (this == DateDisplayFormat.GERMAN) "dd.MM. HH:mm" else "MM-dd HH:mm"

fun LocalDate.withOptionalWeekdayPrefix(
    formatted: String,
    showWeekday: Boolean,
    locale: Locale = Locale.getDefault()
): String = if (showWeekday) "${dayOfWeek.getDisplayName(TextStyle.SHORT, locale)}, $formatted" else formatted
