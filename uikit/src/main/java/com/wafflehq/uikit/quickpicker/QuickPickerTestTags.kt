package com.wafflehq.uikit.quickpicker

import java.time.LocalDate

object QuickPickerTestTags {
    const val CONFIRM_BUTTON = "quickpicker_confirm"
    const val CANCEL_BUTTON = "quickpicker_cancel"
    const val ALL_DAY_BUTTON = "quickpicker_all_day"
    const val NOT_NOW_BUTTON = "quickpicker_not_now"
    const val NOW_CHIP = "quickpicker_now"
    const val TODAY_BUTTON = "quickpicker_today_button"
    const val LAST_WEEK_BUTTON = "quickpicker_last_week_button"
    const val NEXT_WEEK_BUTTON = "quickpicker_next_week_button"
    const val TODAY_SIGN_TOGGLE = "quickpicker_today_sign"
    const val RESTORE_BUTTON = "quickpicker_restore"
    const val RELATIVE_TOGGLE_BUTTON = "quickpicker_relative_toggle"
    const val PREVIOUS_DAY_ARROW = "quickpicker_prev_day"
    const val NEXT_DAY_ARROW = "quickpicker_next_day"
    const val TIME_FIELD = "quickpicker_time_field"
    const val TIME_DIGIT_DISPLAY = "quickpicker_time_digit_display"
    const val DATE_DISPLAY = "quickpicker_date_display"
    const val DATE_PREVIEW = "quickpicker_date_preview"
    const val BACKSPACE = "quickpicker_backspace"
    const val SEPARATOR = "quickpicker_separator"
    const val DIGIT_PREFIX = "quickpicker_digit_"
    const val DAY_CHIP_PREFIX = "quickpicker_day_"

    fun digit(value: Int): String = "$DIGIT_PREFIX$value"

    fun dayChip(day: LocalDate): String = "$DAY_CHIP_PREFIX${day.toEpochDay()}"
}
