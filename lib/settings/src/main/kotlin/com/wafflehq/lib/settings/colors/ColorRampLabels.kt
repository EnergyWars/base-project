package com.wafflehq.lib.settings.colors

import com.wafflehq.lib.settings.R

val ColorRamp.labelRes: Int
    get() = when (this) {
        ColorRamp.SAPPHIRE -> R.string.appsettings_color_ramp_sapphire
        ColorRamp.AQUAMARINE -> R.string.appsettings_color_ramp_aquamarine
        ColorRamp.AMETHYST -> R.string.appsettings_color_ramp_amethyst
        ColorRamp.EMERALD -> R.string.appsettings_color_ramp_emerald
        ColorRamp.CITRINE -> R.string.appsettings_color_ramp_citrine
        ColorRamp.GARNET -> R.string.appsettings_color_ramp_garnet
        ColorRamp.GRAPHITE -> R.string.appsettings_color_ramp_graphite
    }
