package com.wafflehq.base.ui.library.demos

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppPillSelector
import com.wafflehq.lib.uicore.components.AppSectionHeader
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.finance.FinanceAmountFormatter
import com.wafflehq.lib.uicore.finance.FinanceCurrencies
import com.wafflehq.lib.uicore.finance.FinanceCurrencyOption
import com.wafflehq.lib.uicore.finance.parseAmountCents
import com.wafflehq.lib.uicore.time.TimeFormats
import com.wafflehq.lib.uicore.time.rememberTickingNow
import com.wafflehq.lib.uicore.time.shortTimeFormatter
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.Locale

internal object UiCoreTimeFinanceLogic {

    const val DEFAULT_AMOUNT = "1234,56"
    const val TICK_MILLIS = 1_000L
    private const val CURRENCY_COUNT = 4

    val currencyCodes: List<String> = FinanceCurrencies.CODES.take(CURRENCY_COUNT)

    fun clockText(now: Instant, clock: Clock, locale: Locale = Locale.getDefault()): Pair<String, String> {
        val zoned = now.atZone(clock.zone)
        return zoned.format(TimeFormats.HOUR_MINUTE) to zoned.format(shortTimeFormatter(locale))
    }

    fun amountText(input: String, currency: String, locale: Locale = Locale.getDefault()): String? =
        parseAmountCents(input)?.let { FinanceAmountFormatter.format(it, currency, locale) }

    fun option(code: String, locale: Locale = Locale.getDefault()): FinanceCurrencyOption? =
        FinanceCurrencies.options(locale).firstOrNull { it.code == code }
}

internal object UiCoreTimeFinanceTags {
    const val CLOCK_24H = "libex_uicore_clock_24h"
    const val CLOCK_SHORT = "libex_uicore_clock_short"
    const val AMOUNT_INPUT = "libex_uicore_amount_input"
    const val AMOUNT_RESULT = "libex_uicore_amount_result"
    const val CURRENCY_INFO = "libex_uicore_currency_info"
    fun currency(code: String) = "libex_uicore_currency_$code"
}

@Composable
internal fun UiCoreTimeFinanceDemo(
    clock: Clock = Clock.systemDefaultZone(),
    tickingEnabled: Boolean = true,
) {
    val now by rememberTickingNow(intervalMillis = UiCoreTimeFinanceLogic.TICK_MILLIS, clock = clock, enabled = tickingEnabled)
    var amount by remember { mutableStateOf(UiCoreTimeFinanceLogic.DEFAULT_AMOUNT) }
    var currency by remember { mutableStateOf(UiCoreTimeFinanceLogic.currencyCodes.first()) }
    val (hourMinute, shortTime) = UiCoreTimeFinanceLogic.clockText(now, clock)
    val formatted = UiCoreTimeFinanceLogic.amountText(amount, currency)
    val option = UiCoreTimeFinanceLogic.option(currency)

    DemoSection(
        id = "uicore_time",
        titleRes = R.string.libex_uicore_time_title,
        descriptionRes = R.string.libex_uicore_time_desc,
        moduleRes = R.string.libex_module_uicore,
    ) {
        AppSectionHeader(date = LocalDate.now(clock))
        DemoKeyValue(
            label = stringResource(R.string.libex_uicore_clock_24h),
            value = hourMinute,
            modifier = Modifier.testTag(UiCoreTimeFinanceTags.CLOCK_24H),
        )
        DemoKeyValue(
            label = stringResource(R.string.libex_uicore_clock_short),
            value = shortTime,
            modifier = Modifier.testTag(UiCoreTimeFinanceTags.CLOCK_SHORT),
        )
        AppHorizontalDivider()
        AppPillSelector(
            options = UiCoreTimeFinanceLogic.currencyCodes,
            selected = currency,
            onSelect = { currency = it },
            label = { it },
            modifier = Modifier.fillMaxWidth(),
            testTag = { UiCoreTimeFinanceTags.currency(it) },
        )
        AppTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text(stringResource(R.string.libex_uicore_amount_input)) },
            singleLine = true,
            isError = formatted == null,
            modifier = Modifier.fillMaxWidth().testTag(UiCoreTimeFinanceTags.AMOUNT_INPUT),
        )
        DemoBodyText(
            text = formatted ?: stringResource(R.string.libex_uicore_amount_invalid),
            modifier = Modifier.testTag(UiCoreTimeFinanceTags.AMOUNT_RESULT),
        )
        option?.let {
            DemoMetaText(
                text = stringResource(R.string.libex_uicore_currency_info, it.code, it.displayName, it.symbol),
                modifier = Modifier.testTag(UiCoreTimeFinanceTags.CURRENCY_INFO),
            )
        }
    }
}
