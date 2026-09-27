package com.wafflehq.lib.quickpicker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

class QuickDateInputLogicTest {

    private val reference = LocalDate.of(2026, 5, 30)

    @Test
    fun `day first digit 0 no auto dot`() {
        assertEquals("0", QuickDateInputLogic.appendDigit("", 0))
    }

    @Test
    fun `day first digit 1 no auto dot`() {
        assertEquals("1", QuickDateInputLogic.appendDigit("", 1))
    }

    @Test
    fun `day first digit 2 no auto dot`() {
        assertEquals("2", QuickDateInputLogic.appendDigit("", 2))
    }

    @Test
    fun `day first digit 3 no auto dot`() {
        assertEquals("3", QuickDateInputLogic.appendDigit("", 3))
    }

    @Test
    fun `day first digit 4 auto dot`() {
        assertEquals("4.", QuickDateInputLogic.appendDigit("", 4))
    }

    @Test
    fun `day first digit 5 auto dot`() {
        assertEquals("5.", QuickDateInputLogic.appendDigit("", 5))
    }

    @Test
    fun `day first digit 6 auto dot`() {
        assertEquals("6.", QuickDateInputLogic.appendDigit("", 6))
    }

    @Test
    fun `day first digit 7 auto dot`() {
        assertEquals("7.", QuickDateInputLogic.appendDigit("", 7))
    }

    @Test
    fun `day first digit 8 auto dot`() {
        assertEquals("8.", QuickDateInputLogic.appendDigit("", 8))
    }

    @Test
    fun `day first digit 9 auto dot`() {
        assertEquals("9.", QuickDateInputLogic.appendDigit("", 9))
    }

    @Test
    fun `day after 0 second digit 0 rejected`() {
        assertEquals("0", QuickDateInputLogic.appendDigit("0", 0))
    }

    @Test
    fun `day after 0 second digit 1 through 9 all valid and auto dot`() {
        assertEquals("01.", QuickDateInputLogic.appendDigit("0", 1))
        assertEquals("02.", QuickDateInputLogic.appendDigit("0", 2))
        assertEquals("03.", QuickDateInputLogic.appendDigit("0", 3))
        assertEquals("04.", QuickDateInputLogic.appendDigit("0", 4))
        assertEquals("05.", QuickDateInputLogic.appendDigit("0", 5))
        assertEquals("06.", QuickDateInputLogic.appendDigit("0", 6))
        assertEquals("07.", QuickDateInputLogic.appendDigit("0", 7))
        assertEquals("08.", QuickDateInputLogic.appendDigit("0", 8))
        assertEquals("09.", QuickDateInputLogic.appendDigit("0", 9))
    }

    @Test
    fun `day after 1 all second digits valid and auto dot`() {
        assertEquals("10.", QuickDateInputLogic.appendDigit("1", 0))
        assertEquals("11.", QuickDateInputLogic.appendDigit("1", 1))
        assertEquals("12.", QuickDateInputLogic.appendDigit("1", 2))
        assertEquals("13.", QuickDateInputLogic.appendDigit("1", 3))
        assertEquals("14.", QuickDateInputLogic.appendDigit("1", 4))
        assertEquals("15.", QuickDateInputLogic.appendDigit("1", 5))
        assertEquals("16.", QuickDateInputLogic.appendDigit("1", 6))
        assertEquals("17.", QuickDateInputLogic.appendDigit("1", 7))
        assertEquals("18.", QuickDateInputLogic.appendDigit("1", 8))
        assertEquals("19.", QuickDateInputLogic.appendDigit("1", 9))
    }

    @Test
    fun `day after 2 all second digits valid and auto dot`() {
        assertEquals("20.", QuickDateInputLogic.appendDigit("2", 0))
        assertEquals("21.", QuickDateInputLogic.appendDigit("2", 1))
        assertEquals("22.", QuickDateInputLogic.appendDigit("2", 2))
        assertEquals("23.", QuickDateInputLogic.appendDigit("2", 3))
        assertEquals("24.", QuickDateInputLogic.appendDigit("2", 4))
        assertEquals("25.", QuickDateInputLogic.appendDigit("2", 5))
        assertEquals("26.", QuickDateInputLogic.appendDigit("2", 6))
        assertEquals("27.", QuickDateInputLogic.appendDigit("2", 7))
        assertEquals("28.", QuickDateInputLogic.appendDigit("2", 8))
        assertEquals("29.", QuickDateInputLogic.appendDigit("2", 9))
    }

