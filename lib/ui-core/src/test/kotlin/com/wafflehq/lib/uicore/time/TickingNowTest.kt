package com.wafflehq.lib.uicore.time

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class TickingNowTest {

    @get:Rule
    val rule = createComposeRule()

    private class MutableClock(var current: Instant) : Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?): Clock = this
        override fun instant(): Instant = current
    }

    private val start = Instant.parse("2026-01-01T00:00:00Z")

    @Test
    fun emitsClockInstantOnFirstComposition() {
        val clock = MutableClock(start)
        var observed: Instant? = null

        rule.setContent {
            val now by rememberTickingNow(intervalMillis = 1_000L, clock = clock)
            observed = now
        }
        rule.waitForIdle()

        assertEquals(start, observed)
    }

    @Test
    fun refreshesAfterEachInterval() {
        val clock = MutableClock(start)
        var observed: Instant? = null

        rule.mainClock.autoAdvance = false
        rule.setContent {
            val now by rememberTickingNow(intervalMillis = 1_000L, clock = clock)
            observed = now
        }
        rule.mainClock.advanceTimeBy(100L)
        assertEquals(start, observed)

        clock.current = start.plusSeconds(60)
        rule.mainClock.advanceTimeBy(1_000L)
        assertEquals(start.plusSeconds(60), observed)

        clock.current = start.plusSeconds(120)
        rule.mainClock.advanceTimeBy(1_000L)
        assertEquals(start.plusSeconds(120), observed)
    }

    @Test
    fun doesNotTickWhileDisabled() {
        val clock = MutableClock(start)
        var observed: Instant? = null

        rule.mainClock.autoAdvance = false
        rule.setContent {
            val now by rememberTickingNow(intervalMillis = 1_000L, clock = clock, enabled = false)
            observed = now
        }
        clock.current = start.plusSeconds(60)
        rule.mainClock.advanceTimeBy(5_000L)

        assertEquals(start, observed)
    }

    @Test
    fun refreshesImmediatelyWhenEnabledTurnsOn() {
        val clock = MutableClock(start)
        var observed: Instant? = null
        var enabled by mutableStateOf(false)

        rule.mainClock.autoAdvance = false
        rule.setContent {
            val now by rememberTickingNow(intervalMillis = 60_000L, clock = clock, enabled = enabled)
            observed = now
        }
        rule.mainClock.advanceTimeBy(1_000L)
        assertEquals(start, observed)

        clock.current = start.plusSeconds(30)
        rule.runOnIdle { enabled = true }
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()

        assertEquals(start.plusSeconds(30), observed)
    }
}
