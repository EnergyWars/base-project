package com.wafflehq.base

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.wafflehq.base.data.settings.SettingsRepository
import com.wafflehq.base.ui.navigation.AppNavHost
import com.wafflehq.base.ui.theme.AppThemedContent
import com.wafflehq.lib.settings.colors.LocalIsDarkTheme
import com.wafflehq.lib.settings.colors.ColorOverrideStore
import com.wafflehq.lib.settings.colors.ui.ColorSafetyOverlay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var colorPrefs: ColorOverrideStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialThemeMode = runBlocking { settingsRepository.themeMode.first() }
        enableEdgeToEdge()
        setContent {
            AppThemedContent(
                colorPrefs = colorPrefs,
                themeModeFlow = settingsRepository.themeMode,
                initialThemeMode = initialThemeMode
            ) {
                val isDark = LocalIsDarkTheme.current
                DisposableEffect(isDark) {
                    val barStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDark }
                    enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
                    onDispose { }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavHost()
                    }
                    ColorSafetyOverlay(store = colorPrefs, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
