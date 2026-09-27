package com.wafflehq.lib.settings.colors.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.settings.colors.ColorOverrideStore
import com.wafflehq.lib.settings.colors.ColorSafetyGuard
import com.wafflehq.lib.settings.colors.ColorSafetyPending
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppBanner
import com.wafflehq.lib.uicore.components.AppBannerDefaults
import com.wafflehq.lib.uicore.components.AppBannerVariant
import com.wafflehq.lib.uicore.theme.AppSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ColorSafetyTestTags {
    const val BANNER = "color_safety_banner"
    const val KEEP = "color_safety_keep"
    const val REVERT = "color_safety_revert"
    const val COUNTDOWN = "color_safety_countdown"
}

private val SafetyContainer = Color.Black
private val SafetyContent = Color.White

private const val COUNTDOWN_TICK_MILLIS = 250L

@Composable
fun ColorSafetyOverlay(
    store: ColorOverrideStore,
    modifier: Modifier = Modifier,
    clock: () -> Long = System::currentTimeMillis
) {
    val scope = rememberCoroutineScope()
    val pending by remember(store) { store.pendingConfirmationFlow() }.collectAsStateWithLifecycle(initialValue = null)
    LaunchedEffect(store) { ColorSafetyGuard(store, clock).run() }

    Box(modifier = modifier.fillMaxWidth()) {
        pending?.let { current ->
            ColorSafetyBanner(
                pending = current,
                clock = clock,
                onKeep = { scope.launch { store.confirmPending() } },
                onRevert = { scope.launch { store.revertPending() } },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun ColorSafetyBanner(
    pending: ColorSafetyPending,
    clock: () -> Long,
    onKeep: () -> Unit,
    onRevert: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingMillis by remember(pending) { mutableLongStateOf((pending.deadlineMillis - clock()).coerceAtLeast(0L)) }
    LaunchedEffect(pending) {
        while (remainingMillis > 0L) {
            delay(COUNTDOWN_TICK_MILLIS)
            remainingMillis = (pending.deadlineMillis - clock()).coerceAtLeast(0L)
        }
    }
    val seconds = ((remainingMillis + 999L) / 1000L).toInt()

    AppBanner(
        text = stringResource(R.string.appsettings_color_safety_message, seconds),
        containerColor = SafetyContainer,
        contentColor = SafetyContent,
        variant = AppBannerVariant.Card,
        title = stringResource(R.string.appsettings_color_safety_title),
        modifier = modifier
            .navigationBarsPadding()
            .padding(AppSpacing.lg)
            .shadow(8.dp, AppBannerDefaults.cardShape)
            .testTag(ColorSafetyTestTags.BANNER),
        contentPadding = PaddingValues(AppSpacing.lg),
        textModifier = Modifier.testTag(ColorSafetyTestTags.COUNTDOWN),
        footer = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm)
            ) {
                AppButton(
                    text = stringResource(R.string.appsettings_color_safety_keep),
                    containerColor = SafetyContent,
                    contentColor = SafetyContainer,
                    onClick = onKeep,
                    modifier = Modifier.weight(1f).testTag(ColorSafetyTestTags.KEEP)
                )
                AppButton(
                    text = stringResource(R.string.appsettings_color_safety_revert),
                    containerColor = SafetyContent,
                    contentColor = SafetyContainer,
                    onClick = onRevert,
                    variant = AppButtonVariant.Outlined,
                    modifier = Modifier.weight(1f).testTag(ColorSafetyTestTags.REVERT)
                )
            }
        },
    )
}
