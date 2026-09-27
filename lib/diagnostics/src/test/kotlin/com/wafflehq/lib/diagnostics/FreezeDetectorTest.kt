package com.wafflehq.lib.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreezeDetectorTest {

    @Test
    fun evaluate_belowThreshold_reportsNone() {
        val detector = FreezeDetector(thresholdMs = 3000L)

        val transition = detector.evaluate(lastHeartbeatMs = 1000L, nowMs = 2000L)

        assertEquals(FreezeTransition.NONE, transition)
        assertFalse(detector.isFrozen)
    }

    @Test
    fun evaluate_crossingThreshold_reportsStartedOnce() {
        val detector = FreezeDetector(thresholdMs = 3000L)

        val first = detector.evaluate(lastHeartbeatMs = 1000L, nowMs = 4500L)
        val second = detector.evaluate(lastHeartbeatMs = 1000L, nowMs = 5500L)

        assertEquals(FreezeTransition.STARTED, first)
        assertEquals(FreezeTransition.NONE, second)
        assertTrue(detector.isFrozen)
    }

    @Test
    fun evaluate_recoveringAfterFreeze_reportsResolved() {
        val detector = FreezeDetector(thresholdMs = 3000L)
        detector.evaluate(lastHeartbeatMs = 1000L, nowMs = 4500L)

        val transition = detector.evaluate(lastHeartbeatMs = 4600L, nowMs = 4700L)

        assertEquals(FreezeTransition.RESOLVED, transition)
        assertFalse(detector.isFrozen)
    }

    @Test
    fun evaluate_exactlyAtThreshold_countsAsFrozen() {
        val detector = FreezeDetector(thresholdMs = 3000L)

        val transition = detector.evaluate(lastHeartbeatMs = 1000L, nowMs = 4000L)

        assertEquals(FreezeTransition.STARTED, transition)
    }

    @Test
    fun defaultThreshold_isThreeSeconds() {
        val detector = FreezeDetector()

        assertEquals(FreezeTransition.NONE, detector.evaluate(lastHeartbeatMs = 0L, nowMs = 2999L))
        assertEquals(FreezeTransition.STARTED, detector.evaluate(lastHeartbeatMs = 0L, nowMs = 3000L))
    }
}
