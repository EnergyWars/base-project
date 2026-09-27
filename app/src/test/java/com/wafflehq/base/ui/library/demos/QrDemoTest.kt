package com.wafflehq.base.ui.library.demos

import android.app.Application
import com.wafflehq.base.ui.library.LibraryDemoTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class QrDemoTest : LibraryDemoTest() {

    @Test
    fun rendersTheDefaultCode() {
        show { QrDemo() }

        node(DemoTags.section("qr")).assertExists()
        node(QrTags.IMAGE).assertExists()
        node(QrTags.EMPTY).assertDoesNotExist()
    }

    @Test
    fun blankInputShowsTheHintInsteadOfAnImage() {
        show { QrDemo() }

        replaceText(QrTags.INPUT, "   ")

        node(QrTags.EMPTY).assertExists()
        node(QrTags.IMAGE).assertDoesNotExist()
    }

    @Test
    fun newTextStillProducesAnImage() {
        show { QrDemo(initialText = "") }
        node(QrTags.EMPTY).assertExists()

        replaceText(QrTags.INPUT, "waffle")

        node(QrTags.IMAGE).assertExists()
        node(QrTags.EMPTY).assertDoesNotExist()
    }

    @Test
    fun renderReturnsSquareBitmapForValidText() {
        val bitmap = QrDemoLogic.render("hello", 120)

        assertNotNull(bitmap)
        assertEquals(120, bitmap!!.width)
        assertEquals(120, bitmap.height)
    }

    @Test
    fun renderRejectsBlankAndOversizedText() {
        assertNull(QrDemoLogic.render(""))
        assertNull(QrDemoLogic.render("   "))
        assertNull(QrDemoLogic.render("x".repeat(QrDemoLogic.MAX_TEXT_LENGTH + 1)))
    }
}
