package com.wafflehq.uikit.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.wafflehq.uikit.components.SettingsTopBar
import com.wafflehq.uikit.theme.AppTheme

object EditorScaffoldTestTags {
    const val ROOT = "editor_scaffold_root"
    const val SAVE_BAR = "editor_scaffold_save_bar"
    const val SAVE_BUTTON = "editor_scaffold_save_button"
}

@Composable
fun EditorScaffold(
    title: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean,
    backDescription: String,
    saveLabel: String,
    modifier: Modifier = Modifier,
    showSaveBar: Boolean = true,
    accentColor: Color = AppTheme.colors.primary.accent,
    onAccentColor: Color = AppTheme.colors.primary.onAccent,
    containerColor: Color = AppTheme.colors.background,
    backdrop: @Composable BoxScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    FullScreenSubPageEffect()
    BackHandler(onBack = onBack)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(containerColor)
            .testTag(EditorScaffoldTestTags.ROOT),
    ) {
        backdrop()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                Column(Modifier.fillMaxWidth().background(AppTheme.colors.surface).statusBarsPadding()) {
                    SettingsTopBar(title = title, onBack = onBack, backDescription = backDescription)
                }
            },
            bottomBar = {
                EditorSaveBar(
                    visible = showSaveBar,
                    canSave = canSave,
                    onSave = onSave,
                    label = saveLabel,
                    accentColor = accentColor,
                    onAccentColor = onAccentColor,
                )
            },
            content = content,
        )
    }
}

@Composable
private fun EditorSaveBar(
    visible: Boolean,
    canSave: Boolean,
    onSave: () -> Unit,
    label: String,
    accentColor: Color,
    onAccentColor: Color,
) {
    val insets = WindowInsets.navigationBars.union(WindowInsets.ime)
    if (!visible) {
        Box(Modifier.fillMaxWidth().windowInsetsPadding(insets))
        return
    }
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .testTag(EditorScaffoldTestTags.SAVE_BAR),
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.outline))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(insets)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            ExtendedFloatingActionButton(
                onClick = { if (canSave) onSave() },
                containerColor = if (canSave) accentColor else colors.surfaceVariant,
                contentColor = if (canSave) onAccentColor else colors.onSurfaceVariant,
                modifier = Modifier.testTag(EditorScaffoldTestTags.SAVE_BUTTON),
            ) {
                Text(label)
            }
        }
    }
}
