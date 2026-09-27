package com.wafflehq.lib.settings.colors.ui

import androidx.compose.material3.MaterialTheme
import com.wafflehq.lib.uicore.components.AppLabelChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.uicore.button.LocalAppButtonWarningColors
import com.wafflehq.lib.uicore.color.ContrastLevel
import com.wafflehq.lib.uicore.color.contrastLevelForNormalText
import com.wafflehq.lib.uicore.color.contrastRatio
import java.util.Locale

object ColorContrastBadgeTestTag {
    const val ROOT = "color_contrast_badge"
}

@Composable
fun ColorContrastBadge(color: Color, counterpart: Color, counterpartLabel: String?, modifier: Modifier = Modifier) {
    val ratio = remember(color, counterpart) { contrastRatio(color, counterpart) }
    val level = remember(ratio) { contrastLevelForNormalText(ratio) }
    val warningColors = LocalAppButtonWarningColors.current

    val containerColor: Color
    val contentColor: Color
    val levelLabel: String
    when (level) {
        ContrastLevel.AAA -> {
            containerColor = MaterialTheme.colorScheme.secondaryContainer
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            levelLabel = stringResource(R.string.appsettings_color_picker_contrast_level_aaa)
        }
        ContrastLevel.AA -> {
            containerColor = warningColors.warningContainer
            contentColor = warningColors.onWarningContainer
            levelLabel = stringResource(R.string.appsettings_color_picker_contrast_level_aa)
        }
        ContrastLevel.LOW -> {
            containerColor = MaterialTheme.colorScheme.errorContainer
            contentColor = MaterialTheme.colorScheme.onErrorContainer
            levelLabel = stringResource(R.string.appsettings_color_picker_contrast_level_low)
        }
    }

    val ratioText = remember(ratio) { String.format(Locale.getDefault(), "%.1f:1", ratio) }
    val text = if (counterpartLabel != null) {
        stringResource(R.string.appsettings_color_picker_contrast_against_token, counterpartLabel, ratioText, levelLabel)
    } else {
        stringResource(R.string.appsettings_color_picker_contrast_against_text, ratioText, levelLabel)
    }

    AppLabelChip(
        text = text,
        containerColor = containerColor,
        contentColor = contentColor,
        textStyle = MaterialTheme.typography.labelSmall,
        modifier = modifier.testTag(ColorContrastBadgeTestTag.ROOT),
    )
}
