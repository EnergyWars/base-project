package com.wafflehq.lib.settings.legal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.navigation.settings.SettingsHomeEntry
import com.wafflehq.lib.navigation.settings.SettingsHomePage
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.navigation.R as NavR

@Composable
fun LegalSettingsScreen(
    entries: List<SettingsHomeEntry>,
    onBack: () -> Unit,
    title: String = stringResource(R.string.appsettings_legal_title)
) {
    SettingsHomePage(
        title = title,
        onBack = onBack,
        backDescription = stringResource(NavR.string.navigation_navigate_back),
        entries = entries
    )
}
