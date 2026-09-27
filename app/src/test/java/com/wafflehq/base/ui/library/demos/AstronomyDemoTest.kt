package com.wafflehq.base.ui.library.demos

import android.app.Application
import androidx.compose.ui.test.assertTextEquals
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.astronomy.MoonPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AstronomyDemoTest : LibraryDemoTest() {

    private val summerDay = LocalDate.of(2026, 6, 21)
    private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    @Test
    fun rendersSectionWithSunTimesAndMoonPhase() {
        show { AstronomyDemo(initialDate = summerDay) }

        node(DemoTags.section("astronomy")).assertExists()
        node(AstronomyTags.MOON).assertExists()
        node(AstronomyTags.DATE).assertTextEquals(summerDay.format(formatter))
        assertNoTagText(AstronomyTags.SUNRISE, string(R.string.libex_astronomy_no_time))
        assertNoTagText(AstronomyTags.SUNSET, string(R.string.libex_astronomy_no_time))
    }

    @Test
    fun polarDayShowsNoSunrise() {
        show { AstronomyDemo(initialDate = summerDay) }

        click(AstronomyTags.location(AstronomyLocation.TROMSOE))

        assertTagText(AstronomyTags.SUNRISE, string(R.string.libex_astronomy_no_time))
        assertTagText(AstronomyTags.SUNSET, string(R.string.libex_astronomy_no_time))
    }

    @Test
    fun nextAndPreviousMoveTheDate() {
        show { AstronomyDemo(initialDate = summerDay) }

        click(AstronomyTags.NEXT)
        node(AstronomyTags.DATE).assertTextEquals(summerDay.plusDays(1).format(formatter))

        click(AstronomyTags.PREVIOUS)
        click(AstronomyTags.PREVIOUS)
        node(AstronomyTags.DATE).assertTextEquals(summerDay.minusDays(1).format(formatter))
    }

    @Test
    fun snapshotForBerlinHasSunriseAndSunset() {
        val snapshot = AstronomyDemoLogic.snapshot(summerDay, AstronomyLocation.BERLIN)

        assertNotNull(snapshot.sunTimes.sunrise)
        assertNotNull(snapshot.sunTimes.sunset)
        assertTrue(snapshot.sunTimes.sunrise!!.isBefore(snapshot.sunTimes.sunset))
    }

    @Test
    fun snapshotForTromsoeInMidsummerHasNoSunrise() {
        val snapshot = AstronomyDemoLogic.snapshot(summerDay, AstronomyLocation.TROMSOE)

        assertNull(snapshot.sunTimes.sunrise)
        assertNull(snapshot.sunTimes.sunset)
    }

    @Test
    fun upcomingPhasesAreSortedAndInsideTheWindow() {
        val snapshot = AstronomyDemoLogic.snapshot(summerDay, AstronomyLocation.BERLIN)
        val dates = snapshot.upcomingPhases.map { it.first }

        assertTrue(dates.size >= 3)
        assertEquals(dates.sorted(), dates)
        assertTrue(dates.all { !it.isBefore(summerDay) && !it.isAfter(summerDay.plusDays(AstronomyDemoLogic.UPCOMING_DAYS - 1)) })
    }

    @Test
    fun everyMoonPhaseHasItsOwnLabel() {
        val labels = MoonPhase.entries.map { AstronomyDemoLogic.moonPhaseLabel(it) }

        assertEquals(MoonPhase.entries.size, labels.toSet().size)
    }
}