    @Test
    fun `day after 3 digits 0 and 1 valid and auto dot`() {
        assertEquals("30.", QuickDateInputLogic.appendDigit("3", 0))
        assertEquals("31.", QuickDateInputLogic.appendDigit("3", 1))
    }

    @Test
    fun `day after 3 digits 2 through 9 rejected`() {
        assertEquals("3", QuickDateInputLogic.appendDigit("3", 2))
        assertEquals("3", QuickDateInputLogic.appendDigit("3", 3))
        assertEquals("3", QuickDateInputLogic.appendDigit("3", 4))
        assertEquals("3", QuickDateInputLogic.appendDigit("3", 5))
        assertEquals("3", QuickDateInputLogic.appendDigit("3", 6))
        assertEquals("3", QuickDateInputLogic.appendDigit("3", 7))
        assertEquals("3", QuickDateInputLogic.appendDigit("3", 8))
        assertEquals("3", QuickDateInputLogic.appendDigit("3", 9))
    }

    @Test
    fun `day after backspace from auto-dotted state second digit still validates`() {
        val afterBackspace = QuickDateInputLogic.backspace("4.")
        assertEquals("4", afterBackspace)
        for (d in 0..9) {
            assertEquals("4", QuickDateInputLogic.appendDigit(afterBackspace, d))
        }
    }

    @Test
    fun `day after backspace from 31 dot state invalid second digit rejected`() {
        val afterBackspace = QuickDateInputLogic.backspace("31.")
        assertEquals("31", afterBackspace)
        assertEquals("311", QuickDateInputLogic.appendDigit(afterBackspace, 1))
    }

    @Test
    fun `month first digit 0 no auto dot`() {
        assertEquals("16.0", QuickDateInputLogic.appendDigit("16.", 0))
    }

    @Test
    fun `month first digit 1 no auto dot`() {
        assertEquals("16.1", QuickDateInputLogic.appendDigit("16.", 1))
    }

    @Test
    fun `month first digit 2 auto dot`() {
        assertEquals("16.2.", QuickDateInputLogic.appendDigit("16.", 2))
    }

    @Test
    fun `month first digit 3 auto dot`() {
        assertEquals("16.3.", QuickDateInputLogic.appendDigit("16.", 3))
    }

    @Test
    fun `month first digit 4 auto dot`() {
        assertEquals("16.4.", QuickDateInputLogic.appendDigit("16.", 4))
    }

    @Test
    fun `month first digit 5 auto dot`() {
        assertEquals("16.5.", QuickDateInputLogic.appendDigit("16.", 5))
    }

    @Test
    fun `month first digit 6 auto dot`() {
        assertEquals("16.6.", QuickDateInputLogic.appendDigit("16.", 6))
    }

    @Test
    fun `month first digit 7 auto dot`() {
        assertEquals("16.7.", QuickDateInputLogic.appendDigit("16.", 7))
    }

    @Test
    fun `month first digit 8 auto dot`() {
        assertEquals("16.8.", QuickDateInputLogic.appendDigit("16.", 8))
    }

    @Test
    fun `month first digit 9 auto dot`() {
        assertEquals("16.9.", QuickDateInputLogic.appendDigit("16.", 9))
    }

    @Test
    fun `month first digit auto dot with various day prefixes`() {
        assertEquals("4.3.", QuickDateInputLogic.appendDigit("4.", 3))
        assertEquals("09.5.", QuickDateInputLogic.appendDigit("09.", 5))
        assertEquals("31.7.", QuickDateInputLogic.appendDigit("31.", 7))
        assertEquals("1.9.", QuickDateInputLogic.appendDigit("1.", 9))
    }

    @Test
    fun `month after 0 second digit 0 rejected`() {
        assertEquals("16.0", QuickDateInputLogic.appendDigit("16.0", 0))
    }

