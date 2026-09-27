package com.wafflehq.lib.settings.onboarding.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.R as UiCoreR
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.navigation.settings.IntroIconBadge
import com.wafflehq.lib.navigation.settings.SettingsScaffold
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.onboarding.ModuleOnboardingContent
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.navigation.R as NavR
import com.wafflehq.lib.uicore.components.AppLinearProgress

@Composable
fun ModuleOnboardingScreen(
    content: ModuleOnboardingContent,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    stateKey: String = content.nameRes.toString(),
) {
    var pageIndex by rememberSaveable(stateKey) { mutableIntStateOf(0) }
    val totalPages = content.pages.size
    val index = pageIndex.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
    val page = content.pages.getOrNull(index) ?: return
    val isFirstPage = index == 0
    val isLastPage = index == totalPages - 1

    SettingsScaffold(
        title = stringResource(content.nameRes),
        onBack = onBack,
        backDescription = stringResource(NavR.string.navigation_navigate_back),
        modifier = modifier,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                AppButton(
                    text = stringResource(
                        if (isLastPage) R.string.appsettings_onboarding_finish
                        else R.string.appsettings_onboarding_next
                    ),
                    role = AppButtonRole.Primary,
                    onClick = { if (isLastPage) onBack() else pageIndex = index + 1 },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (!isFirstPage) {
                    AppButton(
                        text = stringResource(UiCoreR.string.uicore_back),
                        role = AppButtonRole.Neutral,
                        variant = AppButtonVariant.Outlined,
                        onClick = { pageIndex = index - 1 },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (!isLastPage) {
                    AppButton(
                        text = stringResource(R.string.appsettings_module_onboarding_skip),
                        role = AppButtonRole.Neutral,
                        variant = AppButtonVariant.Text,
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(OnboardingTestTags.SKIP),
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xl),
            verticalArrangement = Arrangement.Center
        ) {
            AppLinearProgress(
                progress = { (index + 1).toFloat() / totalPages },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppRadius.pill))
                    .height(4.dp)
                    .testTag(OnboardingTestTags.PROGRESS),
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.appsettings_onboarding_page_progress, index + 1, totalPages),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(AppSpacing.md))
            IntroIconBadge(content.icon)
            Spacer(Modifier.height(AppSpacing.xl))
            Text(
                text = stringResource(page.titleRes),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(AppSpacing.sm))
            Text(
                text = stringResource(page.bodyRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(AppSpacing.lg))
            Text(
                text = stringResource(content.permissionRes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
