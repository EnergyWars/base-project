package com.wafflehq.lib.uicore.theme

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InputTextStyleTest {

    @Test
    fun forInputClearsLineHeightAndFontPadding() {
        val style = TextStyle(fontSize = 16.sp, lineHeight = 24.sp).forInput()

        assertEquals(TextUnit.Unspecified, style.lineHeight)
        assertNull(style.lineHeightStyle)
        assertEquals(PlatformTextStyle(includeFontPadding = false), style.platformStyle)
    }

    @Test
    fun forInputKeepsFontSize() {
        assertEquals(16.sp, TextStyle(fontSize = 16.sp).forInput().fontSize)
    }
}
