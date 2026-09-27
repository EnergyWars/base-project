package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.Color
import com.wafflehq.lib.settings.R

internal object TestColorTokens {

    const val ALPHA = "alpha"
    const val BETA = "beta"

    val alphaAccent = ColorTokenId("alpha.accent")
    val alphaOnAccent = ColorTokenId("alpha.onAccent")
    val betaAccent = ColorTokenId("beta.accent")

    val registry = ColorTokenRegistry(
        categories = listOf(
            ColorCategoryDescriptor(
                key = ALPHA,
                labelRes = R.string.appsettings_color_ramp_sapphire,
                descriptionRes = R.string.appsettings_color_mode_light,
                alwaysVisible = true
            ),
            ColorCategoryDescriptor(
                key = BETA,
                labelRes = R.string.appsettings_color_ramp_garnet,
                descriptionRes = R.string.appsettings_color_mode_dark
            )
        ),
        all = listOf(
            ColorToken(
                id = alphaAccent,
                categoryKey = ALPHA,
                labelRes = R.string.appsettings_color_ramp_emerald,
                descriptionRes = R.string.appsettings_color_view_mode_simplified,
                defaultLight = Sapphire40,
                defaultDark = Sapphire80
            ),
            ColorToken(
                id = alphaOnAccent,
                categoryKey = ALPHA,
                labelRes = R.string.appsettings_color_ramp_citrine,
                descriptionRes = R.string.appsettings_color_view_mode_advanced,
                defaultLight = Color.White,
                defaultDark = Color.Black
            ),
            ColorToken(
                id = betaAccent,
                categoryKey = BETA,
                labelRes = R.string.appsettings_color_ramp_amethyst,
                descriptionRes = R.string.appsettings_color_simplified_reset_ramp,
                defaultLight = Garnet40,
                defaultDark = Garnet80
            )
        )
    )
}
