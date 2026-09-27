package com.wafflehq.lib.quickpicker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalTime

class QuickTimeInputLogicTest {

    @Test
    fun `first digit 0 appends normally`() {
        assertEquals("0", QuickTimeInputLogic.appendDigit("", 0))
    }

    @Test
    fun `first digit 1 appends normally`() {
        assertEquals("1", QuickTimeInputLogic.appendDigit("", 1))
    }

    @Test
    fun `first digit 2 appends normally`() {
        assertEquals("2", QuickTimeInputLogic.appendDigit("", 2))
    }

    @Test
    fun `first digit 3 auto prepends 0`() {
        assertEquals("03", QuickTimeInputLogic.appendDigit("", 3))
    }

    @Test
    fun `first digit 4 auto prepends 0`() {
        assertEquals("04", QuickTimeInputLogic.appendDigit("", 4))
    }

    @Test
    fun `first digit 5 auto prepends 0`() {
        assertEquals("05", QuickTimeInputLogic.appendDigit("", 5))
    }

    @Test
    fun `first digit 6 auto prepends 0`() {
        assertEquals("06", QuickTimeInputLogic.appendDigit("", 6))
    }

    @Test
    fun `first digit 7 auto prepends 0`() {
        assertEquals("07", QuickTimeInputLogic.appendDigit("", 7))
    }

    @Test
    fun `first digit 8 auto prepends 0`() {
        assertEquals("08", QuickTimeInputLogic.appendDigit("", 8))
    }

    @Test
    fun `first digit 9 auto prepends 0`() {
        assertEquals("09", QuickTimeInputLogic.appendDigit("", 9))
    }

    @Test
    fun `second digit appends to first 0`() {
        assertEquals("01", QuickTimeInputLogic.appendDigit("0", 1))
    }

    @Test
    fun `second digit appends to first 1 producing 9 hour`() {
        assertEquals("19", QuickTimeInputLogic.appendDigit("1", 9))
    }

    @Test
    fun `second digit appends to first 2`() {
        assertEquals("23", QuickTimeInputLogic.appendDigit("2", 3))
    }

    @Test
    fun `does not prepend 0 when current is not empty`() {
        assertEquals("08", QuickTimeInputLogic.appendDigit("0", 8))
        assertEquals("18", QuickTimeInputLogic.appendDigit("1", 8))
    }

    @Test
    fun `appends third and fourth digit normally`() {
        assertEquals("083", QuickTimeInputLogic.appendDigit("08", 3))
        assertEquals("0830", QuickTimeInputLogic.appendDigit("083", 0))
    }

    @Test
    fun `appending beyond four digits is rejected`() {
        assertEquals("0830", QuickTimeInputLogic.appendDigit("0830", 5))
    }

