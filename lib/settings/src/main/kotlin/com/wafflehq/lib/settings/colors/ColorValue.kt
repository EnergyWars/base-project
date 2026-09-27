package com.wafflehq.lib.settings.colors

sealed interface ColorValue {
    data class Palette(val ramp: ColorRamp, val step: Int, val alpha: Float = 1f) : ColorValue
    data class Custom(val argb: Int) : ColorValue

    companion object {
        fun paletteOrNull(ramp: ColorRamp, step: Int, alpha: Float = 1f): Palette? =
            if (step in ColorRampSteps) Palette(ramp, step, alpha) else null
    }
}
