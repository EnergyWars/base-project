package com.wafflehq.base.ui.library.demos

import android.app.Application
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UiCoreTimeFinanceDemoTest : LibraryDemoTest() {

    private val instant = Instant.parse("2026-03-10T14:05:00Z")
    private val clock = Clock.fixed(instant, ZoneOffset.UTC)

    private fun showDemo() = show { UiCoreTimeFinanceDemo(clock = clock, tickingEnabled = false) }

    @Test
    fun showsTheClockInBothFormats() {
        showDemo()

        val (hourMinute, shortTime) = UiCoreTimeFinanceLogic.clockText(instant, clock)
        assertEquals("14:05", hourMinute)
        assertTagText(UiCoreTimeFinanceTags.CLOCK_24H, hourMinute)
        assertTagText(UiCoreTimeFinanceTags.CLOCK_SHORT, shortTime)
    }

    @Test
    fun formatsTheDefaultAmountInEuro() {
        showDemo()

        val expected = UiCoreTimeFinanceLogic.amountText(UiCoreTimeFinanceLogic.DEFAULT_AMOUNT, "EUR")
        assertTagText(UiCoreTimeFinanceTags.AMOUNT_RESULT, expected!!)
    }

    @Test
    fun switchingTheCurrencyReformatsTheAmount() {
        showDemo()

        click(UiCoreTimeFinanceTags.currency("USD"))

        val expected = UiCoreTimeFinanceLogic.amountText(UiCoreTimeFinanceLogic.DEFAULT_AMOUNT, "USD")
        assertTagText(UiCoreTimeFinanceTags.AMOUNT_RESULT, expected!!)
        assertTagText(UiCoreTimeFinanceTags.CURRENCY_INFO, "USD")
    }

    @Test
    fun invalidAmountsShowAHint() {
        showDemo()

        replaceText(UiCoreTimeFinanceTags.AMOUNT_INPUT, "abc")
        assertTagText(UiCoreTimeFinanceTags.AMOUNT_RESULT, string(R.string.libex_uicore_amount_invalid))

        replaceText(UiCoreTimeFinanceTags.AMOUNT_INPUT, "0")
        assertTagText(UiCoreTimeFinanceTags.AMOUNT_RESULT, string(R.string.libex_uicore_amount_invalid))
    }

    @Test
    fun amountTextFormatsWithTheGivenLocale() {
        assertEquals("1.234,56 €", UiCoreTimeFinanceLogic.amountText("1234,56", "EUR", Locale.GERMANY)?.replace('\u00A0', ' ')?.replace('\u202F', ' '))
        assertNull(UiCoreTimeFinanceLogic.amountText("-5", "EUR"))
    }

    @Test
    fun clockTextUsesTheClockZone() {
        val plusTwo = Clock.fixed(instant, ZoneOffset.ofHours(2))

        assertEquals("16:05", UiCoreTimeFinanceLogic.clockText(instant, plusTwo).first)
    }

    @Test
    fun currencyCodesAreLimitedToTheFirstFour() {
        assertEquals(listOf("EUR", "USD", "GBP", "CHF"), UiCoreTimeFinanceLogic.currencyCodes)
        assertTrue(UiCoreTimeFinanceLogic.option("EUR")?.symbol?.isNotEmpty() == true)
        assertNull(UiCoreTimeFinanceLogic.option("XXX"))
    }
}