    @Test
    fun `month after 0 second digit 1 through 9 all valid and auto dot`() {
        assertEquals("16.01.", QuickDateInputLogic.appendDigit("16.0", 1))
        assertEquals("16.02.", QuickDateInputLogic.appendDigit("16.0", 2))
        assertEquals("16.03.", QuickDateInputLogic.appendDigit("16.0", 3))
        assertEquals("16.04.", QuickDateInputLogic.appendDigit("16.0", 4))
        assertEquals("16.05.", QuickDateInputLogic.appendDigit("16.0", 5))
        assertEquals("16.06.", QuickDateInputLogic.appendDigit("16.0", 6))
        assertEquals("16.07.", QuickDateInputLogic.appendDigit("16.0", 7))
        assertEquals("16.08.", QuickDateInputLogic.appendDigit("16.0", 8))
        assertEquals("16.09.", QuickDateInputLogic.appendDigit("16.0", 9))
    }

    @Test
    fun `month after 1 digits 0 through 2 valid and auto dot`() {
        assertEquals("16.10.", QuickDateInputLogic.appendDigit("16.1", 0))
        assertEquals("16.11.", QuickDateInputLogic.appendDigit("16.1", 1))
        assertEquals("16.12.", QuickDateInputLogic.appendDigit("16.1", 2))
    }

    @Test
    fun `month after 1 digits 3 through 9 rejected`() {
        assertEquals("16.1", QuickDateInputLogic.appendDigit("16.1", 3))
        assertEquals("16.1", QuickDateInputLogic.appendDigit("16.1", 4))
        assertEquals("16.1", QuickDateInputLogic.appendDigit("16.1", 5))
        assertEquals("16.1", QuickDateInputLogic.appendDigit("16.1", 6))
        assertEquals("16.1", QuickDateInputLogic.appendDigit("16.1", 7))
        assertEquals("16.1", QuickDateInputLogic.appendDigit("16.1", 8))
        assertEquals("16.1", QuickDateInputLogic.appendDigit("16.1", 9))
    }

    @Test
    fun `month rejected digit is idempotent on repeated presses`() {
        var s = "12.1"
        repeat(5) { s = QuickDateInputLogic.appendDigit(s, 3) }
        assertEquals("12.1", s)
    }

    @Test
    fun `year segment appends all digits freely`() {
        assertEquals("16.8.0", QuickDateInputLogic.appendDigit("16.8.", 0))
        assertEquals("16.8.1", QuickDateInputLogic.appendDigit("16.8.", 1))
        assertEquals("16.8.9", QuickDateInputLogic.appendDigit("16.8.", 9))
        assertEquals("16.8.20", QuickDateInputLogic.appendDigit("16.8.2", 0))
        assertEquals("16.8.202", QuickDateInputLogic.appendDigit("16.8.20", 2))
        assertEquals("16.8.2026", QuickDateInputLogic.appendDigit("16.8.202", 6))
    }

