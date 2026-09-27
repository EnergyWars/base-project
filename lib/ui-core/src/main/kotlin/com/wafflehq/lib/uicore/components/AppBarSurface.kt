package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.theme.AppSpacing

object AppBarSurfaceDefaults {
    val tonalElevation: Dp = 3.dp
    val contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
}

@Composable
fun AppBarSurface(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = AppBarSurfaceDefaults.contentPadding,
    content: @Composable () -> Unit,
) {
    Surface(tonalElevation = AppBarSurfaceDefaults.tonalElevation, modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.navigationBarsPadding().padding(contentPadding)) { content() }
    }
}
