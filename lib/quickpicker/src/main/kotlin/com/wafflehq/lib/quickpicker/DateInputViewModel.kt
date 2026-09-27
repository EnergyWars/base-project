package com.wafflehq.lib.quickpicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

abstract class DateInputViewModel(prefs: DateInputPreferences) : ViewModel() {

    val useQuickTimeInput: StateFlow<Boolean> = prefs.useQuickTimeInput
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS), false)

    val useQuickDateInput: StateFlow<Boolean> = prefs.useQuickDateInput
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS), false)

    val dateDisplayFormat: StateFlow<DateDisplayFormat> = prefs.dateDisplayFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS), DateDisplayFormat.GERMAN)

    val showWeekdayInDateFields: StateFlow<Boolean> = prefs.showWeekdayInDateFields
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS), true)

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L
    }
}
