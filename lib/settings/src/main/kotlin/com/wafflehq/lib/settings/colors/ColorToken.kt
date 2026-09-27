package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.Color

data class ColorToken(
    val id: ColorTokenId,
    val categoryKey: String,
    val labelRes: Int,
    val descriptionRes: Int,
    val defaultLight: Color,
    val defaultDark: Color,
    val anchorRamp: ColorRamp? = null
)
