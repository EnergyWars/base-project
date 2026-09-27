package com.wafflehq.lib.folders.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import com.wafflehq.lib.folders.FolderTreeRow
import com.wafflehq.lib.uicore.theme.AppRadius

@Composable
fun FolderInsertionLine(visible: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(vertical = FolderListDefaults.insertionLineVerticalPadding)
            .height(FolderListDefaults.insertionLineThickness)
            .clip(RoundedCornerShape(50))
            .background(if (visible) MaterialTheme.colorScheme.primary else Color.Transparent)
            .then(if (visible) Modifier.testTag(FolderTestTags.INSERTION_LINE) else Modifier)
    )
}

@Composable
fun Modifier.folderInsertionLineOverlay(above: Boolean?): Modifier {
    val color = MaterialTheme.colorScheme.primary
    return drawWithContent {
        drawContent()
        if (above != null) {
            val thickness = FolderListDefaults.insertionLineThickness.toPx()
            drawRoundRect(
                color = color,
                topLeft = Offset(0f, if (above) 0f else size.height - thickness),
                size = Size(size.width, thickness),
                cornerRadius = CornerRadius(thickness / 2f)
            )
        }
    }
}

data class FolderFrameLevel(val isFirst: Boolean, val isLast: Boolean, val color: Color? = null)

fun Modifier.folderNestedFrame(color: Color): Modifier = this
    .padding(start = FolderListDefaults.nestedFrameIndent)
    .testTag(FolderTestTags.NESTED_FRAME)
    .border(
        width = FolderListDefaults.expandedBorderWidth,
        color = color.copy(alpha = FolderListDefaults.nestedFrameAlpha),
        shape = RoundedCornerShape(AppRadius.card)
    )
    .padding(FolderListDefaults.nestedFrameInset)

fun folderFrameLevels(rows: List<FolderTreeRow<*, *>>, index: Int): List<FolderFrameLevel> {
    val depth = rows[index].frameDepth()
    val previousDepth = rows.getOrNull(index - 1)?.frameDepth()
    val nextDepth = rows.getOrNull(index + 1)?.frameDepth()
    return (1..depth).map { level ->
        FolderFrameLevel(
            isFirst = previousDepth == level - 1,
            isLast = nextDepth == null || nextDepth < level
        )
    }
}

fun Modifier.folderFlatFrame(
    levels: List<FolderFrameLevel>,
    defaultColor: Color,
    spacing: Dp = FolderListDefaults.itemSpacing
): Modifier {
    val inset = FolderListDefaults.nestedFrameInset
    val firstCount = levels.count { it.isFirst }
    val lastCount = levels.count { it.isLast }
    return this
        .folderFlatFrameDrawing(levels, defaultColor, spacing)
        .padding(
            start = FolderListDefaults.nestedFrameStep * levels.size,
            end = inset * levels.size,
            top = inset * firstCount,
            bottom = inset * lastCount
        )
}

private fun Modifier.folderFlatFrameDrawing(
    levels: List<FolderFrameLevel>,
    defaultColor: Color,
    spacing: Dp
): Modifier = drawBehind {
    if (levels.isEmpty()) return@drawBehind
    val strokeWidth = FolderListDefaults.expandedBorderWidth.toPx()
    val half = strokeWidth / 2f
    val inset = FolderListDefaults.nestedFrameInset.toPx()
    val step = FolderListDefaults.nestedFrameStep.toPx()
    val indent = FolderListDefaults.nestedFrameIndent.toPx()
    val radius = AppRadius.card.toPx()
    val extendTop = spacing.toPx()
    levels.forEachIndexed { level, frame ->
        val left = indent + level * step + half
        val right = size.width - level * inset - half
        val top = levels.take(level).count { it.isFirst } * inset + half
        val bottom = size.height - levels.take(level).count { it.isLast } * inset - half
        val startY = if (frame.isFirst) top + radius else -extendTop
        val endY = if (frame.isLast) bottom - radius else size.height
        val path = Path().apply {
            moveTo(left, endY)
            lineTo(left, startY)
            if (frame.isFirst) {
                arcTo(Rect(left, top, left + 2 * radius, top + 2 * radius), 180f, 90f, false)
                lineTo(right - radius, top)
                arcTo(Rect(right - 2 * radius, top, right, top + 2 * radius), 270f, 90f, false)
            } else {
                moveTo(right, startY)
            }
            lineTo(right, endY)
            if (frame.isLast) {
                arcTo(Rect(right - 2 * radius, bottom - 2 * radius, right, bottom), 0f, 90f, false)
                lineTo(left + radius, bottom)
                arcTo(Rect(left, bottom - 2 * radius, left + 2 * radius, bottom), 90f, 90f, false)
            }
        }
        drawPath(
            path = path,
            color = (frame.color ?: defaultColor).copy(alpha = FolderListDefaults.nestedFrameAlpha),
            style = Stroke(width = strokeWidth)
        )
    }
}

private fun FolderTreeRow<*, *>.frameDepth(): Int = when (this) {
    is FolderTreeRow.FolderRow -> depth
    is FolderTreeRow.EntryRow -> depth
}
