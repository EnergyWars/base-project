package com.wafflehq.base.ui.library.demos

import android.app.Application
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.uicore.R as UiCoreR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UiCoreDataDemoTest : LibraryDemoTest() {

    @Test
    fun parsesTheDefaultCsvAndNeutralizesFormulas() {
        show { UiCoreDataDemo() }

        node(DemoTags.section("uicore_data")).assertExists()
        assertTagText(UiCoreDataTags.CSV_ROWS, string(R.string.libex_uicore_csv_parsed, 3))
        assertTagText(UiCoreDataTags.CSV_ENCODED, "'=SUM(A1),ok")
    }

    @Test
    fun editingTheCsvUpdatesTheParsedRows() {
        show { UiCoreDataDemo() }

        replaceText(UiCoreDataTags.CSV_INPUT, "a,b\n1,2\n3,4\n5,6")

        assertTagText(UiCoreDataTags.CSV_ROWS, string(R.string.libex_uicore_csv_parsed, 4))
    }

    @Test
    fun previewDialogSharesTheCsv() {
        show { UiCoreDataDemo() }

        click(UiCoreDataTags.CSV_PREVIEW)
        rule.onNode(hasContentDescription(string(UiCoreR.string.uicore_csv_preview_share))).performClick()

        waitForTag(UiCoreDataTags.PREVIEW_STATUS)
        assertTagCount(UiCoreDataTags.PREVIEW_STATUS, 1)
    }

    @Test
    fun formulaGuardPrefixesTriggerCharacters() {
        show { UiCoreDataDemo() }

        replaceText(UiCoreDataTags.FORMULA_INPUT, "@cmd")

        assertTagText(UiCoreDataTags.FORMULA_RESULT, string(R.string.libex_uicore_formula_result, "'@cmd", "@cmd"))
    }

    @Test
    fun likeEscapingProtectsWildcards() {
        show { UiCoreDataDemo() }

        assertTagText(
            UiCoreDataTags.LIKE_RESULT,
            string(R.string.libex_uicore_like_result, UiCoreDataDemoLogic.likePattern("50%_off")),
        )
        assertEquals("%50\\%\\_off%", UiCoreDataDemoLogic.likePattern("50%_off"))
    }

    @Test
    fun enumCodecFallsBackToTheDefault() {
        show { UiCoreDataDemo() }
        assertTagText(UiCoreDataTags.ENUM_RESULT, DemoPriority.HIGH.name)

        replaceText(UiCoreDataTags.ENUM_INPUT, "urgent")

        assertTagText(UiCoreDataTags.ENUM_RESULT, DemoPriority.NORMAL.name)
    }

    @Test
    fun reminderListDropsInvalidEntries() {
        show { UiCoreDataDemo() }

        assertTagText(UiCoreDataTags.REMINDERS_RESULT, string(R.string.libex_uicore_reminders_result, "10, 30, 60"))
    }

    @Test
    fun csvRoundTripKeepsQuotedFieldsAndGuardsFormulas() {
        val result = UiCoreDataDemoLogic.roundTrip(UiCoreDataDemoLogic.DEFAULT_CSV)

        assertEquals(3, result.rows.size)
        assertEquals(listOf("Doe, Jane", "says \"hi\""), result.reparsed[1])
        assertEquals("'=SUM(A1)", result.reparsed[2][0])
        assertEquals("=SUM(A1)", UiCoreDataDemoLogic.restored(result.reparsed[2][0]))
    }

    @Test
    fun emptyCsvProducesNoRows() {
        val result = UiCoreDataDemoLogic.roundTrip("")

        assertTrue(result.rows.isEmpty())
        assertEquals("\n", result.encoded)
    }

    @Test
    fun priorityLookupIsCaseInsensitive() {
        assertEquals(DemoPriority.LOW, UiCoreDataDemoLogic.priority(" low "))
        assertEquals(DemoPriority.NORMAL, UiCoreDataDemoLogic.priority("nope"))
    }

    @Test
    fun exportFileNamesCarryPrefixAndExtension() {
        val (csv, json) = UiCoreDataDemoLogic.fileNames()

        assertTrue(csv.startsWith("libex_") && csv.endsWith(".csv"))
        assertTrue(json.startsWith("libex_") && json.endsWith(".json"))
    }

    @Test
    fun remindersParserKeepsOnlyPositiveNumbers() {
        assertEquals(listOf(5, 15), UiCoreDataDemoLogic.reminders("5, x, 0, -3, 15"))
        assertTrue(UiCoreDataDemoLogic.reminders("").isEmpty())
    }
}
