package com.wafflehq.uikit.textarea

import android.app.Application
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
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
@Config(application = Application::class, sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class KeyboardAwareTextAreaTest {

    @get:Rule
    val rule = createComposeRule()

    private val screenHeight = 640.dp

    @Test
    fun typingPropagatesStringValue() {
        var value by mutableStateOf("")
        rule.setContent {
            KeyboardAwareTextArea(value = value, onValueChange = { value = it }, modifier = Modifier.testTag(TAG))
        }

        rule.onNodeWithTag(TAG).performTextInput("Hello")

        rule.runOnIdle { assertEquals("Hello", value) }
        rule.onNodeWithTag(TAG).assertTextEquals("Hello")
    }

    @Test
    fun typingPropagatesTextFieldValue() {
        var value by mutableStateOf(TextFieldValue(""))
        rule.setContent {
            KeyboardAwareTextArea(value = value, onValueChange = { value = it }, modifier = Modifier.testTag(TAG))
        }

        rule.onNodeWithTag(TAG).performTextInput("Note")

        rule.runOnIdle {
            assertEquals("Note", value.text)
            assertEquals(TextRange(4), value.selection)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun selectionOnlyChangeDoesNotInvokeStringCallback() {
        var callbacks = 0
        var value by mutableStateOf("Hello")
        rule.setContent {
            KeyboardAwareTextArea(
                value = value,
                onValueChange = { value = it; callbacks++ },
                modifier = Modifier.testTag(TAG)
            )
        }

        rule.onNodeWithTag(TAG).requestFocus()
        rule.onNodeWithTag(TAG).performTextInputSelection(TextRange(2))

        rule.runOnIdle { assertEquals(0, callbacks) }
    }

    @Test
    fun labelAndPlaceholderAreRendered() {
        rule.setContent {
            Column {
                KeyboardAwareTextArea(value = "", onValueChange = {}, label = { Text("Label") })
                KeyboardAwareTextArea(value = "", onValueChange = {}, placeholder = { Text("Placeholder") })
            }
        }

        rule.onNodeWithText("Label").assertExists()
        rule.onNodeWithText("Placeholder").assertExists()
    }

    @Test
    fun labelNodeAcceptsTextInputLikeOutlinedTextField() {
        var value by mutableStateOf("")
        rule.setContent {
            KeyboardAwareTextArea(value = value, onValueChange = { value = it }, label = { Text("Notes") })
        }

        rule.onNodeWithText("Notes").performTextInput("Typed")

        rule.runOnIdle { assertEquals("Typed", value) }
    }

    @Test
    fun errorStateExposesErrorSemantics() {
        rule.setContent {
            KeyboardAwareTextArea(value = "", onValueChange = {}, isError = true, modifier = Modifier.testTag(TAG))
        }

        rule.onNodeWithTag(TAG).assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
    }

    @Test
    fun nonErrorStateHasNoErrorSemantics() {
        rule.setContent {
            KeyboardAwareTextArea(value = "", onValueChange = {}, modifier = Modifier.testTag(TAG))
        }

        rule.onNodeWithTag(TAG).assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
    }

    @Test
    fun enabledFlagIsReflectedInSemantics() {
        rule.setContent {
            Column {
                KeyboardAwareTextArea(value = "", onValueChange = {}, modifier = Modifier.testTag(TAG))
                KeyboardAwareTextArea(value = "", onValueChange = {}, enabled = false, modifier = Modifier.testTag(DISABLED_TAG))
            }
        }

        rule.onNodeWithTag(TAG).assertIsEnabled()
        rule.onNodeWithTag(DISABLED_TAG).assertIsNotEnabled()
    }

    @Test
    fun readOnlyFieldStaysEnabledAndShowsValue() {
        var value by mutableStateOf("fixed")
        rule.setContent {
            KeyboardAwareTextArea(value = value, onValueChange = { value = it }, readOnly = true, modifier = Modifier.testTag(TAG))
        }

        rule.onNodeWithTag(TAG).assertIsEnabled()
        rule.onNodeWithTag(TAG).assertTextEquals("fixed")
    }

    @Test
    fun typingWithTheCursorOnScreenDoesNotScroll() {
        val scrollState = ScrollState(0)
        var value by mutableStateOf("")
        rule.setContent {
            ScrollableHost(scrollState, spacerAbove = 0.dp) {
                KeyboardAwareTextArea(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth().testTag(TAG)
                )
            }
        }

        rule.onNodeWithTag(TAG).performTextInput("abc\nd\ne")
        rule.waitForIdle()

        rule.runOnIdle { assertEquals(0, scrollState.value) }
    }

    @Test
    fun cursorBelowTheScreenScrollsOnlyDownToTheBottomEdge() {
        val scrollState = ScrollState(0)
        var value by mutableStateOf("")
        rule.setContent {
            ScrollableHost(scrollState, spacerAbove = screenHeight - 20.dp) {
                KeyboardAwareTextArea(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth().testTag(TAG)
                )
            }
        }

        rule.onNodeWithTag(TAG).performTextInput("a")
        rule.waitForIdle()

        val screenPx = with(rule.density) { screenHeight.toPx() }
        val top = rule.onNodeWithTag(TAG).fetchSemanticsNode().boundsInRoot.top
        rule.runOnIdle {
            assertTrue("expected a scroll, was ${scrollState.value}", scrollState.value > 0)
            assertTrue("field top $top must be on screen (< $screenPx)", top < screenPx)
            assertTrue(
                "field top $top must stay near the bottom edge, not be scrolled up further",
                top > screenPx * 0.6f
            )
        }
    }

    @Test
    fun remeasureWithoutTextChangeDoesNotScroll() {
        val scrollState = ScrollState(0)
        var value by mutableStateOf("")
        var hostWidth by mutableStateOf(360.dp)
        rule.setContent {
            Box(Modifier.size(width = hostWidth, height = screenHeight)) {
                Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
                    KeyboardAwareTextArea(
                        value = value,
                        onValueChange = { value = it },
                        modifier = Modifier.fillMaxWidth().testTag(TAG)
                    )
                    Spacer(Modifier.height(1200.dp))
                }
            }
        }

        rule.onNodeWithTag(TAG).performTextInput("abc")
        rule.waitForIdle()
        rule.runOnIdle { scrollState.dispatchRawDelta(500f) }
        rule.waitForIdle()
        val scrolledAway = rule.runOnIdle { scrollState.value }
        assertTrue("field must be scrolled out of view", scrolledAway > 0)

        repeat(3) {
            hostWidth = if (hostWidth == 360.dp) 340.dp else 360.dp
            rule.waitForIdle()
        }

        rule.runOnIdle {
            assertEquals("re-measure must not move the scroll", scrolledAway, scrollState.value)
        }
    }

    @Test
    fun unfocusedProgrammaticGrowthDoesNotScroll() {
        val scrollState = ScrollState(0)
        var value by mutableStateOf("")
        rule.setContent {
            ScrollableHost(scrollState, spacerAbove = screenHeight - 20.dp) {
                KeyboardAwareTextArea(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth().testTag(TAG)
                )
            }
        }

        value = "1\n2\n3\n4\n5\n6\n7\n8"
        rule.waitForIdle()

        rule.runOnIdle { assertEquals(0, scrollState.value) }
    }

    @Test
    fun minLinesReservesHeightForEmptyField() {
        rule.setContent {
            Column {
                KeyboardAwareTextArea(value = "", onValueChange = {}, modifier = Modifier.testTag(TAG))
                KeyboardAwareTextArea(value = "", onValueChange = {}, minLines = 4, modifier = Modifier.testTag(TALL_TAG))
            }
        }

        val single = rule.onNodeWithTag(TAG).fetchSemanticsNode().size.height
        val tall = rule.onNodeWithTag(TALL_TAG).fetchSemanticsNode().size.height
        assertTrue("minLines field ($tall) must be taller than single-line field ($single)", tall > single)
    }

    @Composable
    private fun ScrollableHost(
        scrollState: ScrollState,
        spacerAbove: Dp,
        content: @Composable () -> Unit
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
            Spacer(Modifier.height(spacerAbove))
            content()
            Spacer(Modifier.height(1200.dp))
        }
    }

    private companion object {
        const val TAG = "textarea"
        const val DISABLED_TAG = "textarea_disabled"
        const val TALL_TAG = "textarea_tall"
    }
}
