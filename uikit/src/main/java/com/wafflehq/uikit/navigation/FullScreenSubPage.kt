package com.wafflehq.uikit.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.unit.dp

val LocalFullScreenSubPageDepth = compositionLocalOf<MutableIntState> { mutableIntStateOf(0) }

@Composable
fun FullScreenSubPageEffect() {
    val depth = LocalFullScreenSubPageDepth.current
    DisposableEffect(Unit) {
        depth.intValue += 1
        onDispose { depth.intValue -= 1 }
    }
}

fun appContentPadding(isFullScreenSubPage: Boolean, innerPadding: PaddingValues): PaddingValues =
    if (isFullScreenSubPage) PaddingValues(0.dp) else innerPadding
