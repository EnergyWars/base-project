package com.wafflehq.base.ui.library.demos

import android.app.Application
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.diagnostics.DiagnosticLevel
import com.wafflehq.lib.diagnostics.DiagnosticLogEntry
import com.wafflehq.lib.diagnostics.FreezeDetector
import com.wafflehq.lib.diagnostics.FreezeTransition
import kotlinx.coroutines.test.runTest
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
class DiagnosticsDemoTest : LibraryDemoTest() {

    @Test
    fun startsWithThePlaceholder() {
        show { DiagnosticsDemo(sink = InMemoryDiagnosticSink()) }

        node(DemoTags.section("diagnostics")).assertExists()
        assertTagText(DiagnosticsTags.LOG, string(R.string.libex_diagnostics_empty))
    }

    @Test
    fun logButtonsAppendFormattedEntries() {
        val sink = InMemoryDiagnosticSink()
        show { DiagnosticsDemo(sink = sink) }

        click(DiagnosticsTags.DEBUG)
        click(DiagnosticsTags.WARN)
        click(DiagnosticsTags.ERROR)

        assertEquals(
            listOf(DiagnosticLevel.DEBUG, DiagnosticLevel.WARN, DiagnosticLevel.ERROR),
            sink.entries.map { it.level },
        )
        assertTagText(DiagnosticsTags.LOG, "[${DiagnosticsDemoLogic.TAG}] ${string(R.string.libex_diagnostics_message_debug)}")
        assertTagText(DiagnosticsTags.LOG, "WARN")
    }

    @Test
    fun clearRemovesAllEntries() {
        val sink = InMemoryDiagnosticSink()
        show { DiagnosticsDemo(sink = sink) }
        click(DiagnosticsTags.WARN)

        click(DiagnosticsTags.CLEAR)

        assertTrue(sink.entries.isEmpty())
        assertTagText(DiagnosticsTags.LOG, string(R.string.libex_diagnostics_empty))
    }

    @Test
    fun movingTheSliderPastTheThresholdLogsAFreeze() {
        val sink = InMemoryDiagnosticSink()
        show { DiagnosticsDemo(sink = sink) }

        setSlider(DiagnosticsTags.SLIDER, 4_500f)

        rule.waitUntil(5_000) { sink.entries.any { it.level == DiagnosticLevel.FREEZE } }
        assertTagText(DiagnosticsTags.FREEZE_STATE, string(R.string.libex_diagnostics_transition_started))
    }

    @Test
    fun formatterOrdersEntriesByTimestamp() {
        val entries = listOf(
            DiagnosticLogEntry(2_000L, DiagnosticLevel.WARN, "B", "second"),
            DiagnosticLogEntry(1_000L, DiagnosticLevel.DEBUG, "A", "first"),
        )

        val text = DiagnosticsDemoLogic.formatted(entries, "empty")

        assertTrue(text.indexOf("first") < text.indexOf("second"))
        assertEquals("empty", DiagnosticsDemoLogic.formatted(emptyList(), "empty"))
    }

    @Test
    fun freezeDetectorReportsStartAndResolution() {
        val detector = FreezeDetector()

        assertEquals(FreezeTransition.NONE, DiagnosticsDemoLogic.evaluate(detector, 1_000f))
        assertEquals(FreezeTransition.STARTED, DiagnosticsDemoLogic.evaluate(detector, 4_000f))
        assertEquals(FreezeTransition.NONE, DiagnosticsDemoLogic.evaluate(detector, 5_000f))
        assertEquals(FreezeTransition.RESOLVED, DiagnosticsDemoLogic.evaluate(detector, 0f))
    }

    @Test
    fun everyTransitionHasALabel() {
        val labels = FreezeTransition.entries.map { DiagnosticsDemoLogic.transitionLabel(it) }

        assertEquals(FreezeTransition.entries.size, labels.toSet().size)
    }

    @Test
    fun sinkStoresEntriesInOrder() = runTest {
        val sink = InMemoryDiagnosticSink()

        sink.log(DiagnosticLogEntry(1L, DiagnosticLevel.DEBUG, "t", "one"))
        sink.log(DiagnosticLogEntry(2L, DiagnosticLevel.ERROR, "t", "two", "trace"))

        assertEquals(listOf("one", "two"), sink.entries.map { it.message })
        assertEquals("trace", sink.entries.last().stackTrace)
    }
}
