package com.wafflehq.uikit.pdf

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

object PdfColors {
    const val SUBTLE: Int = 0xFF666666.toInt()
    const val GRID: Int = 0xFFDDDDDD.toInt()
}

object PdfPaints {
    fun title(sizeSp: Float = 20f) = Paint().apply {
        color = Color.BLACK; textSize = sizeSp; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true
    }

    fun subtitle(sizeSp: Float = 10f) = Paint().apply {
        color = PdfColors.SUBTLE; textSize = sizeSp; isAntiAlias = true
    }

    fun heading(sizeSp: Float = 13f) = Paint().apply {
        color = Color.BLACK; textSize = sizeSp; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true
    }

    fun body(sizeSp: Float = 11f) = Paint().apply {
        color = Color.BLACK; textSize = sizeSp; isAntiAlias = true
    }

    fun subtleBody(sizeSp: Float = 9f) = Paint().apply {
        color = PdfColors.SUBTLE; textSize = sizeSp; isAntiAlias = true
    }

    fun divider() = Paint().apply { color = PdfColors.GRID; strokeWidth = 0.5f }
}

fun PdfPageState.drawTitleBlock(title: String, subtitle: String) {
    val titlePaint = PdfPaints.title()
    canvas.drawText(title, PdfExportUtils.MARGIN, y + titlePaint.textSize, titlePaint)
    y += titlePaint.textSize + 6f
    val subPaint = PdfPaints.subtitle()
    canvas.drawText(subtitle, PdfExportUtils.MARGIN, y + subPaint.textSize, subPaint)
    y += subPaint.textSize + 14f
}

fun PdfPageState.drawSectionHeading(text: String) {
    val headingPaint = PdfPaints.heading()
    ensureSpace(headingPaint.textSize + 8f)
    canvas.drawText(text, PdfExportUtils.MARGIN, y + headingPaint.textSize, headingPaint)
    y += headingPaint.textSize + 6f
}

fun PdfPageState.drawDivider() {
    canvas.drawLine(
        PdfExportUtils.MARGIN, y,
        PdfExportUtils.PAGE_WIDTH - PdfExportUtils.MARGIN, y,
        PdfPaints.divider(),
    )
}

fun truncatePdfText(text: String, paint: Paint, maxWidth: Float): String {
    if (paint.measureText(text) <= maxWidth) return text
    var s = text
    while (s.isNotEmpty() && paint.measureText("$s…") > maxWidth) {
        s = s.dropLast(1)
    }
    return "$s…"
}

fun wrapPdfText(text: String, paint: Paint, maxWidth: Float): List<String> {
    val lines = mutableListOf<String>()
    text.split("\n").forEach { paragraph ->
        if (paragraph.isEmpty()) {
            lines += ""
            return@forEach
        }
        var remaining = paragraph
        while (remaining.isNotEmpty()) {
            var count = paint.breakText(remaining, true, maxWidth, null)
            if (count < remaining.length) {
                val lastSpace = remaining.lastIndexOf(' ', count - 1)
                if (lastSpace > 0) count = lastSpace
            }
            if (count <= 0) count = 1
            lines += remaining.substring(0, count).trimEnd()
            remaining = remaining.substring(count).trimStart()
        }
    }
    return lines
}

fun PdfPageState.drawWrappedText(
    text: String,
    paint: Paint,
    maxWidth: Float = pageWidth - 2 * margin,
    lineSpacing: Float = 2f,
) {
    val lineHeight = paint.textSize + lineSpacing
    wrapPdfText(text, paint, maxWidth).forEach { line ->
        ensureSpace(lineHeight)
        canvas.drawText(line, margin, y + paint.textSize, paint)
        y += lineHeight
    }
}
