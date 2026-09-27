package com.wafflehq.lib.quickpicker

import kotlinx.coroutines.flow.Flow

interface DateInputPreferences {
    val useQuickTimeInput: Flow<Boolean>
    val useQuickDateInput: Flow<Boolean>
    val dateDisplayFormat: Flow<DateDisplayFormat>
    val showWeekdayInDateFields: Flow<Boolean>
}
