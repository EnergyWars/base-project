package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout

@Composable
fun RotatedLandscapeBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(
        content = { Box(Modifier.graphicsLayer { rotationZ = 90f }) { content() } },
        modifier = modifier
    ) { measurables, constraints ->
        val rotatedConstraints = constraints.copy(
            minWidth = constraints.minHeight,
            maxWidth = constraints.maxHeight,
            minHeight = constraints.minWidth,
            maxHeight = constraints.maxWidth
        )
        val placeable = measurables.first().measure(rotatedConstraints)
        layout(placeable.height, placeable.width) {
            val x = (placeable.height - placeable.width) / 2
            val y = (placeable.width - placeable.height) / 2
            placeable.place(x, y)
        }
    }
}