    @Test
    fun `appendDigit out of range throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            QuickDateInputLogic.appendDigit("", -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            QuickDateInputLogic.appendDigit("", 10)
        }
    }

    @Test
    fun `appendDigit respects max length`() {
        var s = ""
        repeat(20) { s = QuickDateInputLogic.appendDigit(s, 1) }
        assertEquals(QuickDateInputLogic.MAX_LENGTH, s.length)
    }

    @Test
    fun `appendDot not allowed as first character`() {
        assertEquals("", QuickDateInputLogic.appendSeparator(""))
    }

    @Test
    fun `appendDot adds separator after day`() {
        assertEquals("16.", QuickDateInputLogic.appendSeparator("16"))
        assertEquals("1.", QuickDateInputLogic.appendSeparator("1"))
        assertEquals("5.", QuickDateInputLogic.appendSeparator("5"))
    }

    @Test
    fun `appendDot adds separator after month`() {
        assertEquals("16.8.", QuickDateInputLogic.appendSeparator("16.8"))
        assertEquals("16.12.", QuickDateInputLogic.appendSeparator("16.12"))
    }

    @Test
    fun `appendDot does not allow consecutive dots`() {
        assertEquals("16.", QuickDateInputLogic.appendSeparator("16."))
        assertEquals("16.8.", QuickDateInputLogic.appendSeparator("16.8."))
    }

    @Test
    fun `appendDot does not allow more than two dots`() {
        assertEquals("16.8.2026", QuickDateInputLogic.appendSeparator("16.8.2026"))
        assertEquals("16.8.20", QuickDateInputLogic.appendSeparator("16.8.20"))
    }

    @Test
    fun `backspace on empty stays empty`() {
        assertEquals("", QuickDateInputLogic.backspace(""))
    }

    @Test
    fun `backspace drops last character`() {
        assertEquals("16.", QuickDateInputLogic.backspace("16.8"))
        assertEquals("16", QuickDateInputLogic.backspace("16."))
        assertEquals("1", QuickDateInputLogic.backspace("16"))
        assertEquals("", QuickDateInputLogic.backspace("1"))
    }

    @Test
    fun `backspace on full date removes last digit`() {
        assertEquals("16.8.202", QuickDateInputLogic.backspace("16.8.2026"))
    }

    @Test
    fun `parseDate empty returns null`() {
        assertNull(QuickDateInputLogic.parseDate("", reference))
    }

    @Test
    fun `parseDate day only uses reference month and year`() {
        assertEquals(LocalDate.of(2026, 5, 1), QuickDateInputLogic.parseDate("1", reference))
        assertEquals(LocalDate.of(2026, 5, 16), QuickDateInputLogic.parseDate("16", reference))
        assertEquals(LocalDate.of(2026, 5, 9), QuickDateInputLogic.parseDate("9", reference))
    }

    @Test
    fun `parseDate day with trailing dot uses reference month and year`() {
        assertEquals(LocalDate.of(2026, 5, 16), QuickDateInputLogic.parseDate("16.", reference))
        assertEquals(LocalDate.of(2026, 5, 5), QuickDateInputLogic.parseDate("5.", reference))
    }

    @Test
    fun `parseDate day and month uses reference year`() {
        assertEquals(LocalDate.of(2026, 1, 16), QuickDateInputLogic.parseDate("16.1", reference))
        assertEquals(LocalDate.of(2026, 8, 16), QuickDateInputLogic.parseDate("16.8", reference))
        assertEquals(LocalDate.of(2026, 12, 3), QuickDateInputLogic.parseDate("3.12", reference))
    }

    @Test
    fun `parseDate day and month with trailing dot uses reference year`() {
        assertEquals(LocalDate.of(2026, 8, 16), QuickDateInputLogic.parseDate("16.8.", reference))
        assertEquals(LocalDate.of(2026, 3, 1), QuickDateInputLogic.parseDate("1.3.", reference))
    }

    @Test
    fun `parseDate full date with four digit year`() {
        assertEquals(LocalDate.of(2024, 8, 16), QuickDateInputLogic.parseDate("16.8.2024", reference))
        assertEquals(LocalDate.of(2024, 8, 16), QuickDateInputLogic.parseDate("16.08.2024", reference))
        assertEquals(LocalDate.of(2000, 1, 1), QuickDateInputLogic.parseDate("1.1.2000", reference))
        assertEquals(LocalDate.of(1999, 12, 31), QuickDateInputLogic.parseDate("31.12.1999", reference))
    }

    @Test
    fun `parseDate single digit year 0 maps to start of current decade`() {
        assertEquals(LocalDate.of(2020, 1, 1), QuickDateInputLogic.parseDate("1.1.0", reference))
    }

    @Test
    fun `parseDate single digit year 1 through 5 maps within current decade`() {
        assertEquals(LocalDate.of(2021, 6, 15), QuickDateInputLogic.parseDate("15.6.1", reference))
        assertEquals(LocalDate.of(2022, 3, 10), QuickDateInputLogic.parseDate("10.3.2", reference))
        assertEquals(LocalDate.of(2023, 11, 1), QuickDateInputLogic.parseDate("1.11.3", reference))
        assertEquals(LocalDate.of(2024, 2, 29), QuickDateInputLogic.parseDate("29.2.4", reference))
        assertEquals(LocalDate.of(2025, 7, 4), QuickDateInputLogic.parseDate("4.7.5", reference))
    }

    @Test
    fun `parseDate single digit year 6 matches current year`() {
        assertEquals(LocalDate.of(2026, 7, 12), QuickDateInputLogic.parseDate("12.7.6", reference))
        assertEquals(LocalDate.of(2026, 5, 30), QuickDateInputLogic.parseDate("30.5.6", reference))
    }

    @Test
    fun `parseDate single digit year 7 through 9 maps to future years in current decade`() {
        assertEquals(LocalDate.of(2027, 6, 12), QuickDateInputLogic.parseDate("12.6.7", reference))
        assertEquals(LocalDate.of(2028, 1, 1), QuickDateInputLogic.parseDate("1.1.8", reference))
        assertEquals(LocalDate.of(2029, 12, 31), QuickDateInputLogic.parseDate("31.12.9", reference))
    }

    @Test
    fun `parseDate two digit year 07 becomes 2007`() {
        assertEquals(LocalDate.of(2007, 6, 12), QuickDateInputLogic.parseDate("12.6.07", reference))
    }

    @Test
    fun `parseDate two digit year 00 becomes 2000`() {
        assertEquals(LocalDate.of(2000, 1, 1), QuickDateInputLogic.parseDate("1.1.00", reference))
    }

    @Test
    fun `parseDate two digit year 24 becomes 2024`() {
        assertEquals(LocalDate.of(2024, 8, 16), QuickDateInputLogic.parseDate("16.8.24", reference))
    }

    @Test
    fun `parseDate two digit year 99 becomes 2099`() {
        assertEquals(LocalDate.of(2099, 1, 1), QuickDateInputLogic.parseDate("1.1.99", reference))
    }

    @Test
    fun `parseDate three digit year 994 becomes 1994`() {
        assertEquals(LocalDate.of(1994, 7, 12), QuickDateInputLogic.parseDate("12.7.994", reference))
    }

    @Test
    fun `parseDate three digit year 001 becomes 1001`() {
        assertEquals(LocalDate.of(1001, 3, 5), QuickDateInputLogic.parseDate("5.3.001", reference))
    }

    @Test
    fun `parseDate three digit year 100 becomes 1100`() {
        assertEquals(LocalDate.of(1100, 6, 20), QuickDateInputLogic.parseDate("20.6.100", reference))
    }

    @Test
    fun `parseDate three digit year 999 becomes 1999`() {
        assertEquals(LocalDate.of(1999, 12, 31), QuickDateInputLogic.parseDate("31.12.999", reference))
    }

    @Test
    fun `parseDate rejects invalid month`() {
        assertNull(QuickDateInputLogic.parseDate("16.13", reference))
        assertNull(QuickDateInputLogic.parseDate("16.0", reference))
        assertNull(QuickDateInputLogic.parseDate("1.20", reference))
    }

    @Test
    fun `parseDate rejects invalid day`() {
        assertNull(QuickDateInputLogic.parseDate("32.1", reference))
        assertNull(QuickDateInputLogic.parseDate("0.1", reference))
        assertNull(QuickDateInputLogic.parseDate("0", reference))
        assertNull(QuickDateInputLogic.parseDate("00.1", reference))
    }

    @Test
    fun `parseDate rejects day that does not exist in month`() {
        assertNull(QuickDateInputLogic.parseDate("31.2", reference))
        assertNull(QuickDateInputLogic.parseDate("31.4", reference))
        assertNull(QuickDateInputLogic.parseDate("31.6", reference))
        assertNull(QuickDateInputLogic.parseDate("31.9", reference))
        assertNull(QuickDateInputLogic.parseDate("31.11", reference))
    }

    @Test
    fun `parseDate accepts leap day in leap year`() {
        assertEquals(LocalDate.of(2024, 2, 29), QuickDateInputLogic.parseDate("29.2.2024", reference))
        assertEquals(LocalDate.of(2024, 2, 29), QuickDateInputLogic.parseDate("29.2.4", reference))
    }

    @Test
    fun `parseDate rejects leap day in non leap year`() {
        assertNull(QuickDateInputLogic.parseDate("29.2.2025", reference))
        assertNull(QuickDateInputLogic.parseDate("29.2.5", reference))
    }

    @Test
    fun `parseDate rejects non numeric input`() {
        assertNull(QuickDateInputLogic.parseDate("ab", reference))
        assertNull(QuickDateInputLogic.parseDate("16.x", reference))
        assertNull(QuickDateInputLogic.parseDate("16.8.abc", reference))
    }

    @Test
    fun `parseDate rejects too many dot segments`() {
        assertNull(QuickDateInputLogic.parseDate("16.8.2024.1", reference))
    }

    @Test
    fun `full sequence two digit day and single digit month auto dots`() {
        var s = ""
        s = QuickDateInputLogic.appendDigit(s, 1)
        s = QuickDateInputLogic.appendDigit(s, 6)
        s = QuickDateInputLogic.appendSeparator(s)
        s = QuickDateInputLogic.appendDigit(s, 8)
        assertEquals("16.8.", s)
        assertEquals(LocalDate.of(2026, 8, 16), QuickDateInputLogic.parseDate(s, reference))
    }

    @Test
    fun `full sequence single digit day four or higher auto dots immediately`() {
        var s = ""
        s = QuickDateInputLogic.appendDigit(s, 5)
        s = QuickDateInputLogic.appendDigit(s, 3)
        assertEquals("5.3.", s)
        assertEquals(LocalDate.of(2026, 3, 5), QuickDateInputLogic.parseDate(s, reference))
    }

    @Test
    fun `full sequence leading zero day and leading zero month`() {
        var s = ""
        s = QuickDateInputLogic.appendDigit(s, 0)
        s = QuickDateInputLogic.appendDigit(s, 9)
        s = QuickDateInputLogic.appendDigit(s, 0)
        s = QuickDateInputLogic.appendDigit(s, 3)
        assertEquals("09.03.", s)
        assertEquals(LocalDate.of(2026, 3, 9), QuickDateInputLogic.parseDate(s, reference))
    }

    @Test
    fun `full sequence with full year`() {
        var s = ""
        s = QuickDateInputLogic.appendDigit(s, 1)
        s = QuickDateInputLogic.appendDigit(s, 6)
        s = QuickDateInputLogic.appendDigit(s, 8)
        s = QuickDateInputLogic.appendDigit(s, 2)
        s = QuickDateInputLogic.appendDigit(s, 0)
        s = QuickDateInputLogic.appendDigit(s, 2)
        s = QuickDateInputLogic.appendDigit(s, 4)
        assertEquals("16.8.2024", s)
        assertEquals(LocalDate.of(2024, 8, 16), QuickDateInputLogic.parseDate(s, reference))
    }

    @Test
    fun `full sequence with single digit year uses current decade`() {
        var s = ""
        s = QuickDateInputLogic.appendDigit(s, 1)
        s = QuickDateInputLogic.appendDigit(s, 2)
        s = QuickDateInputLogic.appendDigit(s, 7)
        s = QuickDateInputLogic.appendDigit(s, 6)
        assertEquals("12.7.6", s)
        assertEquals(LocalDate.of(2026, 7, 12), QuickDateInputLogic.parseDate(s, reference))
    }

    @Test
    fun `full sequence with three digit year becomes 1000 plus`() {
        var s = ""
        s = QuickDateInputLogic.appendDigit(s, 1)
        s = QuickDateInputLogic.appendDigit(s, 2)
        s = QuickDateInputLogic.appendDigit(s, 7)
        s = QuickDateInputLogic.appendDigit(s, 9)
        s = QuickDateInputLogic.appendDigit(s, 9)
        s = QuickDateInputLogic.appendDigit(s, 4)
        assertEquals("12.7.994", s)
        assertEquals(LocalDate.of(1994, 7, 12), QuickDateInputLogic.parseDate(s, reference))
    }

    @Test
    fun `full sequence invalid month digit repeatedly rejected`() {
        var s = "12."
        s = QuickDateInputLogic.appendDigit(s, 1)
        repeat(5) { s = QuickDateInputLogic.appendDigit(s, 3) }
        assertEquals("12.1", s)
    }

    @Test
    fun `full sequence invalid day digit repeatedly rejected`() {
        var s = "3"
        repeat(5) { s = QuickDateInputLogic.appendDigit(s, 5) }
        assertEquals("3", s)
    }

    @Test
    fun `full sequence backspace and retype corrects invalid month`() {
        var s = "16.1"
        s = QuickDateInputLogic.appendDigit(s, 9)
        assertEquals("16.1", s)
        s = QuickDateInputLogic.backspace(s)
        s = QuickDateInputLogic.appendDigit(s, 8)
        assertEquals("16.8.", s)
        assertEquals(LocalDate.of(2026, 8, 16), QuickDateInputLogic.parseDate(s, reference))
    }

    @Test
    fun `full sequence dot press after auto-dot is no-op`() {
        var s = ""
        s = QuickDateInputLogic.appendDigit(s, 1)
        s = QuickDateInputLogic.appendDigit(s, 2)
        s = QuickDateInputLogic.appendSeparator(s)
        s = QuickDateInputLogic.appendSeparator(s)
        s = QuickDateInputLogic.appendDigit(s, 5)
        s = QuickDateInputLogic.appendSeparator(s)
        assertEquals("12.5.", s)
    }

    @Test
    fun `full sequence december 31`() {
        var s = ""
        s = QuickDateInputLogic.appendDigit(s, 3)
        s = QuickDateInputLogic.appendDigit(s, 1)
        s = QuickDateInputLogic.appendDigit(s, 1)
        s = QuickDateInputLogic.appendDigit(s, 2)
        assertEquals("31.12.", s)
        assertEquals(LocalDate.of(2026, 12, 31), QuickDateInputLogic.parseDate(s, reference))
    }

    @Test
    fun `full sequence january 1`() {
        var s = ""
        s = QuickDateInputLogic.appendDigit(s, 0)
        s = QuickDateInputLogic.appendDigit(s, 1)
        s = QuickDateInputLogic.appendDigit(s, 0)
        s = QuickDateInputLogic.appendDigit(s, 1)
        assertEquals("01.01.", s)
        assertEquals(LocalDate.of(2026, 1, 1), QuickDateInputLogic.parseDate(s, reference))
    }

    @Test
    fun `next week starts tomorrow and spans seven days`() {
        val week = QuickDateInputLogic.nextWeek(reference)
        assertEquals(7, week.size)
        assertEquals(reference.plusDays(1), week.first())
        assertEquals(reference.plusDays(7), week.last())
        assertEquals((1L..7L).map { reference.plusDays(it) }, week)
    }

    @Test
    fun `last week ends yesterday and spans seven days`() {
        val week = QuickDateInputLogic.lastWeek(reference)
        assertEquals(7, week.size)
        assertEquals(reference.minusDays(7), week.first())
        assertEquals(reference.minusDays(1), week.last())
        assertEquals((7L downTo 1L).map { reference.minusDays(it) }, week)
    }

    @Test
    fun `relative digit appends and trims leading zeros`() {
        assertEquals("", QuickDateInputLogic.appendRelativeDigit("", 0))
        assertEquals("3", QuickDateInputLogic.appendRelativeDigit("", 3))
        assertEquals("30", QuickDateInputLogic.appendRelativeDigit("3", 0))
        assertEquals("7", QuickDateInputLogic.appendRelativeDigit("0", 7))
    }

    @Test
    fun `relative digit caps at four digits`() {
        assertEquals("1234", QuickDateInputLogic.appendRelativeDigit("123", 4))
        assertEquals("1234", QuickDateInputLogic.appendRelativeDigit("1234", 5))
    }

    @Test
    fun `relative digit rejects invalid digit`() {
        assertThrows(IllegalArgumentException::class.java) {
            QuickDateInputLogic.appendRelativeDigit("", 10)
        }
    }

    @Test
    fun `relative date adds signed offset`() {
        assertEquals(reference.plusDays(5), QuickDateInputLogic.relativeDate(reference, "5", 1L))
        assertEquals(reference.minusDays(5), QuickDateInputLogic.relativeDate(reference, "5", -1L))
    }

    @Test
    fun `relative date with empty input is today`() {
        assertEquals(reference, QuickDateInputLogic.relativeDate(reference, "", 1L))
        assertEquals(reference, QuickDateInputLogic.relativeDate(reference, "", -1L))
    }

    @Test
    fun `formatForInput german pads day month and year`() {
        assertEquals(
            "05.09.2026",
            QuickDateInputLogic.formatForInput(LocalDate.of(2026, 9, 5), DateDisplayFormat.GERMAN)
        )
        assertEquals(
            "31.12.1999",
            QuickDateInputLogic.formatForInput(LocalDate.of(1999, 12, 31), DateDisplayFormat.GERMAN)
        )
    }

    @Test
    fun `formatForInput international pads year month and day with dashes`() {
        assertEquals(
            "2026-09-05",
            QuickDateInputLogic.formatForInput(LocalDate.of(2026, 9, 5), DateDisplayFormat.INTERNATIONAL)
        )
    }

    @Test
    fun `formatForInput output round trips through parseDate`() {
        val date = LocalDate.of(2024, 2, 29)
        val germanFormatted = QuickDateInputLogic.formatForInput(date, DateDisplayFormat.GERMAN)
        assertEquals(date, QuickDateInputLogic.parseDate(germanFormatted, reference, DateDisplayFormat.GERMAN))

        val internationalFormatted = QuickDateInputLogic.formatForInput(date, DateDisplayFormat.INTERNATIONAL)
        assertEquals(
            date,
            QuickDateInputLogic.parseDate(internationalFormatted, reference, DateDisplayFormat.INTERNATIONAL)
        )
    }
}
