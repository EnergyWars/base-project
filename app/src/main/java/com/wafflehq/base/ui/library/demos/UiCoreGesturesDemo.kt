package com.wafflehq.base.ui.library.demos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppCard
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppSlider
import com.wafflehq.lib.uicore.components.AppStepper
import com.wafflehq.lib.uicore.components.DragReorderableList
import com.wafflehq.lib.uicore.gesture.detectPinchZoom
import com.wafflehq.lib.uicore.gesture.dismissSheetOnDownwardSwipe
import com.wafflehq.lib.uicore.gesture.dragAutoScrollSpeed
import com.wafflehq.lib.uicore.gesture.snapZoomDown
import com.wafflehq.lib.uicore.gesture.snapZoomUp
import com.wafflehq.lib.uicore.gesture.tapFlash
import com.wafflehq.lib.uicore.theme.AppSpacing
import java.util.Locale

internal object UiCoreGesturesLogic {

    const val ZOOM_STEP = 0.25f
    const val ZOOM_MIN = 0.5f
    const val ZOOM_MAX = 3f
    const val CONTAINER_HEIGHT_PX = 300f
    const val EDGE_PX = 60f
    const val MAX_SPEED_PX = 28f
    const val PINCH_MIN = 0.5f
    const val PINCH_MAX = 3f

    fun zoomIn(current: Float): Float = snapZoomUp(current, ZOOM_STEP, ZOOM_MIN, ZOOM_MAX)

    fun zoomOut(current: Float): Float = snapZoomDown(current, ZOOM_STEP, ZOOM_MIN, ZOOM_MAX)

    fun autoScrollSpeed(positionY: Float): Float =
        dragAutoScrollSpeed(positionY, 0f, CONTAINER_HEIGHT_PX, EDGE_PX, MAX_SPEED_PX)

    fun pinch(current: Float, change: Float): Float = (current * change).coerceIn(PINCH_MIN, PINCH_MAX)

    fun format(value: Float, locale: Locale = Locale.getDefault()): String = String.format(locale, "%.2f", value)
}

internal object UiCoreGesturesTags {
    const val FLASH = "libex_uicore_flash"
    const val FLASH_COUNT = "libex_uicore_flash_count"
    const val ZOOM_IN = "libex_uicore_zoom_in"
    const val ZOOM_OUT = "libex_uicore_zoom_out"
    const val ZOOM_VALUE = "libex_uicore_zoom_value"
    const val SPEED_SLIDER = "libex_uicore_speed_slider"
    const val SPEED_VALUE = "libex_uicore_speed_value"
    const val PINCH = "libex_uicore_pinch"
    const val PINCH_VALUE = "libex_uicore_pinch_value"
    const val SWIPE = "libex_uicore_swipe"
    const val SWIPE_COUNT = "libex_uicore_swipe_count"
    const val REORDER_RESULT = "libex_uicore_reorder_result"
    const val HOLD_STEPPER = "libex_uicore_hold_stepper"
    const val HOLD_VALUE = "libex_uicore_hold_value"
    fun handle(item: String) = "libex_uicore_handle_$item"
}

