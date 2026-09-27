package com.wafflehq.lib.uicore.render

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutStabilityTrackerTest {

    @Test
    fun `is not stable before any value was recorded`() {
        assertFalse(LayoutStabilityTracker(3).isStable)
    }

    @Test
    fun `becomes stable after the same positive value was recorded often enough`() {
        val tracker = LayoutStabilityTracker(3)

        tracker.record(120)
        tracker.record(120)
        assertFalse(tracker.isStable)
        tracker.record(120)

        assertTrue(tracker.isStable)
    }

    @Test
    fun `a changed value restarts the count`() {
        val tracker = LayoutStabilityTracker(2)

        tracker.record(100)
        tracker.record(140)
        assertFalse(tracker.isStable)
        tracker.record(140)

        assertTrue(tracker.isStable)
    }

    @Test
    fun `zero or negative values are never stable`() {
        val tracker = LayoutStabilityTracker(1)

        tracker.record(0)
        assertFalse(tracker.isStable)
        tracker.record(-5)
        assertFalse(tracker.isStable)
    }

    @Test
    fun `an empty layout after a stable one resets stability`() {
        val tracker = LayoutStabilityTracker(1)

        tracker.record(80)
        assertTrue(tracker.isStable)
        tracker.record(0)

        assertFalse(tracker.isStable)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `requires at least one stable frame`() {
        LayoutStabilityTracker(0)
    }
}
