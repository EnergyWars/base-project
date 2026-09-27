package com.wafflehq.lib.uicore.render

import org.junit.Assert.assertEquals
import org.junit.Test

class OffscreenRenderSpecTest {

    @Test
    fun `keeps valid dimensions`() {
        val spec = OffscreenRenderSpec(widthPx = 800, maxHeightPx = 3000, densityDpi = 420)

        assertEquals(800, spec.widthPx)
        assertEquals(3000, spec.maxHeightPx)
        assertEquals(420, spec.densityDpi)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects zero width`() {
        OffscreenRenderSpec(widthPx = 0, maxHeightPx = 100, densityDpi = 420)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects zero height`() {
        OffscreenRenderSpec(widthPx = 100, maxHeightPx = 0, densityDpi = 420)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects zero density`() {
        OffscreenRenderSpec(widthPx = 100, maxHeightPx = 100, densityDpi = 0)
    }
}
