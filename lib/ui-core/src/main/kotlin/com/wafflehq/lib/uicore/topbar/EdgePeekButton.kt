package com.wafflehq.lib.uicore.topbar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.R
import com.wafflehq.lib.uicore.theme.AppRadius

enum class ScreenEdge { Top, Bottom, Start }

enum class EdgePeekStyle { Bubble, Tab }

enum class TopBarRevealButtonPosition { LEFT, CENTER, RIGHT }

val EdgePeekButtonDiameter = 40.dp
val EdgePeekButtonVisibleHeight = EdgePeekButtonDiameter / 2

private const val EDGE_PEEK_BORDER_ALPHA = 0.25f
private const val EDGE_PEEK_PRESS_EXTENT = 1.2f
private const val EDGE_PEEK_PRESS_SCALE = 1.1f
private const val EDGE_PEEK_BUBBLE_ICON_FACTOR = 0.45f
private const val EDGE_PEEK_TAB_ICON_FACTOR = 0.4f
private const val EDGE_PEEK_BUBBLE_INSET_FACTOR = 0.05f
private val EdgePeekBorderWidth = 1.dp
private val EdgePeekElevation = 2.dp
private val EdgePeekPressedElevation = 6.dp

private fun edgePeekBubbleShape(edge: ScreenEdge): Shape = GenericShape { size, _ ->
    val oval = when (edge) {
        ScreenEdge.Top -> Rect(0f, -size.height, size.width, size.height)
        ScreenEdge.Bottom -> Rect(0f, 0f, size.width, size.height * 2f)
        ScreenEdge.Start -> Rect(-size.width, 0f, size.width, size.height)
    }
    addOval(oval)
}

private fun edgePeekTabShape(edge: ScreenEdge, radius: Dp): Shape = when (edge) {
    ScreenEdge.Top -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
    ScreenEdge.Bottom -> RoundedCornerShape(topStart = radius, topEnd = radius)
    ScreenEdge.Start -> RoundedCornerShape(topEnd = radius, bottomEnd = radius)
}

@Composable
fun EdgePeekButton(
    edge: ScreenEdge,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    diameter: Dp = EdgePeekButtonDiameter,
    style: EdgePeekStyle = EdgePeekStyle.Bubble,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val shape = remember(edge, style, diameter) {
        when (style) {
            EdgePeekStyle.Bubble -> edgePeekBubbleShape(edge)
            EdgePeekStyle.Tab -> edgePeekTabShape(edge, AppRadius.card)
        }
    }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val visibleExtent = diameter / 2
    val extent by animateDpAsState(
        targetValue = if (pressed) visibleExtent * EDGE_PEEK_PRESS_EXTENT else visibleExtent,
        animationSpec = spring(),
        label = "edgePeekExtent"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (pressed) EDGE_PEEK_PRESS_SCALE else 1f,
        animationSpec = spring(),
        label = "edgePeekIconScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) EdgePeekPressedElevation else EdgePeekElevation,
        animationSpec = spring(),
        label = "edgePeekElevation"
    )
    val iconFactor = when (style) {
        EdgePeekStyle.Bubble -> EDGE_PEEK_BUBBLE_ICON_FACTOR
        EdgePeekStyle.Tab -> EDGE_PEEK_TAB_ICON_FACTOR
    }
    val edgeInset = when (style) {
        EdgePeekStyle.Bubble -> diameter * EDGE_PEEK_BUBBLE_INSET_FACTOR
        EdgePeekStyle.Tab -> 0.dp
    }
    val sizedModifier = if (edge == ScreenEdge.Start) {
        modifier.size(width = extent, height = diameter)
    } else {
        modifier.size(width = diameter, height = extent)
    }
    Surface(
        modifier = sizedModifier.combinedClickable(
            interactionSource = interactionSource,
            indication = ripple(),
            role = Role.Button,
            onLongClickLabel = onLongClickLabel,
            onLongClick = onLongClick,
            onClick = onClick
        ),
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(EdgePeekBorderWidth, contentColor.copy(alpha = EDGE_PEEK_BORDER_ALPHA)),
        shadowElevation = elevation
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = when {
                style == EdgePeekStyle.Tab -> Alignment.Center
                edge == ScreenEdge.Top -> Alignment.BottomCenter
                edge == ScreenEdge.Bottom -> Alignment.TopCenter
                else -> Alignment.CenterEnd
            }
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(diameter * iconFactor)
                    .padding(
                        top = if (edge == ScreenEdge.Bottom) edgeInset else 0.dp,
                        bottom = if (edge == ScreenEdge.Top) edgeInset else 0.dp,
                        end = if (edge == ScreenEdge.Start) edgeInset else 0.dp
                    )
                    .scale(iconScale)
            )
        }
    }
}

@Composable
fun TopBarRevealButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(R.string.uicore_top_bar_expand_cd)
) {
    EdgePeekButton(
        edge = ScreenEdge.Top,
        onClick = onClick,
        icon = Icons.Default.KeyboardArrowDown,
        contentDescription = contentDescription,
        modifier = modifier
    )
}
