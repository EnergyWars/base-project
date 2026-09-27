package com.wafflehq.lib.uicore.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BitmapSlicesTest {

    @Test
    fun `content smaller than a slice produces one slice`() {
        assertEquals(listOf(VerticalSlice(top = 0, height = 300)), verticalSlices(300, 1024))
    }

    @Test
    fun `content exactly one slice high produces one slice`() {
        assertEquals(listOf(VerticalSlice(top = 0, height = 1024)), verticalSlices(1024, 1024))
    }

    @Test
    fun `taller content is split with a shorter last slice`() {
        assertEquals(
            listOf(
                VerticalSlice(top = 0, height = 1000),
                VerticalSlice(top = 1000, height = 1000),
                VerticalSlice(top = 2000, height = 500)
            ),
            verticalSlices(2500, 1000)
        )
    }

    @Test
    fun `slices cover the whole height without gaps`() {
        val slices = verticalSlices(3333, 700)

        assertEquals(3333, slices.sumOf { it.height })
        slices.zipWithNext().forEach { (current, next) -> assertEquals(current.top + current.height, next.top) }
    }

    @Test
    fun `empty content produces no slices`() {
        assertTrue(verticalSlices(0, 100).isEmpty())
        assertTrue(verticalSlices(-10, 100).isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `requires a positive slice height`() {
        verticalSlices(100, 0)
    }
}