@Composable
internal fun UiCoreGesturesDemo() {
    val flashSource = remember { MutableInteractionSource() }
    var taps by remember { mutableIntStateOf(0) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var pointerY by remember { mutableFloatStateOf(0f) }
    var pinchScale by remember { mutableFloatStateOf(1f) }
    var swipes by remember { mutableIntStateOf(0) }
    var holdValue by remember { mutableIntStateOf(0) }
    val initialOrder = listOf(
        stringResource(R.string.libex_uicore_item_belgian),
        stringResource(R.string.libex_uicore_item_liege),
        stringResource(R.string.libex_uicore_item_stroop),
        stringResource(R.string.libex_uicore_item_hongkong),
    )
    var order by remember { mutableStateOf(initialOrder) }

    DemoSection(
        id = "uicore_gestures",
        titleRes = R.string.libex_uicore_gestures_title,
        descriptionRes = R.string.libex_uicore_gestures_desc,
        moduleRes = R.string.libex_module_uicore,
    ) {
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .tapFlash(flashSource)
                .clickable(interactionSource = flashSource, indication = null) { taps++ }
                .testTag(UiCoreGesturesTags.FLASH),
        ) {
            Text(
                text = stringResource(R.string.libex_uicore_flash_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(AppSpacing.lg),
            )
        }
        DemoMetaText(
            text = stringResource(R.string.libex_uicore_flash_count, taps),
            modifier = Modifier.testTag(UiCoreGesturesTags.FLASH_COUNT),
        )
        AppHorizontalDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            AppButton(
                text = stringResource(R.string.libex_uicore_zoom_out),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Outlined,
                onClick = { zoom = UiCoreGesturesLogic.zoomOut(zoom) },
                modifier = Modifier.testTag(UiCoreGesturesTags.ZOOM_OUT),
            )
            AppButton(
                text = stringResource(R.string.libex_uicore_zoom_in),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                onClick = { zoom = UiCoreGesturesLogic.zoomIn(zoom) },
                modifier = Modifier.testTag(UiCoreGesturesTags.ZOOM_IN),
            )
        }
        DemoMetaText(
            text = stringResource(R.string.libex_uicore_zoom_value, UiCoreGesturesLogic.format(zoom)),
            modifier = Modifier.testTag(UiCoreGesturesTags.ZOOM_VALUE),
        )
        AppHorizontalDivider()
        AppSlider(
            value = pointerY,
            onValueChange = { pointerY = it },
            valueRange = 0f..UiCoreGesturesLogic.CONTAINER_HEIGHT_PX,
            modifier = Modifier.testTag(UiCoreGesturesTags.SPEED_SLIDER),
        )
        DemoMetaText(
            text = stringResource(R.string.libex_uicore_speed_value, UiCoreGesturesLogic.format(UiCoreGesturesLogic.autoScrollSpeed(pointerY))),
            modifier = Modifier.testTag(UiCoreGesturesTags.SPEED_VALUE),
        )
        AppHorizontalDivider()
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(PINCH_HEIGHT)
                .pointerInput(Unit) {
                    detectPinchZoom(onZoom = { _, change -> pinchScale = UiCoreGesturesLogic.pinch(pinchScale, change) })
                }
                .testTag(UiCoreGesturesTags.PINCH),
        ) {
            Text(
                text = stringResource(R.string.libex_uicore_pinch_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(AppSpacing.lg),
            )
        }
        DemoMetaText(
            text = stringResource(R.string.libex_uicore_pinch_value, UiCoreGesturesLogic.format(pinchScale)),
            modifier = Modifier.testTag(UiCoreGesturesTags.PINCH_VALUE),
        )
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(SWIPE_HEIGHT)
                .dismissSheetOnDownwardSwipe { swipes++ }
                .testTag(UiCoreGesturesTags.SWIPE),
        ) {
            Text(
                text = stringResource(R.string.libex_uicore_swipe_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(AppSpacing.lg),
            )
        }
        DemoMetaText(
            text = stringResource(R.string.libex_uicore_swipe_count, swipes),
            modifier = Modifier.testTag(UiCoreGesturesTags.SWIPE_COUNT),
        )
        AppHorizontalDivider()
        DragReorderableList(
            items = order,
            keyOf = { it },
            onOrderChanged = { order = it },
            itemSpacing = AppSpacing.xs,
        ) { item, _, _, handle ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = stringResource(R.string.libex_uicore_reorder_handle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.then(handle).testTag(UiCoreGesturesTags.handle(item)),
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        DemoMetaText(
            text = stringResource(R.string.libex_uicore_reorder_result, order.joinToString()),
            modifier = Modifier.testTag(UiCoreGesturesTags.REORDER_RESULT),
        )
        AppHorizontalDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            DemoBodyText(stringResource(R.string.libex_uicore_hold_hint), modifier = Modifier.weight(1f))
            AppStepper(
                value = holdValue,
                onValueChange = { holdValue = it },
                range = 0..HOLD_MAX,
                repeatOnHold = true,
                modifier = Modifier.testTag(UiCoreGesturesTags.HOLD_STEPPER),
            )
        }
        DemoMetaText(
            text = stringResource(R.string.libex_uicore_hold_value, holdValue),
            modifier = Modifier.testTag(UiCoreGesturesTags.HOLD_VALUE),
        )
    }
}

private val PINCH_HEIGHT = 96.dp
private val SWIPE_HEIGHT = 180.dp
private const val HOLD_MAX = 100
