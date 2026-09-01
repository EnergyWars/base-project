package com.wafflehq.base.ui.theme.showcase

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.base.ui.components.SettingsListContent
import com.wafflehq.base.ui.components.SettingsTopBar

@Composable
fun Section23SettingsList() = Section(R.string.sc_s23_title, R.string.sc_s23_desc) {
    SettingsMock(R.string.sc_set_list_cap, groupCode = "23a") {
        SettingsTopBar(
            title = stringResource(R.string.settings_title),
            onBack = {},
            backDescription = stringResource(R.string.label_back),
        )
        SettingsListContent(
            showFeatures = true,
            featuresLabel = stringResource(R.string.settings_row_features),
            displayLabel = stringResource(R.string.settings_display_title),
            displaySubtitle = stringResource(R.string.settings_display_sub),
            onOpenFeatures = {},
            onOpenDisplay = {},
            featuresCode = "23a.1",
            displayCode = "23a.2",
        )
    }
}
