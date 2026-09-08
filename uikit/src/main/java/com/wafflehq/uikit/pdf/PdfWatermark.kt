package com.wafflehq.uikit.pdf

import android.graphics.Canvas
import android.graphics.Paint

object PdfWatermark {

    private const val TEXT_SIZE = 8f

    fun draw(canvas: Canvas, text: String, pageWidth: Int, pageHeight: Int, margin: Float) {
        if (text.isEmpty()) return
        val paint = Paint().apply {
            color = PdfColors.SUBTLE
            textSize = TEXT_SIZE
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(text, pageWidth - margin, pageHeight - margin / 2f, paint)
    }
}
