package com.wafflehq.lib.pdf.ui

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class PdfReaderOpenActionTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `defaults to unavailable`() {
        var action: ((File) -> Unit)? = { }
        rule.setContent { action = LocalPdfReaderOpenAction.current }

        rule.runOnIdle { assertNull(action) }
    }

    @Test
    fun `exposes the provided action`() {
        val opened = mutableListOf<File>()
        var action: ((File) -> Unit)? = null
        rule.setContent {
            CompositionLocalProvider(LocalPdfReaderOpenAction provides { file -> opened += file }) {
                action = LocalPdfReaderOpenAction.current
            }
        }

        val file = File("report.pdf")
        rule.runOnIdle { requireNotNull(action).invoke(file) }

        assertEquals(listOf(file), opened)
    }
}
