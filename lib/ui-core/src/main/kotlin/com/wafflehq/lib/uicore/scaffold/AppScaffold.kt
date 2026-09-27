package com.wafflehq.lib.uicore.scaffold

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.R
import com.wafflehq.lib.uicore.components.AppDividerTone
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.theme.AppRadius

object AppScaffoldTestTags {
    const val TOP_BAR = "app_scaffold_top_bar"
    const val BACK = "app_scaffold_back"
    const val TITLE = "app_scaffold_title"
}

object AppScaffoldDefaults {
    val topBarHeight = 56.dp
    val backButtonSize = 40.dp
    val topBarTitleStyle: TextStyle
        @Composable get() = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
}

@Composable
fun AppScaffold(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    backDescription: String? = stringResource(R.string.uicore_back),
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    containerColor: Color = MaterialTheme.colorScheme.background,
    topBarContainerColor: Color = MaterialTheme.colorScheme.surface,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    AppScaffold(
        titleContent = {
            Text(
                text = title,
                style = AppScaffoldDefaults.topBarTitleStyle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(AppScaffoldTestTags.TITLE),
            )
        },
        onBack = onBack,
        modifier = modifier,
        backDescription = backDescription,
        navigationIcon = navigationIcon,
        containerColor = containerColor,
        topBarContainerColor = topBarContainerColor,
        actions = actions,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}

@Composable
fun AppScaffold(
    titleContent: @Composable () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    backDescription: String? = stringResource(R.string.uicore_back),
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    containerColor: Color = MaterialTheme.colorScheme.background,
    topBarContainerColor: Color = MaterialTheme.colorScheme.surface,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val pill = RoundedCornerShape(AppRadius.pill)
    AppScaffold(
        topBar = {
            AppTopBar(
                title = titleContent,
                containerColor = topBarContainerColor,
                navigation = {
                    if (onBack != null) {
                        Box(
                            modifier = Modifier
                                .size(AppScaffoldDefaults.backButtonSize)
                                .clip(pill)
                                .clickable(onClick = onBack)
                                .testTag(AppScaffoldTestTags.BACK),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = navigationIcon,
                                contentDescription = backDescription,
                                tint = colors.onSurface,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    } else {
                        Box(Modifier.size(8.dp))
                    }
                },
                actions = actions,
            )
        },
        modifier = modifier,
        containerColor = containerColor,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}

@Composable
fun AppScaffold(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.background,
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = containerColor,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}

@Composable
fun AppTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    navigation: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(containerColor)
            .statusBarsPadding()
            .testTag(AppScaffoldTestTags.TOP_BAR)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppScaffoldDefaults.topBarHeight)
                .padding(start = AppSpacing.sm, end = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            navigation()
            Box(modifier = Modifier.weight(1f)) {
                ProvideTextStyle(AppScaffoldDefaults.topBarTitleStyle) { title() }
            }
            actions()
        }
        AppHorizontalDivider(tone = AppDividerTone.Subtle)
    }
}
