package com.wafflehq.lib.quickpicker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class RelativeTimeInputStateTest {

    private fun newState() = RelativeTimeInputState(LocalTime.NOON)

    @Test
    fun `starts inactive with defaults and base as result`() {
        val state = newState()

        assertFalse(state.active)
        assertEquals("", state.amount)
        assertEquals(1L, state.sign)
        assertEquals(RelativeTimeUnit.MINUTES, state.unit)
        assertEquals(LocalTime.NOON, state.result)
    }

    @Test
    fun `first toggle activates and returns null`() {
        val state = newState()

        assertNull(state.toggle())
        assertTrue(state.active)
    }

    @Test
    fun `second toggle deactivates and returns result`() {
        val state = newState()
        state.toggle()
        state.appendDigit(3)
        state.appendDigit(0)

        assertEquals(LocalTime.of(12, 30), state.toggle())
        assertFalse(state.active)
    }

    @Test
    fun `toggle resets amount sign and unit`() {
        val state = newState()
        state.toggle()
        state.appendDigit(5)
        state.flipSign()
        state.selectUnit(RelativeTimeUnit.HOURS)
        state.toggle()
        state.toggle()

        assertEquals("", state.amount)
        assertEquals(1L, state.sign)
        assertEquals(RelativeTimeUnit.MINUTES, state.unit)
    }

    @Test
    fun `flipSign switches direction`() {
        val state = newState()
        state.appendDigit(2)
        state.selectUnit(RelativeTimeUnit.HOURS)
        state.flipSign()

        assertEquals(LocalTime.of(10, 0), state.result)
        state.flipSign()
        assertEquals(LocalTime.of(14, 0), state.result)
    }

    @Test
    fun `backspace removes last digit`() {
        val state = newState()
        state.appendDigit(1)
        state.appendDigit(5)
        state.backspace()

        assertEquals("1", state.amount)
        state.backspace()
        state.backspace()
        assertEquals("", state.amount)
    }

    @Test
    fun `deactivate leaves entered amount untouched`() {
        val state = newState()
        state.toggle()
        state.appendDigit(7)
        state.deactivate()

        assertFalse(state.active)
        assertEquals("7", state.amount)
    }
}
