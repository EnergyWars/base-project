package com.wafflehq.lib.uicore.color

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorContrastTest {

    @Test
    fun `black on white has the maximum contrast ratio of 21`() {
        val ratio = contrastRatio(Color.Black, Color.White)
        assertEquals(21.0, ratio, 0.01)
    }

    @Test
    fun `a color against itself has the minimum contrast ratio of 1`() {
        val color = Color(0xFF3366CC)
        assertEquals(1.0, contrastRatio(color, color), 0.001)
    }

    @Test
    fun `contrast ratio is symmetric regardless of argument order`() {
        val a = Color(0xFF224466)
        val b = Color(0xFFEEEEEE)
        assertEquals(contrastRatio(a, b), contrastRatio(b, a), 0.0001)
    }

    @Test
    fun `white text on mid gray meets AA but not AAA`() {
        val ratio = contrastRatio(Color.White, Color(0xFF767676))
        assertTrue("expected ratio >= 4.5, was $ratio", ratio >= 4.5)
        assertTrue("expected ratio < 7.0, was $ratio", ratio < 7.0)
        assertEquals(ContrastLevel.AA, contrastLevelForNormalText(ratio))
    }

    @Test
    fun `black on white is classified as AAA`() {
        assertEquals(ContrastLevel.AAA, contrastLevelForNormalText(contrastRatio(Color.Black, Color.White)))
    }

    @Test
    fun `two similar mid tones are classified as low contrast`() {
        val ratio = contrastRatio(Color(0xFF808080), Color(0xFF8A8A8A))
        assertEquals(ContrastLevel.LOW, contrastLevelForNormalText(ratio))
    }

    @Test
    fun `contrastLevelForNormalText boundary at 4point5 is AA`() {
        assertEquals(ContrastLevel.AA, contrastLevelForNormalText(4.5))
        assertEquals(ContrastLevel.LOW, contrastLevelForNormalText(4.4999))
    }

    @Test
    fun `contrastLevelForNormalText boundary at 7 is AAA`() {
        assertEquals(ContrastLevel.AAA, contrastLevelForNormalText(7.0))
        assertEquals(ContrastLevel.AA, contrastLevelForNormalText(6.9999))
    }
}
