package com.wafflehq.lib.uicore.color

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

enum class ContrastLevel { AAA, AA, LOW }

private fun linearizeChannel(channel: Float): Double {
    val c = channel.toDouble()
    return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
}

fun Color.relativeLuminance(): Double {
    val r = linearizeChannel(red)
    val g = linearizeChannel(green)
    val b = linearizeChannel(blue)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

fun contrastRatio(a: Color, b: Color): Double {
    val lighter = max(a.relativeLuminance(), b.relativeLuminance())
    val darker = min(a.relativeLuminance(), b.relativeLuminance())
    return (lighter + 0.05) / (darker + 0.05)
}

fun contrastLevelForNormalText(ratio: Double): ContrastLevel = when {
    ratio >= 7.0 -> ContrastLevel.AAA
    ratio >= 4.5 -> ContrastLevel.AA
    else -> ContrastLevel.LOW
}

fun Color.contrastTextColor(): Color {
    val luminance = 0.299f * red + 0.587f * green + 0.114f * blue
    return if (luminance > 0.5f) Color(0xFF000000) else Color(0xFFFFFFFF)
}
