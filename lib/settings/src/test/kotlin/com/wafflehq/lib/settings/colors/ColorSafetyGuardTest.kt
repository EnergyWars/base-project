package com.wafflehq.lib.settings.colors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ColorSafetyGuardTest {

    private val token = ColorTokenId("test.tokenA")

    @Test
    fun `an unconfirmed change is rolled back at the deadline`() = runTest {
        val store = ColorOverrideStore(FakePreferencesDataStore(), clock = { currentTime }, confirmWindowMillis = 15_000)
        store.createTheme("A")
        store.confirmPending()
        backgroundScope.launch { ColorSafetyGuard(store) { currentTime }.run() }
        store.setOverride(token, false, ColorValue.Custom(1))
        runCurrent()

        advanceTimeBy(14_999)
        runCurrent()
        assertEquals(ColorValue.Custom(1), store.overridesFlow(false).first()[token])

        advanceTimeBy(2)
        runCurrent()
        assertNull(store.overridesFlow(false).first()[token])
        assertNull(store.pendingConfirmationFlow().first())
    }

    @Test
    fun `a confirmed change is kept`() = runTest {
        val store = ColorOverrideStore(FakePreferencesDataStore(), clock = { currentTime }, confirmWindowMillis = 15_000)
        store.createTheme("A")
        store.confirmPending()
        backgroundScope.launch { ColorSafetyGuard(store) { currentTime }.run() }
        store.setOverride(token, false, ColorValue.Custom(1))
        runCurrent()

        store.confirmPending()
        advanceTimeBy(60_000)
        runCurrent()

        assertEquals(ColorValue.Custom(1), store.overridesFlow(false).first()[token])
    }

    @Test
    fun `another edit restarts the countdown`() = runTest {
        val store = ColorOverrideStore(FakePreferencesDataStore(), clock = { currentTime }, confirmWindowMillis = 15_000)
        store.createTheme("A")
        store.confirmPending()
        backgroundScope.launch { ColorSafetyGuard(store) { currentTime }.run() }
        store.setOverride(token, false, ColorValue.Custom(1))
        runCurrent()
        advanceTimeBy(10_000)
        store.setOverride(token, false, ColorValue.Custom(2))
        runCurrent()

        advanceTimeBy(10_000)
        runCurrent()
        assertEquals(ColorValue.Custom(2), store.overridesFlow(false).first()[token])

        advanceTimeBy(6_000)
        runCurrent()
        assertNull(store.overridesFlow(false).first()[token])
    }

    @Test
    fun `a window that expired while the app was closed is rolled back immediately on start`() = runTest {
        var now = 0L
        val dataStore = FakePreferencesDataStore()
        val store = ColorOverrideStore(dataStore, clock = { now }, confirmWindowMillis = 15_000)
        store.createTheme("A")
        store.confirmPending()
        store.setOverride(token, false, ColorValue.Custom(1))
        now = 1_000_000

        backgroundScope.launch { ColorSafetyGuard(store) { now }.run() }
        runCurrent()

        assertNull(store.overridesFlow(false).first()[token])
    }
}
