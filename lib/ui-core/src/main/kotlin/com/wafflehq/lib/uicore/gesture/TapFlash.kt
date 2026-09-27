package com.wafflehq.lib.uicore.gesture

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse

private const val TAP_FLASH_ALPHA = 0.35f
private const val TAP_FLASH_FADE_IN_MILLIS = 30
private const val TAP_FLASH_FADE_OUT_MILLIS = 220

internal fun resolveTapFlashColor(explicit: Color, themeColor: Color): Color = explicit.takeOrElse { themeColor }

@Composable
fun Modifier.tapFlash(
    interactionSource: MutableInteractionSource,
    color: Color = Color.Unspecified
): Modifier {
    val flashColor = resolveTapFlashColor(color, MaterialTheme.colorScheme.onBackground)
    val flashAlpha = remember { Animatable(0f) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press ->
                    flashAlpha.animateTo(TAP_FLASH_ALPHA, tween(TAP_FLASH_FADE_IN_MILLIS))
                is PressInteraction.Release -> {
                    flashAlpha.snapTo(TAP_FLASH_ALPHA)
                    flashAlpha.animateTo(0f, tween(TAP_FLASH_FADE_OUT_MILLIS))
                }
                is PressInteraction.Cancel ->
                    flashAlpha.animateTo(0f, tween(TAP_FLASH_FADE_OUT_MILLIS))
            }
        }
    }
    return this.drawWithContent {
        drawContent()
        if (flashAlpha.value > 0f) drawRect(flashColor.copy(alpha = flashAlpha.value))
    }
}
