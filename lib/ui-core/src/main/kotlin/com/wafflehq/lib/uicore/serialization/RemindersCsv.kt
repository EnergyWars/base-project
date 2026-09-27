package com.wafflehq.lib.uicore.serialization

fun parseRemindersCsv(csv: String?): List<Int> =
    csv?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.filter { it > 0 } ?: emptyList()
