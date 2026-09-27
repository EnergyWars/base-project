package com.wafflehq.lib.settings.onboarding.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.components.AppLinearProgress
import com.wafflehq.lib.uicore.scaffold.AppScaffold

object OnboardingTestTags {
    const val PROGRESS = "onboarding_progress"
    const val SKIP = "onboarding_skip"
}

@Composable
fun OnboardingScaffold(
    pageIndex: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AppScaffold(
        topBar = {},
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            AppLinearProgress(
                progress = { if (pageCount <= 0) 0f else (pageIndex + 1).toFloat() / pageCount },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg)
                    .clip(RoundedCornerShape(AppRadius.pill))
                    .height(4.dp)
                    .testTag(OnboardingTestTags.PROGRESS),
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            content()
        }
    }
}

@Composable
fun OnboardingButtonRow(
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        AppButton(
            text = primaryLabel,
            role = AppButtonRole.Primary,
            onClick = onPrimary,
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(
            text = secondaryLabel,
            role = AppButtonRole.Neutral,
            variant = AppButtonVariant.Outlined,
            onClick = onSecondary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
