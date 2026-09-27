package com.wafflehq.lib.uicore.color

import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.theme.AppRadius
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

object ColorCanvasDefaults {
    val checkerboard = Color(0xFFB8C2D2)
}

@Composable
fun ColorCanvasPicker(
    initialColor: Color,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier,
    checkerboardColor: Color = ColorCanvasDefaults.checkerboard
) {
    val initialHsv = remember(initialColor) {
        FloatArray(3).also { arr ->
            android.graphics.Color.RGBToHSV(
                (initialColor.red * 255).roundToInt(),
                (initialColor.green * 255).roundToInt(),
                (initialColor.blue * 255).roundToInt(),
                arr
            )
        }
    }
    var hue by remember { mutableStateOf(initialHsv[0]) }
    var saturation by remember { mutableStateOf(initialHsv[1]) }
    var value by remember { mutableStateOf(initialHsv[2]) }
    var alpha by remember { mutableStateOf(initialColor.alpha) }

    fun emit() {
        onColorChanged(Color.hsv(hue.coerceIn(0f, 359.9f), saturation, value, alpha))
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        SaturationValuePanel(
            hue = hue,
            saturation = saturation,
            value = value,
            onSaturationValueChange = { s, v -> saturation = s; value = v; emit() }
        )
        HueSlider(hue = hue, onHueChange = { hue = it; emit() })
        AlphaSlider(
            color = Color.hsv(hue.coerceIn(0f, 359.9f), saturation, value),
            alpha = alpha,
            checkerboardColor = checkerboardColor,
            onAlphaChange = { alpha = it; emit() }
        )
    }
}

@Composable
fun SaturationValuePanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationValueChange: (Float, Float) -> Unit
) {
    val hueColor = Color.hsv(hue.coerceIn(0f, 359.9f), 1f, 1f)
    val callback by rememberUpdatedState(onSaturationValueChange)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(AppRadius.cell))
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    callback(
                        (down.position.x / w).coerceIn(0f, 1f),
                        (1f - down.position.y / h).coerceIn(0f, 1f)
                    )
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            callback(
                                (change.position.x / w).coerceIn(0f, 1f),
                                (1f - change.position.y / h).coerceIn(0f, 1f)
                            )
                            change.consume()
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        drawRect(brush = Brush.horizontalGradient(listOf(Color.White, hueColor)))
        drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        drawThumb(Offset(saturation * size.width, (1f - value) * size.height))
    }
}

@Composable
fun HueSlider(hue: Float, onHueChange: (Float) -> Unit) {
    val hueColors = remember {
        (0..24).map { i -> Color.hsv(i * 15f, 1f, 1f) }
    }
    val callback by rememberUpdatedState(onHueChange)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(MaterialTheme.shapes.small)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    callback((down.position.x / size.width.toFloat()).coerceIn(0f, 1f) * 360f)
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            callback((change.position.x / size.width.toFloat()).coerceIn(0f, 1f) * 360f)
                            change.consume()
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        drawRect(brush = Brush.horizontalGradient(hueColors))
        drawThumb(Offset((hue / 360f) * size.width, size.height / 2f))
    }
}

@Composable
fun AlphaSlider(
    color: Color,
    alpha: Float,
    enabled: Boolean = true,
    checkerboardColor: Color = ColorCanvasDefaults.checkerboard,
    onAlphaChange: (Float) -> Unit
) {
    val callback by rememberUpdatedState(onAlphaChange)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(MaterialTheme.shapes.small)
            .alpha(if (enabled) 1f else 0.4f)
            .then(
                if (enabled) {
                    Modifier.pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            callback((down.position.x / size.width.toFloat()).coerceIn(0f, 1f))
                            do {
                                val event = awaitPointerEvent()
                                event.changes.forEach { change ->
                                    callback((change.position.x / size.width.toFloat()).coerceIn(0f, 1f))
                                    change.consume()
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        val squareSize = 12.dp.toPx()
        val cols = (size.width / squareSize).toInt() + 1
        val rows = (size.height / squareSize).toInt() + 1
        for (row in 0..rows) {
            for (col in 0..cols) {
                drawRect(
                    color = if ((row + col) % 2 == 0) checkerboardColor else Color.White,
                    topLeft = Offset(col * squareSize, row * squareSize),
                    size = Size(squareSize, squareSize)
                )
            }
        }
        drawRect(brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0f), color)))
        drawThumb(Offset(alpha * size.width, size.height / 2f))
    }
}

fun DrawScope.drawThumb(center: Offset) {
    drawCircle(Color.White, radius = 10.dp.toPx(), center = center)
    drawCircle(Color.Black, radius = 10.dp.toPx(), center = center, style = Stroke(1.5.dp.toPx()))
}
