package com.wafflehq.lib.uicore.gesture

import android.app.Application
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AwaitLongPressTest {

    @get:Rule
    val rule = createComposeRule()

    private val results = mutableListOf<Boolean>()

    private fun setContent(timeoutMillis: Long = 500L) {
        rule.setContent {
            Box(
                Modifier
                    .size(200.dp)
                    .testTag(TAG)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            results += awaitLongPress(down, timeoutMillis, viewConfiguration.touchSlop)
                        }
                    }
            )
        }
        rule.mainClock.autoAdvance = false
    }

    @Test
    fun `holding still until the timeout counts as a long press`() {
        setContent()

        rule.onNodeWithTag(TAG).performTouchInput { down(center) }
        rule.mainClock.advanceTimeBy(600L)
        rule.onNodeWithTag(TAG).performTouchInput { up() }

        assertEquals(listOf(true), results)
    }

    @Test
    fun `releasing before the timeout is not a long press`() {
        setContent()

        rule.onNodeWithTag(TAG).performTouchInput { down(center) }
        rule.mainClock.advanceTimeBy(100L)
        rule.onNodeWithTag(TAG).performTouchInput { up() }

        assertEquals(listOf(false), results)
    }

    @Test
    fun `moving beyond the touch slop before the timeout is not a long press`() {
        setContent()

        rule.onNodeWithTag(TAG).performTouchInput {
            down(center)
            moveBy(Offset(300f, 0f))
        }
        rule.mainClock.advanceTimeBy(600L)
        rule.onNodeWithTag(TAG).performTouchInput { up() }

        assertEquals(listOf(false), results)
    }

    private companion object {
        const val TAG = "long_press_target"
    }
}
