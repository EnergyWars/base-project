package com.wafflehq.lib.uicore.topbar

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CollapsibleTopBarTest {

    @get:Rule
    val rule = createComposeRule()

    private var expanded by mutableStateOf(false)
    private var showRevealButton by mutableStateOf(true)
    private var position by mutableStateOf(TopBarRevealButtonPosition.RIGHT)
    private var reveals = 0

    private fun setContent() {
        rule.setContent {
            CollapsibleTopBar(
                expanded = expanded,
                showRevealButton = showRevealButton,
                revealButtonPosition = position,
                onReveal = { reveals++ }
            ) {
                Text(EXPANDED_LABEL)
            }
        }
    }

    @Test
    fun collapsedShowsRevealButtonAndHidesContent() {
        setContent()
        rule.onNodeWithTag(CollapsibleTopBarTestTags.REVEAL_BUTTON).assertIsDisplayed()
        rule.onNodeWithText(EXPANDED_LABEL).assertDoesNotExist()
    }

    @Test
    fun expandedShowsContentAndHidesRevealButton() {
        expanded = true
        setContent()
        rule.onNodeWithText(EXPANDED_LABEL).assertIsDisplayed()
        rule.onNodeWithTag(CollapsibleTopBarTestTags.REVEAL_BUTTON).assertDoesNotExist()
        rule.onNodeWithTag(CollapsibleTopBarTestTags.COLLAPSED_PEEK).assertDoesNotExist()
    }

    @Test
    fun collapsedWithoutButtonShowsOnlyPeekArea() {
        showRevealButton = false
        setContent()
        rule.onNodeWithTag(CollapsibleTopBarTestTags.COLLAPSED_PEEK).assertExists()
        rule.onNodeWithTag(CollapsibleTopBarTestTags.REVEAL_BUTTON).assertDoesNotExist()
        rule.onNodeWithText(EXPANDED_LABEL).assertDoesNotExist()
    }

    @Test
    fun revealButtonClickInvokesOnReveal() {
        setContent()
        rule.onNodeWithTag(CollapsibleTopBarTestTags.REVEAL_BUTTON).performClick()
        assertEquals(1, reveals)
    }

    @Test
    fun togglingExpandedSwitchesBetweenContentAndButton() {
        setContent()
        expanded = true
        rule.mainClock.advanceTimeBy(TOP_BAR_COLLAPSE_ANIM_MS * 2L)
        rule.waitForIdle()
        rule.onNodeWithText(EXPANDED_LABEL).assertIsDisplayed()
        rule.onNodeWithTag(CollapsibleTopBarTestTags.REVEAL_BUTTON).assertDoesNotExist()
        expanded = false
        rule.mainClock.advanceTimeBy(TOP_BAR_COLLAPSE_ANIM_MS * 2L)
        rule.waitForIdle()
        rule.onNodeWithTag(CollapsibleTopBarTestTags.REVEAL_BUTTON).assertIsDisplayed()
        rule.onNodeWithText(EXPANDED_LABEL).assertDoesNotExist()
    }

    @Test
    fun draggingDownOnPeekAreaRevealsWithoutReleasing() {
        setContent()
        rule.onNodeWithTag(CollapsibleTopBarTestTags.COLLAPSED_PEEK).performTouchInput {
            down(topLeft)
            moveBy(Offset(0f, 200f))
        }
        assertEquals(1, reveals)
    }

    @Test
    fun draggingDownOnlyRevealsOnceEvenWithFurtherMovement() {
        setContent()
        rule.onNodeWithTag(CollapsibleTopBarTestTags.COLLAPSED_PEEK).performTouchInput {
            down(topLeft)
            moveBy(Offset(0f, 200f))
            moveBy(Offset(0f, 50f))
            up()
        }
        assertEquals(1, reveals)
    }

    @Test
    fun revealButtonRendersForEveryPosition() {
        setContent()
        TopBarRevealButtonPosition.entries.forEach { candidate ->
            position = candidate
            rule.waitForIdle()
            rule.onNodeWithTag(CollapsibleTopBarTestTags.REVEAL_BUTTON).assertIsDisplayed()
        }
    }

    private companion object {
        const val EXPANDED_LABEL = "expanded content"
    }
}
