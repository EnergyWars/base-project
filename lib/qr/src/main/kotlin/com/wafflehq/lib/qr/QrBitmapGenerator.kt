package com.wafflehq.lib.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

fun generateQrBitmap(text: String, sizePx: Int): Bitmap? = runCatching {
    val matrix = QRCodeWriter().encode(
        text,
        BarcodeFormat.QR_CODE,
        sizePx,
        sizePx,
        mapOf(EncodeHintType.MARGIN to 1)
    )
    val pixels = IntArray(sizePx * sizePx) { i ->
        if (matrix[i % sizePx, i / sizePx]) Color.BLACK else Color.WHITE
    }
    Bitmap.createBitmap(pixels, sizePx, sizePx, Bitmap.Config.ARGB_8888)
}.getOrNull()
