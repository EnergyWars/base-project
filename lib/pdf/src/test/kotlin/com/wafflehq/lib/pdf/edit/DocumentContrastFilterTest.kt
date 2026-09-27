package com.wafflehq.lib.pdf.edit

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentContrastFilterTest {

    private val paper = rgb(222, 200, 140)
    private val ink = rgb(40, 35, 30)
    private val white = 0xFFFFFFFF.toInt()

    private fun rgb(r: Int, g: Int, b: Int) = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    private fun red(pixel: Int) = (pixel shr 16) and 0xFF

    private fun stripes(width: Int, height: Int, period: Int = 12, thickness: Int = 3, paperAt: (Int) -> Int = { paper }) =
        IntArray(width * height) { i ->
            val x = i % width
            if (x % period < thickness) ink else paperAt(x)
        }

    private fun isInkColumn(x: Int, period: Int = 12, thickness: Int = 3) = x % period < thickness

    @Test
    fun `uniform yellowed paper becomes pure white`() {
        val pixels = IntArray(64 * 64) { paper }

        DocumentContrastFilter.apply(pixels, 64, 64)

        assertTrue(pixels.all { it == white })
    }

    @Test
    fun `dark text turns black and the paper white`() {
        val width = 120
        val pixels = stripes(width, 120)

        DocumentContrastFilter.apply(pixels, width, 120)

        for (y in 0 until 120) {
            for (x in 0 until width) {
                val value = red(pixels[y * width + x])
                if (isInkColumn(x)) assertTrue("ink at $x,$y was $value", value < 20) else assertEquals(255, value)
            }
        }
    }

    @Test
    fun `output is opaque grayscale`() {
        val pixels = stripes(96, 96)

        DocumentContrastFilter.apply(pixels, 96, 96)

        assertTrue(
            pixels.all { pixel ->
                val level = pixel and 0xFF
                (pixel ushr 24) == 0xFF && ((pixel shr 8) and 0xFF) == level && ((pixel shr 16) and 0xFF) == level
            }
        )
    }

    @Test
    fun `uneven illumination is flattened while the text stays dark`() {
        val width = 200
        val height = 100
        val pixels = stripes(width, height) { x ->
            val shade = 230 - x * 100 / width
            rgb(shade, shade * 9 / 10, shade * 6 / 10)
        }

        DocumentContrastFilter.apply(pixels, width, height)

        for (x in 0 until width) {
            val value = red(pixels[50 * width + x])
            if (isInkColumn(x)) assertTrue("ink at $x was $value", value < 80) else assertTrue("paper at $x was $value", value > 235)
        }
    }

    @Test
    fun `a large dark area keeps its center`() {
        val size = 300
        val pixels = IntArray(size * size) { i ->
            val x = i % size
            val y = i / size
            if (x in 132 until 168 && y in 132 until 168) rgb(30, 30, 30) else rgb(210, 210, 210)
        }

        DocumentContrastFilter.apply(pixels, size, size)

        assertTrue(red(pixels[150 * size + 150]) < 64)
        assertEquals(255, red(pixels[20 * size + 20]))
    }

    @Test
    fun `a dark image without paper stays dark`() {
        val pixels = IntArray(48 * 48) { rgb(50, 50, 50) }

        DocumentContrastFilter.apply(pixels, 48, 48)

        assertTrue(pixels.all { red(it) < 40 })
    }

    @Test
    fun `applying the filter twice changes nothing`() {
        val pixels = stripes(120, 120)
        DocumentContrastFilter.apply(pixels, 120, 120)
        val once = pixels.copyOf()

        DocumentContrastFilter.apply(pixels, 120, 120)

        assertArrayEquals(once, pixels)
    }

    @Test
    fun `an image smaller than one tile is processed`() {
        val pixels = IntArray(5 * 3) { paper }

        DocumentContrastFilter.apply(pixels, 5, 3)

        assertTrue(pixels.all { it == white })
    }

    @Test
    fun `an empty image is accepted`() {
        DocumentContrastFilter.apply(IntArray(0), 0, 0)
    }

    @Test
    fun `a pixel buffer that does not match the size is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { DocumentContrastFilter.apply(IntArray(10), 4, 4) }
    }

    @Test
    fun `a non positive tile size is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { DocumentContrastFilter.apply(IntArray(16), 4, 4, tileSize = 0) }
    }

    @Test
    fun `tile size follows the shorter side within its limits`() {
        assertEquals(8, DocumentContrastFilter.tileSizeFor(100, 100))
        assertEquals(33, DocumentContrastFilter.tileSizeFor(1000, 800))
        assertEquals(33, DocumentContrastFilter.tileSizeFor(800, 1000))
        assertEquals(96, DocumentContrastFilter.tileSizeFor(5000, 5000))
    }

    @Test
    fun `percentileLevel picks the level that reaches the requested share`() {
        val histogram = IntArray(256).apply {
            this[10] = 50
            this[200] = 50
        }

        assertEquals(10, DocumentContrastFilter.percentileLevel(histogram, 100, 0.5))
        assertEquals(200, DocumentContrastFilter.percentileLevel(histogram, 100, 0.51))
        assertEquals(200, DocumentContrastFilter.percentileLevel(histogram, 100, 1.0))
    }

    @Test
    fun `percentileLevel falls back to the last level for an empty histogram`() {
        assertEquals(255, DocumentContrastFilter.percentileLevel(IntArray(256), 0, 0.9))
    }
}
