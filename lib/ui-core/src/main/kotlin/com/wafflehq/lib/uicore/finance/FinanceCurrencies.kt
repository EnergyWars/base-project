package com.wafflehq.lib.uicore.finance

import java.util.Currency
import java.util.Locale

data class FinanceCurrencyOption(val code: String, val displayName: String, val symbol: String)

object FinanceCurrencies {
    val CODES: List<String> = listOf(
        "EUR", "USD", "GBP", "CHF", "JPY", "CAD", "AUD", "SEK", "NOK", "DKK",
        "PLN", "CZK", "HUF", "TRY", "CNY", "INR", "BRL", "MXN", "ZAR", "NZD"
    )

    fun options(locale: Locale = Locale.getDefault()): List<FinanceCurrencyOption> = CODES.map { code ->
        val currency = runCatching { Currency.getInstance(code) }.getOrNull()
        FinanceCurrencyOption(
            code = code,
            displayName = currency?.getDisplayName(locale) ?: code,
            symbol = currency?.getSymbol(locale) ?: code
        )
    }
}
