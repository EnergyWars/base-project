package com.wafflehq.base.ui.library.demos

import android.app.Application
import com.wafflehq.base.ui.library.LibraryDemoTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChartsDemoTest : LibraryDemoTest() {

    @Test
    fun rendersTheLineChartFirst() {
        show { ChartsDemo() }

        node(DemoTags.section("charts")).assertExists()
        node(ChartsTags.CHART).assertExists()
        assertTagText(ChartsTags.MINIMUM, ChartsDemoLogic.format(ChartsDemoData.mood.min()))
        assertTagText(ChartsTags.MAXIMUM, ChartsDemoLogic.format(ChartsDemoData.mood.max()))
    }

    @Test
    fun everyKindRendersItsChart() {
        ChartKind.entries.forEach { kind ->
            val summary = ChartsDemoLogic.summary(kind)
            assertTrue(summary.minimum <= summary.average && summary.average <= summary.maximum)
        }
        show { ChartsDemo() }

        ChartKind.entries.forEach { kind ->
            click(ChartsTags.kind(kind))
            node(ChartsTags.CHART).assertExists()
            assertTagText(ChartsTags.MAXIMUM, ChartsDemoLogic.format(ChartsDemoLogic.summary(kind).maximum))
        }
    }

    @Test
    fun radarSummaryUsesTheCurrentValues() {
        show { ChartsDemo(initialKind = ChartKind.RADAR) }

        assertTagText(ChartsTags.MAXIMUM, ChartsDemoLogic.format(8f))
        assertTagText(ChartsTags.MINIMUM, ChartsDemoLogic.format(4f))
    }

    @Test
    fun stackedSummaryUsesTheSegmentTotals() {
        val summary = ChartsDemoLogic.summary(ChartKind.STACKED)

        assertEquals(6f, summary.minimum, 0.001f)
        assertEquals(10f, summary.maximum, 0.001f)
    }

    @Test
    fun trendSmoothsTheMoodValues() {
        assertEquals(ChartsDemoData.mood.size, ChartsDemoData.trend.size)
        assertEquals((-2f + -1f) / 2f, ChartsDemoData.trend.first(), 0.001f)
        assertEquals((3.5f + 2.5f + 2f) / 3f, ChartsDemoData.trend[12], 0.001f)
    }

    @Test
    fun formatUsesTheGivenLocale() {
        assertEquals("1.5", ChartsDemoLogic.format(1.5f, Locale.ENGLISH))
        assertEquals("1,5", ChartsDemoLogic.format(1.5f, Locale.GERMAN))
    }
}
