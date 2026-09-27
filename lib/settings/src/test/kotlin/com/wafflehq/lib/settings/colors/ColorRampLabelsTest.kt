package com.wafflehq.lib.settings.colors

import org.junit.Assert.assertEquals
import org.junit.Test

class ColorRampLabelsTest {

    @Test
    fun `every ramp has a distinct label resource`() {
        val labelResIds = ColorRamp.entries.map { it.labelRes }
        assertEquals(ColorRamp.entries.size, labelResIds.distinct().size)
    }
}
