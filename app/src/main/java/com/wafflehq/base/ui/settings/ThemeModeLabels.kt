package com.wafflehq.base.ui.settings

import com.wafflehq.base.R
import com.wafflehq.base.data.model.ThemeMode

val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }
