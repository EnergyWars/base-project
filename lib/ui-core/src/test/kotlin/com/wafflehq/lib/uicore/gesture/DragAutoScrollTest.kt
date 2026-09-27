package com.wafflehq.lib.uicore.gesture

import org.junit.Assert.assertEquals
import org.junit.Test

class DragAutoScrollTest {

    @Test
    fun `middle of the container does not scroll`() {
        assertEquals(0f, dragAutoScrollSpeed(500f, 0f, 1000f, 100f, 10f))
    }

    @Test
    fun `top edge scrolls up proportionally`() {
        assertEquals(-5f, dragAutoScrollSpeed(50f, 0f, 1000f, 100f, 10f))
    }

    @Test
    fun `bottom edge scrolls down proportionally`() {
        assertEquals(5f, dragAutoScrollSpeed(950f, 0f, 1000f, 100f, 10f))
    }

    @Test
    fun `speed is capped beyond the container edges`() {
        assertEquals(-10f, dragAutoScrollSpeed(-300f, 0f, 1000f, 100f, 10f))
        assertEquals(10f, dragAutoScrollSpeed(1400f, 0f, 1000f, 100f, 10f))
    }

    @Test
    fun `container offset is respected`() {
        assertEquals(-5f, dragAutoScrollSpeed(250f, 200f, 800f, 100f, 10f))
        assertEquals(0f, dragAutoScrollSpeed(500f, 200f, 800f, 100f, 10f))
    }

    @Test
    fun `non positive edge disables scrolling`() {
        assertEquals(0f, dragAutoScrollSpeed(0f, 0f, 1000f, 0f, 10f))
        assertEquals(0f, dragAutoScrollSpeed(0f, 0f, 1000f, -5f, 10f))
    }
}
