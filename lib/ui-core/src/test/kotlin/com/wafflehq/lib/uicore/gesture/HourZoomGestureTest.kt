package com.wafflehq.lib.uicore.gesture

import org.junit.Assert.assertEquals
import org.junit.Test

class HourZoomGestureTest {

    private val step = 0.2f
    private val min = 0.6f
    private val max = 2.4f

    @Test
    fun `snapZoomUp from aligned value advances by one step`() {
        assertEquals(1.2f, snapZoomUp(1.0f, step, min, max), 0.001f)
    }

    @Test
    fun `snapZoomUp from misaligned value jumps to next step above`() {
        assertEquals(1.4f, snapZoomUp(1.37f, step, min, max), 0.001f)
    }

    @Test
    fun `snapZoomUp clamps to maximum`() {
        assertEquals(max, snapZoomUp(2.35f, step, min, max), 0.001f)
    }

    @Test
    fun `snapZoomDown from aligned value retreats by one step`() {
        assertEquals(0.8f, snapZoomDown(1.0f, step, min, max), 0.001f)
    }

    @Test
    fun `snapZoomDown from misaligned value jumps to next step below`() {
        assertEquals(1.2f, snapZoomDown(1.37f, step, min, max), 0.001f)
    }

    @Test
    fun `snapZoomDown clamps to minimum`() {
        assertEquals(min, snapZoomDown(0.65f, step, min, max), 0.001f)
    }
}
