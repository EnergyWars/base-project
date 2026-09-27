package com.wafflehq.lib.pdf.edit

object PdfPageListEditor {

    private const val FULL_TURN = 360
    private const val QUARTER_TURN = 90

    fun normalizeRotation(degrees: Int): Int = ((degrees % FULL_TURN) + FULL_TURN) % FULL_TURN

    fun rotateClockwise(pages: List<PdfEditPage>, id: Long): List<PdfEditPage> = rotate(pages, id, QUARTER_TURN)

    fun rotateCounterClockwise(pages: List<PdfEditPage>, id: Long): List<PdfEditPage> = rotate(pages, id, -QUARTER_TURN)

    fun rotate(pages: List<PdfEditPage>, id: Long, deltaDegrees: Int): List<PdfEditPage> {
        require(deltaDegrees % QUARTER_TURN == 0) { "Rotation must be a multiple of $QUARTER_TURN degrees" }
        return pages.map { page ->
            if (page.id == id) page.copy(rotationDegrees = normalizeRotation(page.rotationDegrees + deltaDegrees)) else page
        }
    }

    fun setContrastEnhanced(pages: List<PdfEditPage>, id: Long, enabled: Boolean): List<PdfEditPage> =
        pages.map { page -> if (page.id == id) page.copy(enhanceContrast = enabled) else page }

    fun setContrastEnhancedForAll(pages: List<PdfEditPage>, enabled: Boolean): List<PdfEditPage> =
        pages.map { page -> page.copy(enhanceContrast = enabled) }

    fun move(pages: List<PdfEditPage>, fromIndex: Int, toIndex: Int): List<PdfEditPage> {
        if (fromIndex !in pages.indices || toIndex !in pages.indices || fromIndex == toIndex) return pages
        return pages.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }

    fun moveBy(pages: List<PdfEditPage>, id: Long, delta: Int): List<PdfEditPage> {
        val from = pages.indexOfFirst { it.id == id }
        if (from < 0) return pages
        return move(pages, from, (from + delta).coerceIn(0, pages.lastIndex))
    }

    fun reorderByIds(pages: List<PdfEditPage>, orderedIds: List<Long>): List<PdfEditPage> {
        if (orderedIds.size != pages.size || orderedIds.toSet() != pages.mapTo(HashSet()) { it.id }) return pages
        val byId = pages.associateBy { it.id }
        return orderedIds.map { byId.getValue(it) }
    }

    fun remove(pages: List<PdfEditPage>, id: Long): List<PdfEditPage> = pages.filterNot { it.id == id }

    fun duplicate(pages: List<PdfEditPage>, id: Long, newId: Long): List<PdfEditPage> {
        val index = pages.indexOfFirst { it.id == id }
        if (index < 0) return pages
        return pages.toMutableList().apply { add(index + 1, pages[index].copy(id = newId)) }
    }
}
