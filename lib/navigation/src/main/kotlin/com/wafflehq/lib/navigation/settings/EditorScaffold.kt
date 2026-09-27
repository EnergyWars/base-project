package com.wafflehq.lib.navigation.settings

import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.R as UiCoreR
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.navigation.R
import com.wafflehq.lib.uicore.button.AppExtendedFab
import com.wafflehq.lib.uicore.components.FullScreenSubPageEffect
import com.wafflehq.lib.uicore.components.LocalReservedBottomInset
import com.wafflehq.lib.uicore.components.reportReservedBottomInset
import com.wafflehq.lib.uicore.components.AppHorizontalDivider

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
    modifier: Modifier = Modifier,
    backDescription: String = stringResource(R.string.navigation_navigate_back),
    saveLabel: String = stringResource(UiCoreR.string.uicore_save),
    showSaveBar: Boolean = true,
    accentColor: Color = editorSaveAccentColor(),
    onAccentColor: Color = editorOnSaveAccentColor(),
    containerColor: Color = MaterialTheme.colorScheme.background,
    topBarContainerColor: Color = MaterialTheme.colorScheme.surface,
    actions: @Composable RowScope.() -> Unit = {},
    backdrop: @Composable BoxScope.() -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    FullScreenSubPageEffect()
    BackHandler(onBack = onBack)
    var reservedBottomInset by remember { mutableStateOf(0.dp) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(containerColor)
            .testTag(EditorScaffoldTestTags.ROOT)
    ) {
        backdrop()
        SettingsScaffold(
            title = title,
            onBack = onBack,
            backDescription = backDescription,
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBarContainerColor = topBarContainerColor,
            actions = actions,
            snackbarHost = snackbarHost,
            bottomBar = {
                EditorSaveBar(
                    visible = showSaveBar,
                    canSave = canSave,
                    onSave = onSave,
                    label = saveLabel,
                    accentColor = accentColor,
                    onAccentColor = onAccentColor,
                    onReservedInsetChanged = { reservedBottomInset = it },
                )
            },
            content = { padding ->
                CompositionLocalProvider(LocalReservedBottomInset provides reservedBottomInset) {
                    content(padding)
                }
            },
        )
    }
}

@Composable
fun editorSaveAccentColor(): Color = MaterialTheme.colorScheme.primary

@Composable
fun editorOnSaveAccentColor(): Color = MaterialTheme.colorScheme.onPrimary

@Composable
private fun EditorSaveBar(
    visible: Boolean,
    canSave: Boolean,
    onSave: () -> Unit,
    label: String,
    accentColor: Color,
    onAccentColor: Color,
    onReservedInsetChanged: (Dp) -> Unit,
) {
    val insets = WindowInsets.navigationBars.union(WindowInsets.ime)
    if (!visible) {
        SideEffect { onReservedInsetChanged(0.dp) }
        Spacer(Modifier.fillMaxWidth().windowInsetsPadding(insets))
        return
    }
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .testTag(EditorScaffoldTestTags.SAVE_BAR)
            .reportReservedBottomInset(onReservedInsetChanged)
    ) {
        AppHorizontalDivider(color = colors.outline)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(insets)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            contentAlignment = Alignment.CenterEnd,
        ) {
            AppExtendedFab(
                text = label,
                containerColor = if (canSave) accentColor else colors.surfaceVariant,
                contentColor = if (canSave) onAccentColor else colors.onSurfaceVariant,
                onClick = { if (canSave) onSave() },
                modifier = Modifier
                    .testTag(EditorScaffoldTestTags.SAVE_BUTTON),
            )
        }
    }
}
