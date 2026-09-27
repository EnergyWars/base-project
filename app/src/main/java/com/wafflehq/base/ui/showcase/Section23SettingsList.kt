package com.wafflehq.base.ui.showcase

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R

@Composable
fun Section23SettingsList() = Section(R.string.sc_s23_title, R.string.sc_s23_desc) {
    SettingsMock(R.string.sc_set_list_cap, groupCode = "23a") {
        MockSettingsTopBar(
            title = stringResource(R.string.settings_title),
            onBack = {},
            backDescription = stringResource(R.string.label_back),
        )
        MockSettingsListContent(
            featuresLabel = stringResource(R.string.settings_row_features),
            displayLabel = stringResource(R.string.settings_display_title),
            displaySubtitle = stringResource(R.string.settings_display_sub),
            featuresCode = "23a.1",
            displayCode = "23a.2",
        )
    }
}
