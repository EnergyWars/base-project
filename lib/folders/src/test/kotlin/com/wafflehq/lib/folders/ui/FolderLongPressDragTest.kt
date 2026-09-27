package com.wafflehq.lib.folders.ui

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
class FolderLongPressDragTest {

    @get:Rule
    val rule = createComposeRule()

    private val state = FolderDragState<String>()
    private var dropped: Pair<String, Long?>? = null
    private var reordered: Pair<String, Any>? = null
    private var clicks = 0

    private fun setContent(enabled: Boolean = true, withReorder: Boolean = false) {
        dropped = null
        reordered = null
        clicks = 0
        rule.setContent {
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .onGloballyPositioned { state.registerFolderBounds(1L, it) }
                        .testTag("folder")
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .folderLongPressDrag(
                            state = state,
                            item = "entry",
                            onDrop = { item, target -> dropped = item to target },
                            enabled = enabled,
                            onReorder = if (withReorder) ({ item, key -> reordered = item to key }) else null
                        )
                        .testTag("entry")
                ) {
                    Box(Modifier.fillMaxSize().clickable { clicks++ })
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .onGloballyPositioned { state.registerEntryBounds("sibling", it) }
                        .testTag("sibling")
                )
            }
        }
    }

    @Test
    fun longPressAndMoveOntoFolder_dropsIntoThatFolder() {
        setContent()

        rule.onNodeWithTag("entry").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, -height * 1f))
            up()
        }

        rule.runOnIdle {
            assertEquals("entry" to 1L, dropped)
            assertFalse(state.isDragging)
        }
    }

    @Test
    fun longPressAndMoveOntoSiblingEntry_reordersRelativeToIt() {
        setContent(withReorder = true)

        rule.onNodeWithTag("entry").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, height * 1f))
            up()
        }

        rule.runOnIdle {
            assertEquals("entry" to "sibling", reordered)
            assertNull(dropped)
        }
    }

    @Test
    fun longPressWithoutMovement_endsDragWithoutTargetAndSwallowsTheClick() {
        setContent()

        rule.onNodeWithTag("entry").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            up()
        }

        rule.runOnIdle {
            assertEquals("entry" to null, dropped)
            assertEquals(0, clicks)
        }
    }

    @Test
    fun draggingIsVisibleInStateWhileFingerIsDown() {
        setContent()

        rule.onNodeWithTag("entry").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, 10f))
        }

        rule.runOnIdle {
            assertTrue(state.isDragging)
            assertEquals("entry", state.draggedItem)
        }

        rule.onNodeWithTag("entry").performTouchInput { up() }
        rule.runOnIdle { assertFalse(state.isDragging) }
    }

    @Test
    fun quickTap_clicksAndNeverStartsDrag() {
        setContent()

        rule.onNodeWithTag("entry").performClick()

        rule.runOnIdle {
            assertEquals(1, clicks)
            assertNull(dropped)
            assertFalse(state.isDragging)
        }
    }

    @Test
    fun movingBeyondTouchSlopBeforeTheLongPressTimeout_cancelsTheDrag() {
        setContent()

        rule.onNodeWithTag("entry").performTouchInput {
            down(center)
            moveBy(Offset(0f, 300f))
            advanceEventTime(1_000)
            up()
        }

        rule.runOnIdle {
            assertNull(dropped)
            assertFalse(state.isDragging)
        }
    }

    @Test
    fun disabledModifier_neverDragsAndKeepsTheClick() {
        setContent(enabled = false)

        rule.onNodeWithTag("entry").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, -height * 1f))
            up()
        }

        rule.runOnIdle {
            assertNull(dropped)
            assertFalse(state.isDragging)
        }
    }
}
