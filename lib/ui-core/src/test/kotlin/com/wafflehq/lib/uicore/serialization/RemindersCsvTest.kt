package com.wafflehq.lib.uicore.serialization

import org.junit.Assert.assertEquals
import org.junit.Test

class RemindersCsvTest {

    @Test
    fun nullAndBlankYieldEmptyList() {
        assertEquals(emptyList<Int>(), parseRemindersCsv(null))
        assertEquals(emptyList<Int>(), parseRemindersCsv(""))
        assertEquals(emptyList<Int>(), parseRemindersCsv("   "))
    }

    @Test
    fun parsesCommaSeparatedMinutesInOrder() {
        assertEquals(listOf(10, 60, 1440), parseRemindersCsv("10,60,1440"))
    }

    @Test
    fun trimsWhitespaceAroundEntries() {
        assertEquals(listOf(5, 15), parseRemindersCsv(" 5 , 15 "))
    }

    @Test
    fun dropsNonNumericZeroAndNegativeEntries() {
        assertEquals(listOf(30), parseRemindersCsv("abc,0,-5,30,,1.5"))
    }
}
