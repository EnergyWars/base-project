package com.wafflehq.lib.uicore.theme

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit

private val InputPlatformTextStyle = PlatformTextStyle(includeFontPadding = false)

fun TextStyle.forInput(): TextStyle = copy(
    lineHeight = TextUnit.Unspecified,
    lineHeightStyle = null,
    platformStyle = InputPlatformTextStyle
)
