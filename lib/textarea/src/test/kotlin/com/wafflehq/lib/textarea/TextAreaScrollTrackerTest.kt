package com.wafflehq.lib.textarea

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextAreaScrollTrackerTest {

    @Test
    fun gainingFocusChecksTheCursor() {
        val tracker = TextAreaScrollTracker()

        assertTrue(tracker.onFocusChanged(true))
        assertTrue(tracker.isFocused)
    }

    @Test
    fun losingFocusDoesNotCheck() {
        val tracker = TextAreaScrollTracker()
        tracker.onFocusChanged(true)

        assertFalse(tracker.onFocusChanged(false))
        assertFalse(tracker.isFocused)
    }

    @Test
    fun firstLayoutAfterFocusDoesNotCheck() {
        val tracker = TextAreaScrollTracker()
        tracker.onFocusChanged(true)

        assertFalse(tracker.onTextLayout("Hello"))
    }

    @Test
    fun everyTypedCharacterChecks() {
        val tracker = TextAreaScrollTracker()
        tracker.onFocusChanged(true)
        tracker.onTextLayout("")

        assertTrue(tracker.onTextLayout("H"))
        assertTrue(tracker.onTextLayout("He"))
        assertTrue(tracker.onTextLayout("Hel"))
    }

    @Test
    fun remeasureWithUnchangedTextDoesNotCheck() {
        val tracker = TextAreaScrollTracker()
        tracker.onFocusChanged(true)
        tracker.onTextLayout("Hello")
        tracker.onTextLayout("Hello!")

        assertFalse(tracker.onTextLayout("Hello!"))
        assertFalse(tracker.onTextLayout("Hello!"))
    }

    @Test
    fun textChangeWithoutFocusDoesNotCheck() {
        val tracker = TextAreaScrollTracker()
        tracker.onTextLayout("")

        assertFalse(tracker.onTextLayout("Hello"))
    }

    @Test
    fun refocusingDoesNotCheckOnTheFirstLayout() {
        val tracker = TextAreaScrollTracker()
        tracker.onFocusChanged(true)
        tracker.onTextLayout("")
        tracker.onTextLayout("Hello")
        tracker.onFocusChanged(false)
        tracker.onFocusChanged(true)

        assertFalse(tracker.onTextLayout("Hello"))
        assertTrue(tracker.onTextLayout("Hello!"))
    }

    @Test
    fun growingImeChecksWhileFocused() {
        val tracker = TextAreaScrollTracker()
        tracker.onFocusChanged(true)

        assertTrue(tracker.onImeBottomChanged(300))
        assertTrue(tracker.onImeBottomChanged(600))
        assertEquals(600, tracker.imeBottom)
    }

    @Test
    fun growingImeWithoutFocusDoesNotCheck() {
        val tracker = TextAreaScrollTracker()

        assertFalse(tracker.onImeBottomChanged(300))
        assertEquals(300, tracker.imeBottom)
    }

    @Test
    fun shrinkingOrUnchangedImeDoesNotCheck() {
        val tracker = TextAreaScrollTracker()
        tracker.onFocusChanged(true)
        tracker.onImeBottomChanged(600)

        assertFalse(tracker.onImeBottomChanged(600))
        assertFalse(tracker.onImeBottomChanged(200))
        assertFalse(tracker.onImeBottomChanged(0))
    }

    @Test
    fun imeGrowingAgainAfterShrinkChecksAgain() {
        val tracker = TextAreaScrollTracker()
        tracker.onFocusChanged(true)
        tracker.onImeBottomChanged(600)
        tracker.onImeBottomChanged(0)

        assertTrue(tracker.onImeBottomChanged(450))
    }
}
