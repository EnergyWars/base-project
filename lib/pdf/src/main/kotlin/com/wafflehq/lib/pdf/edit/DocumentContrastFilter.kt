package com.wafflehq.lib.pdf.edit

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

internal object DocumentContrastFilter {

    const val PAPER_PERCENTILE = 0.9
    const val INK_PERCENTILE = 0.01
    const val BACKGROUND_FLOOR = 100f
    const val WHITE_POINT = 229
    const val MAX_BLACK_POINT = 110

    private const val GRID_DIVISOR = 24
    private const val MIN_TILE_SIZE = 8
    private const val MAX_TILE_SIZE = 96
    private const val LEVELS = 256
    private const val MAX_LEVEL = 255
    private const val OPAQUE = 0xFF shl 24
    private const val RED_WEIGHT = 128
    private const val GREEN_WEIGHT = 102
    private const val BLUE_WEIGHT = 26
    private const val WEIGHT_SHIFT = 8
    private const val BYTE_MASK = 0xFF

    fun tileSizeFor(width: Int, height: Int): Int =
        (min(width, height) / GRID_DIVISOR).coerceIn(MIN_TILE_SIZE, MAX_TILE_SIZE)

    fun apply(pixels: IntArray, width: Int, height: Int, tileSize: Int = tileSizeFor(width, height)) {
        require(width >= 0 && height >= 0 && pixels.size == width * height) { "Pixel buffer does not match the size" }
        require(tileSize > 0) { "Tile size must be positive" }
        if (pixels.isEmpty()) return
        val luma = ByteArray(pixels.size)
        for (i in pixels.indices) luma[i] = luminance(pixels[i]).toByte()
        val grid = PaperGrid.estimate(luma, width, height, tileSize)
        val histogram = normalize(luma, width, height, grid)
        val lut = buildLookupTable(histogram, pixels.size)
        for (i in pixels.indices) {
            val value = lut[luma[i].toInt() and BYTE_MASK]
            pixels[i] = OPAQUE or (value shl 16) or (value shl 8) or value
        }
    }

    private fun luminance(pixel: Int): Int {
        val red = (pixel shr 16) and BYTE_MASK
        val green = (pixel shr 8) and BYTE_MASK
        val blue = pixel and BYTE_MASK
        return (red * RED_WEIGHT + green * GREEN_WEIGHT + blue * BLUE_WEIGHT) shr WEIGHT_SHIFT
    }

    private fun normalize(luma: ByteArray, width: Int, height: Int, grid: PaperGrid): IntArray {
        val histogram = IntArray(LEVELS)
        val columns = grid.columnSamples(width)
        for (y in 0 until height) {
            val row = grid.rowSample(y)
            val base = y * width
            for (x in 0 until width) {
                val background = grid.background(row, columns, x)
                val level = ((luma[base + x].toInt() and BYTE_MASK) * MAX_LEVEL / background + 0.5f).toInt()
                    .coerceIn(0, MAX_LEVEL)
                luma[base + x] = level.toByte()
                histogram[level]++
            }
        }
        return histogram
    }

    private fun buildLookupTable(histogram: IntArray, pixelCount: Int): IntArray {
        val blackPoint = percentileLevel(histogram, pixelCount, INK_PERCENTILE).coerceAtMost(MAX_BLACK_POINT)
        val range = (WHITE_POINT - blackPoint).toFloat()
        return IntArray(LEVELS) { level ->
            val t = ((level - blackPoint) / range).coerceIn(0f, 1f)
            (smoothStep(smoothStep(t)) * MAX_LEVEL + 0.5f).toInt()
        }
    }

    private fun smoothStep(t: Float): Float = t * t * (3f - 2f * t)

    internal fun percentileLevel(histogram: IntArray, total: Int, percentile: Double): Int {
        val target = ceil(total * percentile).toInt().coerceAtLeast(1)
        var cumulative = 0
        for (level in histogram.indices) {
            cumulative += histogram[level]
            if (cumulative >= target) return level
        }
        return histogram.lastIndex
    }

    internal class PaperGrid private constructor(
        private val values: FloatArray,
        private val columns: Int,
        private val rows: Int,
        private val tileSize: Int
    ) {

        class Sample(val first: Int, val second: Int, val fraction: Float)

        fun rowSample(y: Int): Sample = sample(y, rows)

        fun columnSamples(width: Int): Array<Sample> = Array(width) { sample(it, columns) }

        fun background(row: Sample, columnSamples: Array<Sample>, x: Int): Float {
            val column = columnSamples[x]
            val top = mix(values[row.first * columns + column.first], values[row.first * columns + column.second], column.fraction)
            val bottom = mix(values[row.second * columns + column.first], values[row.second * columns + column.second], column.fraction)
            return mix(top, bottom, row.fraction)
        }

        private fun sample(position: Int, count: Int): Sample {
            val grid = (position + 0.5f) / tileSize - 0.5f
            val lower = floor(grid).toInt()
            return Sample(lower.coerceIn(0, count - 1), (lower + 1).coerceIn(0, count - 1), grid - lower)
        }

        private fun mix(a: Float, b: Float, fraction: Float): Float = a + (b - a) * fraction

        companion object {

            fun estimate(luma: ByteArray, width: Int, height: Int, tileSize: Int): PaperGrid {
                val columns = (width + tileSize - 1) / tileSize
                val rows = (height + tileSize - 1) / tileSize
                val papers = tilePaperLevels(luma, width, height, tileSize, columns, rows)
                val dilated = filter3x3(papers, columns, rows) { window -> window.max() }
                val smoothed = filter3x3(dilated, columns, rows) { window -> window.average().toFloat() }
                for (i in smoothed.indices) smoothed[i] = smoothed[i].coerceAtLeast(BACKGROUND_FLOOR)
                return PaperGrid(smoothed, columns, rows, tileSize)
            }

            private fun tilePaperLevels(
                luma: ByteArray,
                width: Int,
                height: Int,
                tileSize: Int,
                columns: Int,
                rows: Int
            ): FloatArray {
                val histograms = IntArray(columns * rows * LEVELS)
                val counts = IntArray(columns * rows)
                val columnOf = IntArray(width) { it / tileSize }
                for (y in 0 until height) {
                    val rowBase = (y / tileSize) * columns
                    val base = y * width
                    for (x in 0 until width) {
                        val tile = rowBase + columnOf[x]
                        histograms[tile * LEVELS + (luma[base + x].toInt() and BYTE_MASK)]++
                        counts[tile]++
                    }
                }
                return FloatArray(columns * rows) { tile ->
                    val slice = histograms.copyOfRange(tile * LEVELS, (tile + 1) * LEVELS)
                    percentileLevel(slice, counts[tile], PAPER_PERCENTILE).toFloat()
                }
            }

            private inline fun filter3x3(
                source: FloatArray,
                columns: Int,
                rows: Int,
                reduce: (List<Float>) -> Float
            ): FloatArray {
                val result = FloatArray(source.size)
                val window = ArrayList<Float>(9)
                for (row in 0 until rows) {
                    for (column in 0 until columns) {
                        window.clear()
                        for (dy in -1..1) {
                            val y = row + dy
                            if (y !in 0 until rows) continue
                            for (dx in -1..1) {
                                val x = column + dx
                                if (x in 0 until columns) window.add(source[y * columns + x])
                            }
                        }
                        result[row * columns + column] = reduce(window)
                    }
                }
                return result
            }
        }
    }
}
