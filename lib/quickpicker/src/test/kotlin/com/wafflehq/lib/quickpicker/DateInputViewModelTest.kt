package com.wafflehq.lib.quickpicker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DateInputViewModelTest {

    private class FakePreferences : DateInputPreferences {
        val quickTime = MutableStateFlow(true)
        val quickDate = MutableStateFlow(true)
        val format = MutableStateFlow(DateDisplayFormat.INTERNATIONAL)
        val weekday = MutableStateFlow(false)
        override val useQuickTimeInput: Flow<Boolean> = quickTime
        override val useQuickDateInput: Flow<Boolean> = quickDate
        override val dateDisplayFormat: Flow<DateDisplayFormat> = format
        override val showWeekdayInDateFields: Flow<Boolean> = weekday
    }

    private class TestViewModel(prefs: DateInputPreferences) : DateInputViewModel(prefs)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialValuesBeforeCollection_areDefaults() {
        val viewModel = TestViewModel(FakePreferences())

        assertFalse(viewModel.useQuickTimeInput.value)
        assertFalse(viewModel.useQuickDateInput.value)
        assertEquals(DateDisplayFormat.GERMAN, viewModel.dateDisplayFormat.value)
        assertTrue(viewModel.showWeekdayInDateFields.value)
    }

    @Test
    fun values_mirrorPreferencesOnceCollected() = runTest {
        val viewModel = TestViewModel(FakePreferences())

        assertTrue(viewModel.useQuickTimeInput.first { it })
        assertTrue(viewModel.useQuickDateInput.first { it })
        assertEquals(DateDisplayFormat.INTERNATIONAL, viewModel.dateDisplayFormat.first { it == DateDisplayFormat.INTERNATIONAL })
        assertFalse(viewModel.showWeekdayInDateFields.first { !it })
    }

    @Test
    fun values_followPreferenceChanges() = runTest {
        val prefs = FakePreferences()
        val viewModel = TestViewModel(prefs)
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.useQuickDateInput.collect {} }

        assertTrue(viewModel.useQuickDateInput.value)
        prefs.quickDate.value = false

        assertFalse(viewModel.useQuickDateInput.value)
        job.cancel()
    }
}
