package com.wafflehq.lib.uicore.gesture

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class TapFlashTest {

    @Test
    fun `an unspecified flash color falls back to the theme color instead of white`() {
        assertEquals(Color.Red, resolveTapFlashColor(Color.Unspecified, Color.Red))
    }

    @Test
    fun `an explicit flash color wins over the theme color`() {
        assertEquals(Color.Blue, resolveTapFlashColor(Color.Blue, Color.Red))
    }

    @Test
    fun `an explicit white flash is still respected`() {
        assertEquals(Color.White, resolveTapFlashColor(Color.White, Color.Red))
    }
}
