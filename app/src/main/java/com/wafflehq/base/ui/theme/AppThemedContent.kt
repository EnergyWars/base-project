package com.wafflehq.base.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.base.data.model.ThemeMode
import com.wafflehq.lib.settings.colors.ColorOverrideStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private val SystemThemeModeFlow: Flow<ThemeMode> = flowOf(ThemeMode.SYSTEM)

@Composable
fun AppThemedContent(
    colorPrefs: ColorOverrideStore,
    themeModeFlow: Flow<ThemeMode> = SystemThemeModeFlow,
    initialThemeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val themeMode by themeModeFlow.collectAsState(initial = initialThemeMode)
    val colorOverridesLight by colorPrefs.overridesFlow(isDark = false).collectAsStateWithLifecycle(emptyMap())
    val colorOverridesDark by colorPrefs.overridesFlow(isDark = true).collectAsStateWithLifecycle(emptyMap())
    val baseRampOverrides by colorPrefs.baseRampOverridesFlow().collectAsStateWithLifecycle(emptyMap())
    BaseAppTheme(
        themeMode = themeMode,
        colorOverridesLight = colorOverridesLight,
        colorOverridesDark = colorOverridesDark,
        baseRampOverrides = baseRampOverrides,
        content = content
    )
}
