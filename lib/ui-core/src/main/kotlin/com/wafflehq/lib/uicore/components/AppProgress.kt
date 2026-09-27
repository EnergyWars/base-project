package com.wafflehq.lib.uicore.components

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AppProgressDefaults {
    val circularStrokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth

    val color: Color @Composable get() = MaterialTheme.colorScheme.primary

    val linearTrackColor: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer

    val circularTrackColor: Color @Composable get() = Color.Transparent
}

@Composable
fun AppLinearProgress(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified,
    flat: Boolean = false,
) {
    val resolvedColor = color.takeOrElse { AppProgressDefaults.color }
    val resolvedTrack = trackColor.takeOrElse { AppProgressDefaults.linearTrackColor }
    if (flat) {
        LinearProgressIndicator(
            progress = progress,
            modifier = modifier,
            color = resolvedColor,
            trackColor = resolvedTrack,
            strokeCap = StrokeCap.Butt,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
    } else {
        LinearProgressIndicator(
            progress = progress,
            modifier = modifier,
            color = resolvedColor,
            trackColor = resolvedTrack,
        )
    }
}

@Composable
fun AppCircularProgress(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    strokeWidth: Dp = AppProgressDefaults.circularStrokeWidth,
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color.takeOrElse { AppProgressDefaults.color },
        strokeWidth = strokeWidth,
    )
}

@Composable
fun AppCircularProgress(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified,
    strokeWidth: Dp = AppProgressDefaults.circularStrokeWidth,
) {
    CircularProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = color.takeOrElse { AppProgressDefaults.color },
        strokeWidth = strokeWidth,
        trackColor = trackColor.takeOrElse { AppProgressDefaults.circularTrackColor },
    )
}