    @Test
    fun `digit out of range throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            QuickTimeInputLogic.appendDigit("", -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            QuickTimeInputLogic.appendDigit("", 10)
        }
    }

    @Test
    fun `backspace on empty stays empty`() {
        assertEquals("", QuickTimeInputLogic.backspace(""))
    }

    @Test
    fun `backspace drops last character`() {
        assertEquals("08", QuickTimeInputLogic.backspace("083"))
        assertEquals("", QuickTimeInputLogic.backspace("0"))
    }

    @Test
    fun `backspace after auto prepended digit removes only the typed digit`() {
        val afterAutoPrepend = QuickTimeInputLogic.appendDigit("", 8)
        assertEquals("08", afterAutoPrepend)
        assertEquals("0", QuickTimeInputLogic.backspace(afterAutoPrepend))
    }

    @Test
    fun `parseTime returns null for empty input`() {
        assertNull(QuickTimeInputLogic.parseTime(""))
    }

    @Test
    fun `parseTime with one digit returns time with minutes zero`() {
        assertEquals(LocalTime.of(0, 0), QuickTimeInputLogic.parseTime("0"))
        assertEquals(LocalTime.of(1, 0), QuickTimeInputLogic.parseTime("1"))
        assertEquals(LocalTime.of(2, 0), QuickTimeInputLogic.parseTime("2"))
        assertEquals(LocalTime.of(9, 0), QuickTimeInputLogic.parseTime("9"))
    }

    @Test
    fun `parseTime with two digits returns time with minutes zero`() {
        assertEquals(LocalTime.of(17, 0), QuickTimeInputLogic.parseTime("17"))
        assertEquals(LocalTime.of(0, 0), QuickTimeInputLogic.parseTime("00"))
        assertEquals(LocalTime.of(23, 0), QuickTimeInputLogic.parseTime("23"))
    }

    @Test
    fun `parseTime with two digits returns null for invalid hour`() {
        assertNull(QuickTimeInputLogic.parseTime("24"))
        assertNull(QuickTimeInputLogic.parseTime("99"))
    }

    @Test
    fun `parseTime returns valid time`() {
        assertEquals(LocalTime.of(8, 30), QuickTimeInputLogic.parseTime("0830"))
        assertEquals(LocalTime.of(0, 0), QuickTimeInputLogic.parseTime("0000"))
        assertEquals(LocalTime.of(23, 59), QuickTimeInputLogic.parseTime("2359"))
    }

    @Test
    fun `parseTime returns null for invalid hour`() {
        assertNull(QuickTimeInputLogic.parseTime("2400"))
        assertNull(QuickTimeInputLogic.parseTime("2500"))
        assertNull(QuickTimeInputLogic.parseTime("9999"))
    }

    @Test
    fun `parseTime returns null for invalid minute`() {
        assertNull(QuickTimeInputLogic.parseTime("0060"))
        assertNull(QuickTimeInputLogic.parseTime("0099"))
    }

    @Test
    fun `parseTime returns null for non digit input`() {
        assertNull(QuickTimeInputLogic.parseTime("abcd"))
        assertNull(QuickTimeInputLogic.parseTime("08:0"))
    }

    @Test
    fun `full sequence single digit 8 yields 0800`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 8)
        s = QuickTimeInputLogic.appendDigit(s, 0)
        s = QuickTimeInputLogic.appendDigit(s, 0)
        assertEquals("0800", s)
        assertEquals(LocalTime.of(8, 0), QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `full sequence 7 then 4 5 yields 0745`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 7)
        s = QuickTimeInputLogic.appendDigit(s, 4)
        s = QuickTimeInputLogic.appendDigit(s, 5)
        assertEquals("0745", s)
        assertEquals(LocalTime.of(7, 45), QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `full sequence 9 then 3 0 yields 0930`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 9)
        s = QuickTimeInputLogic.appendDigit(s, 3)
        s = QuickTimeInputLogic.appendDigit(s, 0)
        assertEquals("0930", s)
        assertEquals(LocalTime.of(9, 30), QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `full sequence 1 then 2 3 0 yields 1230`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 1)
        s = QuickTimeInputLogic.appendDigit(s, 2)
        s = QuickTimeInputLogic.appendDigit(s, 3)
        s = QuickTimeInputLogic.appendDigit(s, 0)
        assertEquals("1230", s)
        assertEquals(LocalTime.of(12, 30), QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `full sequence 2 3 5 9 yields 2359`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 2)
        s = QuickTimeInputLogic.appendDigit(s, 3)
        s = QuickTimeInputLogic.appendDigit(s, 5)
        s = QuickTimeInputLogic.appendDigit(s, 9)
        assertEquals("2359", s)
        assertEquals(LocalTime.of(23, 59), QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `invalid hour 2400 detected by parseTime`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 2)
        s = QuickTimeInputLogic.appendDigit(s, 4)
        s = QuickTimeInputLogic.appendDigit(s, 0)
        s = QuickTimeInputLogic.appendDigit(s, 0)
        assertEquals("2400", s)
        assertNull(QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `digit 0 as first does not prepend`() {
        assertEquals("0", QuickTimeInputLogic.appendDigit("", 0))
        val s = QuickTimeInputLogic.appendDigit("", 0)
        assertEquals("00", QuickTimeInputLogic.appendDigit(s, 0))
    }

    @Test
    fun `repeated backspace until empty`() {
        var s = "0830"
        s = QuickTimeInputLogic.backspace(s); assertEquals("083", s)
        s = QuickTimeInputLogic.backspace(s); assertEquals("08", s)
        s = QuickTimeInputLogic.backspace(s); assertEquals("0", s)
        s = QuickTimeInputLogic.backspace(s); assertEquals("", s)
        s = QuickTimeInputLogic.backspace(s); assertEquals("", s)
    }

    @Test
    fun `appendDigit length never exceeds MAX_DIGITS`() {
        var s = ""
        repeat(10) { s = QuickTimeInputLogic.appendDigit(s, 1) }
        assertEquals(QuickTimeInputLogic.MAX_DIGITS, s.length)
    }

    @Test
    fun `parseTime with three digits pads minute tens with zero`() {
        assertEquals(LocalTime.of(16, 40), QuickTimeInputLogic.parseTime("164"))
        assertEquals(LocalTime.of(9, 0), QuickTimeInputLogic.parseTime("090"))
        assertEquals(LocalTime.of(23, 50), QuickTimeInputLogic.parseTime("235"))
        assertEquals(LocalTime.of(0, 30), QuickTimeInputLogic.parseTime("003"))
    }

    @Test
    fun `parseTime with three digits returns null for invalid minute tens`() {
        assertNull(QuickTimeInputLogic.parseTime("166"))
        assertNull(QuickTimeInputLogic.parseTime("169"))
        assertNull(QuickTimeInputLogic.parseTime("248"))
    }

    @Test
    fun `parseTime with three digits returns null for invalid hour`() {
        assertNull(QuickTimeInputLogic.parseTime("243"))
        assertNull(QuickTimeInputLogic.parseTime("993"))
    }

    @Test
    fun `three digit input 164 enables confirm and resolves to 16 40`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 1)
        s = QuickTimeInputLogic.appendDigit(s, 6)
        s = QuickTimeInputLogic.appendDigit(s, 4)
        assertEquals("164", s)
        val parsed = QuickTimeInputLogic.parseTime(s)
        assertEquals(LocalTime.of(16, 40), parsed)
    }

    @Test
    fun `three digit input 083 resolves to 08 30`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 8)
        s = QuickTimeInputLogic.appendDigit(s, 3)
        assertEquals("083", s)
        assertEquals(LocalTime.of(8, 30), QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `two digit input 16 is parseable as 16 00`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 1)
        s = QuickTimeInputLogic.appendDigit(s, 6)
        assertEquals("16", s)
        assertEquals(LocalTime.of(16, 0), QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `two digit input 9 auto prepends 0 and is parseable as 09 00`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 9)
        assertEquals("09", s)
        assertEquals(LocalTime.of(9, 0), QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `canConfirm is true for valid two digit input`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 1)
        s = QuickTimeInputLogic.appendDigit(s, 6)
        val canConfirm = QuickTimeInputLogic.parseTime(s) != null
        assertEquals(true, canConfirm)
        assertEquals(LocalTime.of(16, 0), QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `canConfirm is true for single digit input and resolves to that hour`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 1)
        assertEquals("1", s)
        val canConfirm = QuickTimeInputLogic.parseTime(s) != null
        assertEquals(true, canConfirm)
        assertEquals(LocalTime.of(1, 0), QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `canConfirm is true for three digit input`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 1)
        s = QuickTimeInputLogic.appendDigit(s, 6)
        s = QuickTimeInputLogic.appendDigit(s, 3)
        val canConfirm = QuickTimeInputLogic.parseTime(s) != null
        assertEquals(true, canConfirm)
    }

    @Test
    fun `canConfirm is true for valid four digit input`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 1)
        s = QuickTimeInputLogic.appendDigit(s, 6)
        s = QuickTimeInputLogic.appendDigit(s, 3)
        s = QuickTimeInputLogic.appendDigit(s, 0)
        val canConfirm = QuickTimeInputLogic.parseTime(s) != null
        assertEquals(true, canConfirm)
        assertEquals(LocalTime.of(16, 30), QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `two digit input 00 is parseable as midnight`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 0)
        s = QuickTimeInputLogic.appendDigit(s, 0)
        assertEquals("00", s)
        assertEquals(LocalTime.of(0, 0), QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `two digit input 23 is parseable as 23 00`() {
        var s = ""
        s = QuickTimeInputLogic.appendDigit(s, 2)
        s = QuickTimeInputLogic.appendDigit(s, 3)
        assertEquals("23", s)
        assertEquals(LocalTime.of(23, 0), QuickTimeInputLogic.parseTime(s))
    }

    @Test
    fun `canConfirm is false for invalid two digit hour 24`() {
        val canConfirm = QuickTimeInputLogic.parseTime("24") != null
        assertEquals(false, canConfirm)
    }

    @Test
    fun `ok confirm flow type 16 confirms as 16h 00m`() {
        var rawDigits = ""
        rawDigits = QuickTimeInputLogic.appendDigit(rawDigits, 1)
        rawDigits = QuickTimeInputLogic.appendDigit(rawDigits, 6)
        val canConfirm = QuickTimeInputLogic.parseTime(rawDigits) != null
        val resolvedTime = QuickTimeInputLogic.parseTime(rawDigits)
        assertEquals(true, canConfirm)
        assertEquals(LocalTime.of(16, 0), resolvedTime)
    }

    @Test
    fun `ok confirm flow type 8 auto prepends 0 confirms as 8h 00m`() {
        var rawDigits = ""
        rawDigits = QuickTimeInputLogic.appendDigit(rawDigits, 8)
        assertEquals("08", rawDigits)
        val canConfirm = QuickTimeInputLogic.parseTime(rawDigits) != null
        val resolvedTime = QuickTimeInputLogic.parseTime(rawDigits)
        assertEquals(true, canConfirm)
        assertEquals(LocalTime.of(8, 0), resolvedTime)
    }

    private val noon = LocalTime.NOON

    @Test
    fun `appendRelativeDigit appends digits`() {
        assertEquals("15", QuickTimeInputLogic.appendRelativeDigit("1", 5))
    }

    @Test
    fun `appendRelativeDigit strips leading zeros`() {
        assertEquals("", QuickTimeInputLogic.appendRelativeDigit("", 0))
        assertEquals("5", QuickTimeInputLogic.appendRelativeDigit("0", 5))
    }

    @Test
    fun `appendRelativeDigit ignores digits beyond max length`() {
        assertEquals("1234", QuickTimeInputLogic.appendRelativeDigit("1234", 5))
    }

    @Test
    fun `appendRelativeDigit rejects invalid digit`() {
        assertThrows(IllegalArgumentException::class.java) { QuickTimeInputLogic.appendRelativeDigit("", 10) }
        assertThrows(IllegalArgumentException::class.java) { QuickTimeInputLogic.appendRelativeDigit("", -1) }
    }

    @Test
    fun `relativeTime adds minutes`() {
        assertEquals(LocalTime.of(12, 45), QuickTimeInputLogic.relativeTime(noon, "45", 1L, RelativeTimeUnit.MINUTES))
    }

    @Test
    fun `relativeTime subtracts minutes`() {
        assertEquals(LocalTime.of(11, 30), QuickTimeInputLogic.relativeTime(noon, "30", -1L, RelativeTimeUnit.MINUTES))
    }

    @Test
    fun `relativeTime adds hours`() {
        assertEquals(LocalTime.of(15, 0), QuickTimeInputLogic.relativeTime(noon, "3", 1L, RelativeTimeUnit.HOURS))
    }

    @Test
    fun `relativeTime subtracts hours`() {
        assertEquals(LocalTime.of(10, 0), QuickTimeInputLogic.relativeTime(noon, "2", -1L, RelativeTimeUnit.HOURS))
    }

    @Test
    fun `relativeTime wraps forward past midnight`() {
        assertEquals(LocalTime.of(1, 0), QuickTimeInputLogic.relativeTime(LocalTime.of(23, 0), "2", 1L, RelativeTimeUnit.HOURS))
    }

    @Test
    fun `relativeTime wraps backward past midnight`() {
        assertEquals(LocalTime.of(23, 30), QuickTimeInputLogic.relativeTime(LocalTime.of(0, 15), "45", -1L, RelativeTimeUnit.MINUTES))
    }

    @Test
    fun `relativeTime wraps multiple days`() {
        assertEquals(LocalTime.of(12, 0), QuickTimeInputLogic.relativeTime(noon, "48", 1L, RelativeTimeUnit.HOURS))
        assertEquals(LocalTime.of(10, 39), QuickTimeInputLogic.relativeTime(noon, "9999", 1L, RelativeTimeUnit.MINUTES))
    }

    @Test
    fun `relativeTime with empty amount returns base`() {
        assertEquals(noon, QuickTimeInputLogic.relativeTime(noon, "", 1L, RelativeTimeUnit.HOURS))
        assertEquals(noon, QuickTimeInputLogic.relativeTime(noon, "", -1L, RelativeTimeUnit.MINUTES))
    }

    @Test
    fun `relativeTime keeps seconds of base`() {
        assertEquals(
            LocalTime.of(12, 20, 30),
            QuickTimeInputLogic.relativeTime(LocalTime.of(12, 0, 30), "20", 1L, RelativeTimeUnit.MINUTES)
        )
    }

    @Test
    fun `formatForInput pads hour and minute`() {
        assertEquals("0905", QuickTimeInputLogic.formatForInput(LocalTime.of(9, 5)))
        assertEquals("2359", QuickTimeInputLogic.formatForInput(LocalTime.of(23, 59)))
        assertEquals("0000", QuickTimeInputLogic.formatForInput(LocalTime.MIDNIGHT))
    }
}
