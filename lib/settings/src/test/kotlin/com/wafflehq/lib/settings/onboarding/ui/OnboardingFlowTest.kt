package com.wafflehq.lib.settings.onboarding.ui

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wafflehq.lib.settings.onboarding.OnboardingPage
import com.wafflehq.lib.settings.onboarding.OnboardingPageActions
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
class OnboardingFlowTest {

    @get:Rule
    val rule = createComposeRule()

    private fun page(name: String) = OnboardingPage(name) { actions ->
        SimplePage(name, actions)
    }

    @Test
    fun `pages forward through the list`() {
        rule.setContent {
            OnboardingFlow(pages = listOf(page("one"), page("two"), page("three")), onFinish = {})
        }

        rule.onNodeWithText("one").assertIsDisplayed()

        rule.onNodeWithText("next-one").performClick()

        rule.onNodeWithText("two").assertIsDisplayed()
        rule.onNodeWithText("one").assertDoesNotExist()
    }

    @Test
    fun `pages back to the previous page`() {
        rule.setContent {
            OnboardingFlow(pages = listOf(page("one"), page("two")), onFinish = {})
        }

        rule.onNodeWithText("next-one").performClick()
        rule.onNodeWithText("back-two").performClick()

        rule.onNodeWithText("one").assertIsDisplayed()
    }

    @Test
    fun `back on the first page stays put`() {
        rule.setContent {
            OnboardingFlow(pages = listOf(page("one"), page("two")), onFinish = {})
        }

        rule.onNodeWithText("back-one").performClick()

        rule.onNodeWithText("one").assertIsDisplayed()
    }

    @Test
    fun `progress follows the list size`() {
        rule.setContent {
            OnboardingFlow(
                pages = listOf(page("one"), page("two"), page("three"), page("four")),
                onFinish = {}
            )
        }

        rule.onNodeWithTag(OnboardingTestTags.PROGRESS)
            .assertRangeInfoEquals(ProgressBarRangeInfo(0.25f, 0f..1f))

        rule.onNodeWithText("next-one").performClick()

        rule.onNodeWithTag(OnboardingTestTags.PROGRESS)
            .assertRangeInfoEquals(ProgressBarRangeInfo(0.5f, 0f..1f))
    }

    @Test
    fun `leaving a page out shifts the progress accordingly`() {
        rule.setContent {
            OnboardingFlow(pages = listOf(page("one"), page("two"), page("three")), onFinish = {})
        }

        rule.onNodeWithTag(OnboardingTestTags.PROGRESS)
            .assertRangeInfoEquals(ProgressBarRangeInfo(1f / 3f, 0f..1f))
    }

    @Test
    fun `the last page knows it is the last one`() {
        rule.setContent {
            OnboardingFlow(pages = listOf(page("one"), page("two")), onFinish = {})
        }

        rule.onNodeWithText("not last").assertIsDisplayed()

        rule.onNodeWithText("next-one").performClick()

        rule.onNodeWithText("last").assertIsDisplayed()
    }

    @Test
    fun `next on the last page finishes`() {
        var finished = 0
        rule.setContent {
            OnboardingFlow(pages = listOf(page("one"), page("two")), onFinish = { finished++ })
        }

        rule.onNodeWithText("next-one").performClick()
        rule.onNodeWithText("next-two").performClick()

        rule.runOnIdle { assertEquals(1, finished) }
    }

    @Test
    fun `finish reports exactly once, however often it is triggered`() {
        var finished = 0
        val finishing = OnboardingPage("finishing") { actions ->
            OnboardingButtonRow(
                primaryLabel = "done",
                onPrimary = actions::finish,
                secondaryLabel = "also-done",
                onSecondary = actions::finish
            )
        }
        rule.setContent {
            OnboardingFlow(pages = listOf(finishing), onFinish = { finished++ })
        }

        rule.onNodeWithText("done").performClick()
        rule.onNodeWithText("done").performClick()
        rule.onNodeWithText("also-done").performClick()

        rule.runOnIdle { assertEquals(1, finished) }
    }

    @Test
    fun `a page may finish early`() {
        var finished = 0
        val pages = listOf(
            OnboardingPage("first") { actions ->
                OnboardingButtonRow(
                    primaryLabel = "next",
                    onPrimary = actions::next,
                    secondaryLabel = "enough",
                    onSecondary = actions::finish
                )
            },
            page("second")
        )
        rule.setContent { OnboardingFlow(pages = pages, onFinish = { finished++ }) }

        rule.onNodeWithText("enough").performClick()

        rule.runOnIdle { assertEquals(1, finished) }
    }

    @Test
    fun `an empty page list finishes right away`() {
        var finished = 0
        rule.setContent { OnboardingFlow(pages = emptyList(), onFinish = { finished++ }) }

        rule.runOnIdle { assertEquals(1, finished) }
    }
}

@Composable
private fun SimplePage(name: String, actions: OnboardingPageActions) {
    Text(name)
    Text(if (actions.isLastPage) "last" else "not last")
    OnboardingButtonRow(
        primaryLabel = "next-$name",
        onPrimary = actions::next,
        secondaryLabel = "back-$name",
        onSecondary = actions::back
    )
}
