package com.wafflehq.base.ui.library.demos

import android.app.Application
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UiCoreGesturesDemoTest : LibraryDemoTest() {

    @Test
    fun tapFlashCardCountsTaps() {
        show { UiCoreGesturesDemo() }
        node(DemoTags.section("uicore_gestures")).assertExists()
        assertTagText(UiCoreGesturesTags.FLASH_COUNT, string(R.string.libex_uicore_flash_count, 0))

        click(UiCoreGesturesTags.FLASH)
        click(UiCoreGesturesTags.FLASH)

        assertTagText(UiCoreGesturesTags.FLASH_COUNT, string(R.string.libex_uicore_flash_count, 2))
    }

    @Test
    fun zoomButtonsSnapToTheStep() {
        show { UiCoreGesturesDemo() }
        assertTagText(UiCoreGesturesTags.ZOOM_VALUE, UiCoreGesturesLogic.format(1f))

        click(UiCoreGesturesTags.ZOOM_IN)
        assertTagText(UiCoreGesturesTags.ZOOM_VALUE, UiCoreGesturesLogic.format(1.25f))

        click(UiCoreGesturesTags.ZOOM_OUT)
        click(UiCoreGesturesTags.ZOOM_OUT)
        assertTagText(UiCoreGesturesTags.ZOOM_VALUE, UiCoreGesturesLogic.format(0.75f))
    }

    @Test
    fun autoScrollSpeedReactsToTheSliderPosition() {
        show { UiCoreGesturesDemo() }
        assertTagText(UiCoreGesturesTags.SPEED_VALUE, UiCoreGesturesLogic.format(UiCoreGesturesLogic.autoScrollSpeed(0f)))

        setSlider(UiCoreGesturesTags.SPEED_SLIDER, 150f)
        assertTagText(UiCoreGesturesTags.SPEED_VALUE, UiCoreGesturesLogic.format(0f))

        setSlider(UiCoreGesturesTags.SPEED_SLIDER, 30f)
        assertTagText(UiCoreGesturesTags.SPEED_VALUE, UiCoreGesturesLogic.format(-14f))
    }

    @Test
    fun longSwipeDownIsCounted() {
        show { UiCoreGesturesDemo() }
        assertTagText(UiCoreGesturesTags.SWIPE_COUNT, string(R.string.libex_uicore_swipe_count, 0))

        node(UiCoreGesturesTags.SWIPE).performScrollTo().performTouchInput {
            swipeDown(startY = top, endY = bottom, durationMillis = 300L)
        }

        assertTagText(UiCoreGesturesTags.SWIPE_COUNT, string(R.string.libex_uicore_swipe_count, 1))
    }

    @Test
    fun reorderListShowsTheInitialOrder() {
        show { UiCoreGesturesDemo() }

        val expected = listOf(
            R.string.libex_uicore_item_belgian,
            R.string.libex_uicore_item_liege,
            R.string.libex_uicore_item_stroop,
            R.string.libex_uicore_item_hongkong,
        ).joinToString { string(it) }
        assertTagText(UiCoreGesturesTags.REORDER_RESULT, string(R.string.libex_uicore_reorder_result, expected))
        assertTagCount(UiCoreGesturesTags.handle(string(R.string.libex_uicore_item_belgian)), 1)
    }

    @Test
    fun holdStepperIncrementsOnTap() {
        show { UiCoreGesturesDemo() }
        assertTagText(UiCoreGesturesTags.HOLD_VALUE, string(R.string.libex_uicore_hold_value, 0))

        stepUp(UiCoreGesturesTags.HOLD_STEPPER)

        assertTagText(UiCoreGesturesTags.HOLD_VALUE, string(R.string.libex_uicore_hold_value, 1))
    }

    @Test
    fun pinchAreaStartsAtScaleOne() {
        show { UiCoreGesturesDemo() }

        node(UiCoreGesturesTags.PINCH).assertExists()
        assertTagText(UiCoreGesturesTags.PINCH_VALUE, string(R.string.libex_uicore_pinch_value, UiCoreGesturesLogic.format(1f)))
    }

    @Test
    fun zoomSnappingClampsToTheBounds() {
        assertEquals(1.25f, UiCoreGesturesLogic.zoomIn(1f), 0.0001f)
        assertEquals(0.75f, UiCoreGesturesLogic.zoomOut(1f), 0.0001f)
        assertEquals(UiCoreGesturesLogic.ZOOM_MAX, UiCoreGesturesLogic.zoomIn(UiCoreGesturesLogic.ZOOM_MAX), 0.0001f)
        assertEquals(UiCoreGesturesLogic.ZOOM_MIN, UiCoreGesturesLogic.zoomOut(UiCoreGesturesLogic.ZOOM_MIN), 0.0001f)
    }

    @Test
    fun autoScrollSpeedScalesWithTheDistanceToTheEdge() {
        assertEquals(-UiCoreGesturesLogic.MAX_SPEED_PX, UiCoreGesturesLogic.autoScrollSpeed(0f), 0.0001f)
        assertEquals(0f, UiCoreGesturesLogic.autoScrollSpeed(UiCoreGesturesLogic.CONTAINER_HEIGHT_PX / 2), 0.0001f)
        assertEquals(UiCoreGesturesLogic.MAX_SPEED_PX, UiCoreGesturesLogic.autoScrollSpeed(UiCoreGesturesLogic.CONTAINER_HEIGHT_PX), 0.0001f)
    }

    @Test
    fun pinchScaleIsClamped() {
        assertEquals(UiCoreGesturesLogic.PINCH_MAX, UiCoreGesturesLogic.pinch(2.5f, 2f), 0.0001f)
        assertEquals(UiCoreGesturesLogic.PINCH_MIN, UiCoreGesturesLogic.pinch(0.6f, 0.1f), 0.0001f)
        assertEquals(1.5f, UiCoreGesturesLogic.pinch(1f, 1.5f), 0.0001f)
    }
}
