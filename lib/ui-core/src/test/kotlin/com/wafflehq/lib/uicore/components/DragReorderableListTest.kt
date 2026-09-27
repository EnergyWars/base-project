package com.wafflehq.lib.uicore.components

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
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
class DragReorderableListTest {

    @get:Rule
    val rule = createComposeRule()

    private val items = listOf("A", "B", "C")

    @Test
    fun rendersAllItemsInOrder() {
        rule.setContent {
            DragReorderableList(items = items, keyOf = { it }, onOrderChanged = {}) { item, index, _, handle ->
                Box(Modifier.fillMaxWidth().height(48.dp).then(handle).testTag("handle_$item")) { Text("$index:$item") }
            }
        }

        rule.onNodeWithText("0:A").assertIsDisplayed()
        rule.onNodeWithText("1:B").assertIsDisplayed()
        rule.onNodeWithText("2:C").assertIsDisplayed()
    }

    @Test
    fun draggingFirstItemDownReordersList() {
        val orders = mutableListOf<List<String>>()
        rule.setContent {
            DragReorderableList(items = items, keyOf = { it }, onOrderChanged = { orders += it }) { item, _, _, handle ->
                Box(Modifier.fillMaxWidth().height(48.dp).then(handle).testTag("handle_$item")) { Text(item) }
            }
        }

        rule.onNodeWithTag("handle_A").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, height * 2.5f))
            up()
        }

        rule.runOnIdle {
            assertTrue(orders.isNotEmpty())
            assertEquals(listOf("B", "C", "A"), orders.last())
        }
    }

    @Test
    fun releasingWithoutMovementKeepsOrder() {
        val orders = mutableListOf<List<String>>()
        rule.setContent {
            DragReorderableList(items = items, keyOf = { it }, onOrderChanged = { orders += it }) { item, _, _, handle ->
                Box(Modifier.fillMaxWidth().height(48.dp).then(handle).testTag("handle_$item")) { Text(item) }
            }
        }

        rule.onNodeWithTag("handle_B").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            up()
        }

        rule.runOnIdle { assertTrue(orders.isEmpty()) }
    }

    @Test
    fun holdingFromOnDragMoveKeepsNeighborsInPlaceWhileDragging() {
        rule.setContent {
            DragReorderableList(
                items = items,
                keyOf = { it },
                onOrderChanged = {},
                onDragMove = { _, _, _ -> true },
                onDragEnd = { false }
            ) { item, _, _, handle ->
                Box(Modifier.fillMaxWidth().height(48.dp).then(handle).testTag("handle_$item")) { Text(item) }
            }
        }

        val bBoundsBeforeDrag = rule.onNodeWithTag("handle_B").getBoundsInRoot()

        rule.onNodeWithTag("handle_A").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, height * 2.5f))
        }

        rule.runOnIdle {
            val bBoundsWhileDragging = rule.onNodeWithTag("handle_B").getBoundsInRoot()
            assertEquals(bBoundsBeforeDrag.top, bBoundsWhileDragging.top)
        }

        rule.onNodeWithTag("handle_A").performTouchInput { up() }
    }

    @Test
    fun holdingFromOnDragMoveSkipsReorderOnDrop() {
        val orders = mutableListOf<List<String>>()
        rule.setContent {
            DragReorderableList(
                items = items,
                keyOf = { it },
                onOrderChanged = { orders += it },
                onDragMove = { _, _, _ -> true }
            ) { item, _, _, handle ->
                Box(Modifier.fillMaxWidth().height(48.dp).then(handle).testTag("handle_$item")) { Text(item) }
            }
        }

        rule.onNodeWithTag("handle_A").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, height * 2.5f))
            up()
        }

        rule.runOnIdle { assertTrue(orders.isEmpty()) }
    }

    @Test
    fun onDragEndReturningTrueSkipsReorder() {
        val orders = mutableListOf<List<String>>()
        val ended = mutableListOf<String>()
        rule.setContent {
            DragReorderableList(
                items = items,
                keyOf = { it },
                onOrderChanged = { orders += it },
                onDragEnd = { ended += it; true }
            ) { item, _, _, handle ->
                Box(Modifier.fillMaxWidth().height(48.dp).then(handle).testTag("handle_$item")) { Text(item) }
            }
        }

        rule.onNodeWithTag("handle_A").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, height * 2.5f))
            up()
        }

        rule.runOnIdle {
            assertEquals(listOf("A"), ended)
            assertTrue(orders.isEmpty())
        }
    }

    @Test
    fun onDragMoveReportsTargetIndexUnderFinger() {
        var lastTarget = -1
        rule.setContent {
            DragReorderableList(
                items = items,
                keyOf = { it },
                onOrderChanged = {},
                onDragMove = { _, _, target -> lastTarget = target; false }
            ) { item, _, _, handle ->
                Box(Modifier.fillMaxWidth().height(48.dp).then(handle).testTag("handle_$item")) { Text(item) }
            }
        }

        rule.onNodeWithTag("handle_A").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, height * 1.25f))
        }

        rule.runOnIdle { assertEquals(1, lastTarget) }

        rule.onNodeWithTag("handle_A").performTouchInput { up() }
    }

    @Test
    fun usesLatestItemsAfterListChangedWithoutIndexShift() {
        val orders = mutableListOf<List<String>>()
        var list by mutableStateOf(items)
        rule.setContent {
            DragReorderableList(items = list, keyOf = { it }, onOrderChanged = { orders += it }) { item, _, _, handle ->
                Box(Modifier.fillMaxWidth().height(48.dp).then(handle).testTag("handle_$item")) { Text(item) }
            }
        }

        rule.runOnIdle { list = listOf("A", "B", "C", "D") }
        rule.waitForIdle()

        rule.onNodeWithTag("handle_A").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, height * 3.6f))
            up()
        }

        rule.runOnIdle { assertEquals(listOf("B", "C", "D", "A"), orders.last()) }
    }

    @Test
    fun tallItemOnlySwapsWithShortNeighborOnceFingerReachesItsLowerHalf() {
        val orders = mutableListOf<List<String>>()
        rule.setContent {
            DragReorderableList(items = listOf("T", "S"), keyOf = { it }, onOrderChanged = { orders += it }) { item, _, _, handle ->
                val itemHeight = if (item == "T") 200.dp else 48.dp
                Box(Modifier.fillMaxWidth().height(itemHeight).then(handle).testTag("handle_$item")) { Text(item) }
            }
        }

        rule.onNodeWithTag("handle_T").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, 110.dp.toPx()))
            up()
        }
        rule.runOnIdle { assertTrue(orders.isEmpty()) }

        rule.onNodeWithTag("handle_T").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, 140.dp.toPx()))
            up()
        }
        rule.runOnIdle { assertEquals(listOf("S", "T"), orders.last()) }
    }

    @Test
    fun dragOnWholeClickableItem_doesNotFireChildClickOnRelease() {
        var clicks = 0
        val orders = mutableListOf<List<String>>()
        rule.setContent {
            DragReorderableList(items = items, keyOf = { it }, onOrderChanged = { orders += it }) { item, _, _, handle ->
                Box(Modifier.fillMaxWidth().height(48.dp).then(handle).testTag("item_$item")) {
                    Box(Modifier.fillMaxSize().clickable { clicks++ })
                }
            }
        }

        rule.onNodeWithTag("item_A").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, height * 2.5f))
            up()
        }

        rule.runOnIdle {
            assertEquals(0, clicks)
            assertEquals(listOf("B", "C", "A"), orders.last())
        }
    }

    @Test
    fun longPressWithoutMovementOnWholeClickableItem_doesNotFireChildClick() {
        var clicks = 0
        rule.setContent {
            DragReorderableList(items = items, keyOf = { it }, onOrderChanged = {}) { item, _, _, handle ->
                Box(Modifier.fillMaxWidth().height(48.dp).then(handle).testTag("item_$item")) {
                    Box(Modifier.fillMaxSize().clickable { clicks++ })
                }
            }
        }

        rule.onNodeWithTag("item_B").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            up()
        }

        rule.runOnIdle { assertEquals(0, clicks) }
    }

    @Test
    fun quickTapOnWholeClickableItem_stillClicksAndNeverReorders() {
        var clicks = 0
        val orders = mutableListOf<List<String>>()
        rule.setContent {
            DragReorderableList(items = items, keyOf = { it }, onOrderChanged = { orders += it }) { item, _, _, handle ->
                Box(Modifier.fillMaxWidth().height(48.dp).then(handle).testTag("item_$item")) {
                    Box(Modifier.fillMaxSize().clickable { clicks++ })
                }
            }
        }

        rule.onNodeWithTag("item_A").performClick()

        rule.runOnIdle {
            assertEquals(1, clicks)
            assertTrue(orders.isEmpty())
        }
    }

    @Test
    fun holdShorterThanActivationDelay_doesNotStartDrag() {
        val orders = mutableListOf<List<String>>()
        rule.setContent {
            DragReorderableList(
                items = items,
                keyOf = { it },
                onOrderChanged = { orders += it },
                activationDelayMs = 5_000
            ) { item, _, _, handle ->
                Box(Modifier.fillMaxWidth().height(48.dp).then(handle).testTag("item_$item")) { Text(item) }
            }
        }

        rule.onNodeWithTag("item_A").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, height * 2.5f))
            up()
        }

        rule.runOnIdle { assertTrue(orders.isEmpty()) }
    }

    @Test
    fun holdLongerThanCustomActivationDelay_startsDrag() {
        val orders = mutableListOf<List<String>>()
        rule.setContent {
            DragReorderableList(
                items = items,
                keyOf = { it },
                onOrderChanged = { orders += it },
                activationDelayMs = 400
            ) { item, _, _, handle ->
                Box(Modifier.fillMaxWidth().height(48.dp).then(handle).testTag("item_$item")) { Text(item) }
            }
        }

        rule.onNodeWithTag("item_A").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, height * 2.5f))
            up()
        }

        rule.runOnIdle { assertEquals(listOf("B", "C", "A"), orders.last()) }
    }
}
