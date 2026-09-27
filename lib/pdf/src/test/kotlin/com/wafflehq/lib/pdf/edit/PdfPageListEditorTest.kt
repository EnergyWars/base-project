package com.wafflehq.lib.pdf.edit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

class PdfPageListEditorTest {

    private fun page(id: Long, rotation: Int = 0) =
        PdfEditPage(id, PdfPageContent.Image(File("page-$id.jpg")), rotation)

    private val pages = listOf(page(1), page(2), page(3))

    @Test
    fun `normalizeRotation wraps positive and negative values into a full turn`() {
        assertEquals(0, PdfPageListEditor.normalizeRotation(0))
        assertEquals(0, PdfPageListEditor.normalizeRotation(360))
        assertEquals(90, PdfPageListEditor.normalizeRotation(450))
        assertEquals(270, PdfPageListEditor.normalizeRotation(-90))
        assertEquals(180, PdfPageListEditor.normalizeRotation(-540))
    }

    @Test
    fun `rotateClockwise adds a quarter turn to the matching page only`() {
        val result = PdfPageListEditor.rotateClockwise(pages, 2)

        assertEquals(listOf(0, 90, 0), result.map { it.rotationDegrees })
    }

    @Test
    fun `rotateCounterClockwise from zero wraps to 270`() {
        val result = PdfPageListEditor.rotateCounterClockwise(pages, 1)

        assertEquals(270, result.first().rotationDegrees)
    }

    @Test
    fun `four clockwise rotations return to the original orientation`() {
        var result = pages
        repeat(4) { result = PdfPageListEditor.rotateClockwise(result, 3) }

        assertEquals(0, result.last().rotationDegrees)
    }

    @Test
    fun `rotate rejects deltas that are not multiples of 90`() {
        assertThrows(IllegalArgumentException::class.java) { PdfPageListEditor.rotate(pages, 1, 45) }
    }

    @Test
    fun `rotate with unknown id leaves all pages unchanged`() {
        assertEquals(pages, PdfPageListEditor.rotateClockwise(pages, 99))
    }

    @Test
    fun `setContrastEnhanced changes only the matching page`() {
        val result = PdfPageListEditor.setContrastEnhanced(pages, 2, true)

        assertEquals(listOf(false, true, false), result.map { it.enhanceContrast })
    }

    @Test
    fun `setContrastEnhanced can switch the enhancement off again`() {
        val enabled = PdfPageListEditor.setContrastEnhanced(pages, 2, true)

        val result = PdfPageListEditor.setContrastEnhanced(enabled, 2, false)

        assertEquals(pages, result)
    }

    @Test
    fun `setContrastEnhanced with unknown id leaves all pages unchanged`() {
        assertEquals(pages, PdfPageListEditor.setContrastEnhanced(pages, 99, true))
    }

    @Test
    fun `setContrastEnhancedForAll switches every page`() {
        assertEquals(listOf(true, true, true), PdfPageListEditor.setContrastEnhancedForAll(pages, true).map { it.enhanceContrast })
        assertEquals(pages, PdfPageListEditor.setContrastEnhancedForAll(PdfPageListEditor.setContrastEnhancedForAll(pages, true), false))
    }

    @Test
    fun `setContrastEnhancedForAll on an empty list stays empty`() {
        assertEquals(emptyList<PdfEditPage>(), PdfPageListEditor.setContrastEnhancedForAll(emptyList(), true))
    }

    @Test
    fun `duplicate keeps the contrast enhancement`() {
        val enhanced = PdfPageListEditor.setContrastEnhanced(pages, 1, true)

        val result = PdfPageListEditor.duplicate(enhanced, 1, newId = 9)

        assertEquals(listOf(true, true, false, false), result.map { it.enhanceContrast })
    }

    @Test
    fun `move relocates a page to the target index`() {
        assertEquals(listOf(2L, 3L, 1L), PdfPageListEditor.move(pages, 0, 2).map { it.id })
        assertEquals(listOf(3L, 1L, 2L), PdfPageListEditor.move(pages, 2, 0).map { it.id })
    }

    @Test
    fun `move with identical or out of range indices returns the same list`() {
        assertSame(pages, PdfPageListEditor.move(pages, 1, 1))
        assertSame(pages, PdfPageListEditor.move(pages, -1, 1))
        assertSame(pages, PdfPageListEditor.move(pages, 0, 3))
    }

    @Test
    fun `moveBy shifts a page and clamps at both ends`() {
        assertEquals(listOf(2L, 1L, 3L), PdfPageListEditor.moveBy(pages, 1, 1).map { it.id })
        assertEquals(listOf(3L, 1L, 2L), PdfPageListEditor.moveBy(pages, 3, -5).map { it.id })
        assertEquals(listOf(1L, 3L, 2L), PdfPageListEditor.moveBy(pages, 2, 5).map { it.id })
    }

    @Test
    fun `moveBy with unknown id returns the same list`() {
        assertSame(pages, PdfPageListEditor.moveBy(pages, 42, 1))
    }

    @Test
    fun `reorderByIds applies a valid permutation`() {
        val result = PdfPageListEditor.reorderByIds(pages, listOf(3, 1, 2))

        assertEquals(listOf(3L, 1L, 2L), result.map { it.id })
    }

    @Test
    fun `reorderByIds ignores mismatching id sets`() {
        assertSame(pages, PdfPageListEditor.reorderByIds(pages, listOf(1, 2)))
        assertSame(pages, PdfPageListEditor.reorderByIds(pages, listOf(1, 2, 4)))
        assertSame(pages, PdfPageListEditor.reorderByIds(pages, listOf(1, 1, 2)))
    }

    @Test
    fun `remove drops only the matching page`() {
        assertEquals(listOf(1L, 3L), PdfPageListEditor.remove(pages, 2).map { it.id })
        assertEquals(pages, PdfPageListEditor.remove(pages, 42))
    }

    @Test
    fun `duplicate inserts a copy with the new id directly after the original and keeps rotation`() {
        val rotated = listOf(page(1), page(2, rotation = 90), page(3))

        val result = PdfPageListEditor.duplicate(rotated, 2, newId = 10)

        assertEquals(listOf(1L, 2L, 10L, 3L), result.map { it.id })
        assertEquals(90, result[2].rotationDegrees)
        assertEquals(result[1].content, result[2].content)
    }

    @Test
    fun `duplicate with unknown id returns the same list`() {
        assertSame(pages, PdfPageListEditor.duplicate(pages, 42, newId = 10))
    }
}
