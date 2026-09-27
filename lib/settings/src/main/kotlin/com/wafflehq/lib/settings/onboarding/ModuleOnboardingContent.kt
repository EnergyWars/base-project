package com.wafflehq.lib.settings.onboarding

import androidx.compose.ui.graphics.vector.ImageVector

data class ModuleOnboardingPage(
    val titleRes: Int,
    val bodyRes: Int,
)

data class ModuleOnboardingContent(
    val icon: ImageVector,
    val nameRes: Int,
    val pages: List<ModuleOnboardingPage>,
    val permissionRes: Int,
)
