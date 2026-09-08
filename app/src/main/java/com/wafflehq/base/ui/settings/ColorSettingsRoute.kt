package com.wafflehq.base.ui.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.wafflehq.uikit.color.ColorSettingsScreen

@Composable
fun ColorSettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    ColorSettingsScreen(
        state = viewModel.paletteState,
        onBack = onBack,
    )
}
