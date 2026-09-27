package com.wafflehq.base.ui.library.demos

import android.app.Application
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TextAreaDemoTest : LibraryDemoTest() {

    @Test
    fun startsEmptyWithACounter() {
        show { TextAreaDemo() }

        node(DemoTags.section("textarea")).assertExists()
        assertCount(string(R.string.libex_textarea_count, 0, TextAreaDemoLogic.MAX_LENGTH, 0))
        node(TextAreaTags.CLEAR).assertIsNotEnabled()
    }

    @Test
    fun typingUpdatesTheCounter() {
        show { TextAreaDemo() }

        replaceText(TextAreaTags.INPUT, "one\ntwo")

        assertCount(string(R.string.libex_textarea_count, 7, TextAreaDemoLogic.MAX_LENGTH, 2))
        node(TextAreaTags.CLEAR).assertIsEnabled()
    }

    @Test
    fun sampleButtonFillsTheFieldAndClearEmptiesIt() {
        show { TextAreaDemo() }
        val sample = string(R.string.libex_textarea_sample)

        click(TextAreaTags.FILL)
        assertCount(string(R.string.libex_textarea_count, sample.length, TextAreaDemoLogic.MAX_LENGTH, sample.lines().size))

        click(TextAreaTags.CLEAR)
        assertCount(string(R.string.libex_textarea_count, 0, TextAreaDemoLogic.MAX_LENGTH, 0))
    }

    private fun assertCount(text: String) {
        rule.onNode(hasTestTag(TextAreaTags.COUNT) and hasText(text), useUnmergedTree = true).assertExists()
    }

    @Test
    fun overLimitTextIsDetected() {
        assertFalse(TextAreaDemoLogic.isOverLimit("x".repeat(TextAreaDemoLogic.MAX_LENGTH)))
        assertTrue(TextAreaDemoLogic.isOverLimit("x".repeat(TextAreaDemoLogic.MAX_LENGTH + 1)))
    }

    @Test
    fun lineCountHandlesEmptyAndMultilineText() {
        assertEquals(0, TextAreaDemoLogic.lineCount(""))
        assertEquals(1, TextAreaDemoLogic.lineCount("a"))
        assertEquals(3, TextAreaDemoLogic.lineCount("a\nb\nc"))
    }
}
