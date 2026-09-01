package com.wafflehq.base.ui.theme.showcase

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.base.data.settings.ThemeMode
import com.wafflehq.base.ui.components.SettingsTopBar
import com.wafflehq.base.ui.settings.DisplaySettingsContent

@Composable
fun Section24SettingsDetail() = Section(R.string.sc_s24_title, R.string.sc_s24_desc) {
    var mockTheme by remember { mutableStateOf(ThemeMode.SYSTEM) }
    SettingsMock(R.string.sc_set_detail_cap, groupCode = "24a") {
        SettingsTopBar(
            title = stringResource(R.string.settings_display_title),
            onBack = {},
            backDescription = stringResource(R.string.label_back),
        )
        DisplaySettingsContent(
            themeMode = mockTheme,
            onThemeSelected = { mockTheme = it },
            groupCodes = listOf("24a.1", "24a.2", "24a.3"),
        )
    }
}
