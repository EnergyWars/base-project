package com.wafflehq.lib.pdf.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfZoomContainerTest {

    @get:Rule
    val rule = createComposeRule()

    private val tag = "zoom-container"
    private val scrolls = mutableListOf<Float>()

    private fun setContainer(state: PdfZoomState) {
        rule.setContent {
            PdfZoomContainer(
                state = state,
                onScrollBy = { scrolls += it },
                modifier = Modifier.fillMaxSize().testTag(tag)
            ) {
                Box(Modifier.fillMaxSize())
            }
        }
    }

    private fun viewportWidth() = rule.onNodeWithTag(tag).fetchSemanticsNode().size.width.toFloat()

    @Test
    fun `starts unzoomed`() {
        val state = PdfZoomState()
        setContainer(state)

        rule.waitForIdle()

        assertFalse(state.isZoomed)
    }

    @Test
    fun `pinching outwards zooms in`() {
        val state = PdfZoomState()
        setContainer(state)
        val width = viewportWidth()

        rule.onNodeWithTag(tag).performTouchInput {
            pinch(
                start0 = Offset(width * 0.4f, 200f),
                end0 = Offset(width * 0.2f, 200f),
                start1 = Offset(width * 0.6f, 200f),
                end1 = Offset(width * 0.8f, 200f)
            )
        }
        rule.waitForIdle()

        assertTrue(state.zoom > 1.3f)
    }

    @Test
    fun `pinching inwards zooms back out`() {
        val state = PdfZoomState()
        setContainer(state)
        val width = viewportWidth()
        rule.runOnIdle { state.transform(Offset(width / 2f, 200f), Offset.Zero, 3f, width) }

        rule.onNodeWithTag(tag).performTouchInput {
            pinch(
                start0 = Offset(width * 0.1f, 200f),
                end0 = Offset(width * 0.4f, 200f),
                start1 = Offset(width * 0.9f, 200f),
                end1 = Offset(width * 0.6f, 200f)
            )
        }
        rule.waitForIdle()

        assertTrue(state.zoom < 3f)
    }

    @Test
    fun `double tap zooms in and a second double tap zooms out`() {
        val state = PdfZoomState()
        setContainer(state)

        rule.onNodeWithTag(tag).performTouchInput { doubleClick(center) }
        rule.waitForIdle()
        assertEquals(PdfZoomState.DEFAULT_DOUBLE_TAP_ZOOM, state.zoom, 0.001f)

        rule.onNodeWithTag(tag).performTouchInput { doubleClick(center) }
        rule.waitForIdle()
        assertEquals(PdfZoomState.DEFAULT_MIN_ZOOM, state.zoom, 0.001f)
    }

    @Test
    fun `double tap reports the vertical scroll that keeps the tapped content in place`() {
        val state = PdfZoomState()
        setContainer(state)

        rule.onNodeWithTag(tag).performTouchInput { doubleClick(Offset(centerX, 250f)) }
        rule.waitForIdle()

        assertEquals(1, scrolls.size)
        assertEquals(250f - 250f / PdfZoomState.DEFAULT_DOUBLE_TAP_ZOOM, scrolls.single(), 1f)
    }

    @Test
    fun `dragging horizontally pans while zoomed`() {
        val state = PdfZoomState()
        setContainer(state)
        val width = viewportWidth()
        rule.runOnIdle { state.transform(Offset(width / 2f, 200f), Offset.Zero, 2f, width) }
        val before = state.offsetX

        rule.onNodeWithTag(tag).performTouchInput {
            swipe(start = Offset(width * 0.8f, 200f), end = Offset(width * 0.4f, 200f))
        }
        rule.waitForIdle()

        assertTrue(state.offsetX < before)
        assertTrue(state.offsetX >= width * (1f - state.zoom))
    }

    @Test
    fun `dragging horizontally does nothing while unzoomed`() {
        val state = PdfZoomState()
        setContainer(state)
        val width = viewportWidth()

        rule.onNodeWithTag(tag).performTouchInput {
            swipe(start = Offset(width * 0.8f, 200f), end = Offset(width * 0.4f, 200f))
        }
        rule.waitForIdle()

        assertEquals(0f, state.offsetX, 0.001f)
        assertFalse(state.isZoomed)
    }
}
