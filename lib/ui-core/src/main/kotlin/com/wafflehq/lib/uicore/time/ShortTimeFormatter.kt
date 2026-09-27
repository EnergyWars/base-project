package com.wafflehq.lib.uicore.time

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun shortTimeFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
