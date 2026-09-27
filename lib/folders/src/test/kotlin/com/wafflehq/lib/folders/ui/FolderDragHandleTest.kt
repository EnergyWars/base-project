package com.wafflehq.lib.folders.ui

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalFoundationApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FolderDragHandleTest {

    @get:Rule
    val rule = createComposeRule()

    private var parentClicks = 0
    private var parentLongClicks = 0

    private fun setHandleInsideClickableParent(enabled: Boolean) {
        parentClicks = 0
        parentLongClicks = 0
        val state = FolderDragState<String>()
        rule.setContent {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .combinedClickable(onClick = { parentClicks++ }, onLongClick = { parentLongClicks++ })
            ) {
                FolderDragHandle(
                    state = state,
                    item = "entry",
                    contentDescription = "handle",
                    onDrop = { _, _ -> },
                    enabled = enabled
                )
            }
        }
    }

    @Test
    fun longPressOnEnabledHandle_doesNotTriggerParentLongClick() {
        setHandleInsideClickableParent(enabled = true)

        rule.onNodeWithTag(FolderDragDefaults.HANDLE_TAG, useUnmergedTree = true).performTouchInput { longClick() }
        rule.waitForIdle()

        assertEquals(0, parentLongClicks)
    }

    @Test
    fun tapOnEnabledHandle_doesNotTriggerParentClick() {
        setHandleInsideClickableParent(enabled = true)

        rule.onNodeWithTag(FolderDragDefaults.HANDLE_TAG, useUnmergedTree = true).performClick()
        rule.waitForIdle()

        assertEquals(0, parentClicks)
    }

    @Test
    fun longPressOnDisabledHandle_stillTriggersParentLongClick() {
        setHandleInsideClickableParent(enabled = false)

        rule.onNodeWithTag(FolderDragDefaults.HANDLE_TAG, useUnmergedTree = true).performTouchInput { longClick() }
        rule.waitForIdle()

        assertEquals(1, parentLongClicks)
    }

    @Test
    fun tapOnDisabledHandle_stillTriggersParentClick() {
        setHandleInsideClickableParent(enabled = false)

        rule.onNodeWithTag(FolderDragDefaults.HANDLE_TAG, useUnmergedTree = true).performClick()
        rule.waitForIdle()

        assertEquals(1, parentClicks)
    }
}
