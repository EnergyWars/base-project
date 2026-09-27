package com.wafflehq.lib.uicore.csv

import android.app.Application
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.uicore.R
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
class CsvPreviewDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val calls = mutableListOf<String>()

    private fun setContent(rows: List<List<String>>) {
        rule.setContent {
            CsvPreviewDialog(
                header = listOf("Date", "Value"),
                rows = rows,
                onDismiss = { calls += "dismiss" },
                onShare = { calls += "share" }
            )
        }
    }

    @Test
    fun `header and rows are displayed`() {
        setContent(listOf(listOf("2026-01-01", "42"), listOf("2026-01-02", "43")))

        rule.onNodeWithText(context.getString(R.string.uicore_csv_preview_title)).assertExists()
        rule.onNodeWithText("Date").assertExists()
        rule.onNodeWithText("Value").assertExists()
        rule.onNodeWithText("2026-01-01").assertExists()
        rule.onNodeWithText("43").assertExists()
        rule.onNodeWithText(context.getString(R.string.uicore_csv_preview_empty)).assertDoesNotExist()
    }

    @Test
    fun `empty rows show the empty hint instead of the table`() {
        setContent(emptyList())

        rule.onNodeWithText(context.getString(R.string.uicore_csv_preview_empty)).assertExists()
        rule.onNodeWithText("Date").assertDoesNotExist()
    }

    @Test
    fun `close and share buttons trigger their callbacks`() {
        setContent(listOf(listOf("2026-01-01", "42")))

        rule.onNodeWithContentDescription(context.getString(R.string.uicore_csv_preview_share)).performClick()
        rule.onNodeWithContentDescription(context.getString(R.string.uicore_csv_preview_close)).performClick()

        assertEquals(listOf("share", "dismiss"), calls)
    }
}
