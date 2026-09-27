package com.wafflehq.lib.settings.colors

import com.wafflehq.lib.uicore.serialization.enumFromNameOrNull

private const val PALETTE_PREFIX = "P"
private const val CUSTOM_PREFIX = "C"
private const val SEPARATOR = "|"

object ColorValueCodec {

    fun encode(value: ColorValue): String = when (value) {
        is ColorValue.Palette ->
            "$PALETTE_PREFIX$SEPARATOR${value.ramp.name}$SEPARATOR${value.step}$SEPARATOR${value.alpha}"
        is ColorValue.Custom -> "$CUSTOM_PREFIX$SEPARATOR${value.argb}"
    }

    fun decode(raw: String): ColorValue? {
        val parts = raw.split(SEPARATOR)
        return when (parts.getOrNull(0)) {
            PALETTE_PREFIX -> {
                val ramp = enumFromNameOrNull<ColorRamp>(parts.getOrNull(1))
                val step = parts.getOrNull(2)?.toIntOrNull()
                val alpha = parts.getOrNull(3)?.toFloatOrNull()?.coerceIn(0f, 1f) ?: 1f
                if (ramp != null && step != null) ColorValue.paletteOrNull(ramp, step, alpha) else null
            }
            CUSTOM_PREFIX -> parts.getOrNull(1)?.toIntOrNull()?.let { ColorValue.Custom(it) }
            else -> null
        }
    }
}
