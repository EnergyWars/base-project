package com.wafflehq.lib.settings.encryption

import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordReminderIntervalTest {

    @Test
    fun `default is never`() {
        assertEquals(PasswordReminderInterval.NEVER, PasswordReminderInterval.fromOrdinal(null))
    }

    @Test
    fun `maps stored ordinals`() {
        PasswordReminderInterval.entries.forEachIndexed { index, interval ->
            assertEquals(interval, PasswordReminderInterval.fromOrdinal(index))
        }
    }

    @Test
    fun `falls back to never for out of range`() {
        assertEquals(PasswordReminderInterval.NEVER, PasswordReminderInterval.fromOrdinal(-1))
        assertEquals(PasswordReminderInterval.NEVER, PasswordReminderInterval.fromOrdinal(99))
    }
}
