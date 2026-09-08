package com.wafflehq.uikit.color

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class Rgb(val r: Int, val g: Int, val b: Int)

data class Hsl(val h: Float, val s: Float, val l: Float)

fun Color.toRgb(): Rgb {
    val argb = toArgb()
    return Rgb((argb ushr 16) and 0xFF, (argb ushr 8) and 0xFF, argb and 0xFF)
}

fun Rgb.toColor(alpha: Float = 1f): Color = Color(
    red = r / 255f,
    green = g / 255f,
    blue = b / 255f,
    alpha = alpha,
)

fun rgbToHsl(r: Int, g: Int, b: Int): Hsl {
    val rf = r / 255f
    val gf = g / 255f
    val bf = b / 255f
    val maxC = max(rf, max(gf, bf))
    val minC = min(rf, min(gf, bf))
    val l = (maxC + minC) / 2f
    if (maxC == minC) return Hsl(0f, 0f, l * 100f)

    val delta = maxC - minC
    val s = if (l > 0.5f) delta / (2f - maxC - minC) else delta / (maxC + minC)
    val h = when (maxC) {
        rf -> ((gf - bf) / delta + (if (gf < bf) 6f else 0f))
        gf -> (bf - rf) / delta + 2f
        else -> (rf - gf) / delta + 4f
    } * 60f
    return Hsl(h, s * 100f, l * 100f)
}

fun hslToRgb(h: Float, s: Float, l: Float): Rgb {
    val hNorm = ((h % 360f) + 360f) % 360f / 360f
    val sNorm = (s / 100f).coerceIn(0f, 1f)
    val lNorm = (l / 100f).coerceIn(0f, 1f)

    if (sNorm == 0f) {
        val v = (lNorm * 255f).roundToInt()
        return Rgb(v, v, v)
    }

    fun hueToRgb(p: Float, q: Float, tIn: Float): Float {
        var t = tIn
        if (t < 0f) t += 1f
        if (t > 1f) t -= 1f
        return when {
            t < 1f / 6f -> p + (q - p) * 6f * t
            t < 1f / 2f -> q
            t < 2f / 3f -> p + (q - p) * (2f / 3f - t) * 6f
            else -> p
        }
    }

    val q = if (lNorm < 0.5f) lNorm * (1f + sNorm) else lNorm + sNorm - lNorm * sNorm
    val p = 2f * lNorm - q
    val r = hueToRgb(p, q, hNorm + 1f / 3f)
    val g = hueToRgb(p, q, hNorm)
    val b = hueToRgb(p, q, hNorm - 1f / 3f)
    return Rgb((r * 255f).roundToInt(), (g * 255f).roundToInt(), (b * 255f).roundToInt())
}

fun Color.toHsl(): Hsl = toRgb().let { rgbToHsl(it.r, it.g, it.b) }

fun Hsl.toColor(alpha: Float = 1f): Color = hslToRgb(h, s, l).toColor(alpha)

fun Color.toHexArgb(): String = "#%08X".format(toArgb().toLong() and 0xFFFFFFFFL)

fun hexToColorOrNull(hex: String): Color? {
    val cleaned = hex.removePrefix("#").trim()
    if (cleaned.any { it !in "0123456789abcdefABCDEF" }) return null
    return when (cleaned.length) {
        6 -> cleaned.toLongOrNull(16)?.let { Color((0xFF000000L or it).toInt()) }
        8 -> cleaned.toLongOrNull(16)?.let { Color(it.toInt()) }
        else -> null
    }
}
