package com.wafflehq.lib.uicore.finance

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object FinanceAmountFormatter {
    fun format(amountCents: Long, currencyCode: String, locale: Locale = Locale.getDefault()): String {
        val format = NumberFormat.getCurrencyInstance(locale)
        runCatching { format.currency = Currency.getInstance(currencyCode) }
        return format.format(amountCents / 100.0)
    }
}

fun parseAmountCents(text: String, allowZero: Boolean = false): Long? {
    val normalized = text.replace(',', '.').trim()
    val value = normalized.toDoubleOrNull() ?: return null
    if (if (allowZero) value < 0 else value <= 0) return null
    return Math.round(value * 100)
}
