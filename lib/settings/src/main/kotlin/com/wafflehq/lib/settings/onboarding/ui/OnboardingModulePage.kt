package com.wafflehq.lib.settings.onboarding.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.navigation.settings.IntroIconBadge
import com.wafflehq.lib.navigation.settings.SettingsSwitchRow
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.uicore.components.AppCard

@Composable
fun OnboardingModulePage(
    icon: ImageVector,
    name: String,
    description: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    permissionHint: String? = null,
    progressLabel: String? = null,
    activateLabel: String = stringResource(R.string.appsettings_onboarding_activate_module),
    nextLabel: String = stringResource(R.string.appsettings_onboarding_next),
    skipLabel: String = stringResource(R.string.appsettings_onboarding_skip),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.xl),
        verticalArrangement = Arrangement.Center
    ) {
        if (progressLabel != null) {
            Text(
                text = progressLabel,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(AppSpacing.md))
        }
        IntroIconBadge(icon)
        Spacer(Modifier.height(AppSpacing.xl))
        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (permissionHint != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = permissionHint,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(AppSpacing.xl))
        AppCard(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)) {
                SettingsSwitchRow(
                    title = activateLabel,
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.xxl))
        OnboardingButtonRow(
            primaryLabel = nextLabel,
            onPrimary = onNext,
            secondaryLabel = skipLabel,
            onSecondary = onSkip
        )
    }
}
