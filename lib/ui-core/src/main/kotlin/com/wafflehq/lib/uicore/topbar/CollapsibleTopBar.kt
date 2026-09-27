package com.wafflehq.lib.uicore.topbar

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

const val TOP_BAR_COLLAPSE_ANIM_MS = 220

object CollapsibleTopBarTestTags {
    const val EXPANDED_CONTENT = "collapsible_top_bar_expanded"
    const val COLLAPSED_PEEK = "collapsible_top_bar_peek"
    const val REVEAL_BUTTON = "collapsible_top_bar_reveal_button"
}

@Composable
fun CollapsibleTopBar(
    expanded: Boolean,
    showRevealButton: Boolean,
    revealButtonPosition: TopBarRevealButtonPosition,
    onReveal: () -> Unit,
    modifier: Modifier = Modifier,
    expandedContent: @Composable () -> Unit
) {
    AnimatedContent(
        targetState = expanded,
        modifier = modifier.fillMaxWidth(),
        transitionSpec = {
            (fadeIn(tween(TOP_BAR_COLLAPSE_ANIM_MS)) togetherWith fadeOut(tween(TOP_BAR_COLLAPSE_ANIM_MS)))
                .using(SizeTransform(clip = true) { _, _ -> tween(TOP_BAR_COLLAPSE_ANIM_MS) })
        },
        contentAlignment = Alignment.TopCenter,
        label = "collapsibleTopBar"
    ) { isExpanded ->
        if (isExpanded) {
            Box(modifier = Modifier.fillMaxWidth().testTag(CollapsibleTopBarTestTags.EXPANDED_CONTENT)) {
                expandedContent()
            }
        } else {
            CollapsedTopBarPeek(
                showRevealButton = showRevealButton,
                revealButtonPosition = revealButtonPosition,
                onReveal = onReveal
            )
        }
    }
}

@Composable
private fun CollapsedTopBarPeek(
    showRevealButton: Boolean,
    revealButtonPosition: TopBarRevealButtonPosition,
    onReveal: () -> Unit
) {
    val alignment = when (revealButtonPosition) {
        TopBarRevealButtonPosition.LEFT -> Alignment.TopStart
        TopBarRevealButtonPosition.CENTER -> Alignment.TopCenter
        TopBarRevealButtonPosition.RIGHT -> Alignment.TopEnd
    }
    val currentOnReveal by rememberUpdatedState(onReveal)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(if (showRevealButton) EdgePeekButtonVisibleHeight else 0.dp)
            .testTag(CollapsibleTopBarTestTags.COLLAPSED_PEEK)
            .then(
                if (showRevealButton) {
                    Modifier.pointerInput(Unit) {
                        var revealedThisGesture = false
                        detectVerticalDragGestures(
                            onDragStart = { revealedThisGesture = false },
                            onDragEnd = { revealedThisGesture = false },
                            onDragCancel = { revealedThisGesture = false },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                if (!revealedThisGesture && dragAmount > 0f) {
                                    revealedThisGesture = true
                                    currentOnReveal()
                                }
                            }
                        )
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = alignment
    ) {
        if (showRevealButton) {
            TopBarRevealButton(
                onClick = onReveal,
                modifier = Modifier
                    .padding(
                        start = if (revealButtonPosition == TopBarRevealButtonPosition.LEFT) AppSpacing.sm else 0.dp,
                        end = if (revealButtonPosition == TopBarRevealButtonPosition.RIGHT) AppSpacing.sm else 0.dp
                    )
                    .testTag(CollapsibleTopBarTestTags.REVEAL_BUTTON)
            )
        }
    }
}
