package com.wafflehq.lib.uicore.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableIntStateOf

val LocalFullScreenSubPageDepth = compositionLocalOf<MutableIntState> { mutableIntStateOf(0) }

@Composable
fun FullScreenSubPageEffect() {
    val depth = LocalFullScreenSubPageDepth.current
    DisposableEffect(Unit) {
        depth.intValue += 1
        onDispose { depth.intValue -= 1 }
    }
}
