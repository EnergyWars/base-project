package com.wafflehq.lib.navigation.settings

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

@Immutable
class SettingsTypography(
    val labelFontFamily: FontFamily = FontFamily.Monospace,
    val inputStyle: (TextStyle) -> TextStyle = { it }
)

val LocalSettingsTypography = staticCompositionLocalOf { SettingsTypography() }
