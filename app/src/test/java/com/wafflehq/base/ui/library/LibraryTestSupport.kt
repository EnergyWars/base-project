package com.wafflehq.base.ui.library

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule

abstract class LibraryDemoTest {

    @get:Rule
    val rule = createComposeRule()

    protected val context: Context get() = ApplicationProvider.getApplicationContext()

    protected fun string(@StringRes id: Int, vararg args: Any): String = context.getString(id, *args)

    protected fun show(content: @Composable () -> Unit) {
        rule.setContent {
            MaterialTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) { content() }
            }
        }
    }

    protected fun node(tag: String): SemanticsNodeInteraction = rule.onNodeWithTag(tag)

    protected fun click(tag: String) {
        node(tag).performScrollTo().performClick()
    }

    protected fun clickText(text: String) {
        rule.onNodeWithText(text).performScrollTo().performClick()
    }

    protected fun replaceText(tag: String, text: String) {
        node(tag).performScrollTo().performTextReplacement(text)
    }

    protected fun setSlider(tag: String, value: Float) {
        node(tag).performScrollTo().performSemanticsAction(SemanticsActions.SetProgress) { it(value) }
    }

    protected fun stepUp(tag: String) {
        node(tag).onChildren()[2].performScrollTo().performClick()
    }

    protected fun stepDown(tag: String) {
        node(tag).onChildren()[0].performScrollTo().performClick()
    }

    protected fun assertStepperValue(tag: String, value: String) {
        node(tag).onChildren()[1].assert(hasText(value))
    }

    protected fun tagWithText(tag: String, text: String): SemanticsMatcher =
        hasTestTag(tag) and (hasText(text, substring = true) or hasAnyDescendant(hasText(text, substring = true)))

    protected fun assertTagText(tag: String, text: String) {
        rule.onNode(tagWithText(tag, text)).assertExists()
    }

    protected fun assertNoTagText(tag: String, text: String) {
        rule.onNode(tagWithText(tag, text)).assertDoesNotExist()
    }

    protected fun waitForTag(tag: String, timeoutMs: Long = DEFAULT_TIMEOUT_MS) {
        rule.waitUntil(timeoutMs) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    }

    protected fun waitForTagText(tag: String, text: String, timeoutMs: Long = DEFAULT_TIMEOUT_MS) {
        rule.waitUntil(timeoutMs) { rule.onAllNodes(tagWithText(tag, text)).fetchSemanticsNodes().isNotEmpty() }
    }

    protected fun assertTagCount(tag: String, count: Int) {
        rule.onAllNodesWithTag(tag).assertCountEquals(count)
    }

    protected fun tagCount(tag: String): Int = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().size

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 10_000L
    }
}
