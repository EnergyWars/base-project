package com.wafflehq.lib.uicore.button

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppButtonWarningColors(
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

val LocalAppButtonWarningColors = staticCompositionLocalOf {
    AppButtonWarningColors(
        warning = Color(0xFF8B4A00),
        onWarning = Color.White,
        warningContainer = Color(0xFFFFDFA8),
        onWarningContainer = Color(0xFF1F1000),
    )
}
