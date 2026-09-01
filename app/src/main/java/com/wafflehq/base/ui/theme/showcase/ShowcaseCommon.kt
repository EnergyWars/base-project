package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wafflehq.base.ui.theme.AppRadius
import com.wafflehq.base.ui.theme.AppRole
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme
import com.wafflehq.base.ui.theme.GeistMono
import com.wafflehq.base.ui.theme.RoleColors

internal val pill = RoundedCornerShape(AppRadius.pill)
internal val radiusL = RoundedCornerShape(16.dp)
internal val radiusM = RoundedCornerShape(12.dp)
internal val radiusS = RoundedCornerShape(8.dp)
internal val radiusXs = RoundedCornerShape(4.dp)

internal val mono = TextStyle(fontFamily = GeistMono)

internal fun Color.hex(): String = "#%06X".format(0xFFFFFF and toArgb())

@Composable
internal fun role(role: AppRole): RoleColors = AppTheme.colors.forRole(role)

@Composable
internal fun Section(titleRes: Int, descRes: Int, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Text(
            stringResource(titleRes),
            style = MaterialTheme.typography.headlineSmall,
            color = AppTheme.colors.onBackground,
        )
        Text(
            stringResource(descRes),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant,
        )
        content()
    }
}

@Composable
internal fun Panel(
    modifier: Modifier = Modifier,
    background: Color = AppTheme.colors.surface,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(radiusL)
            .background(background)
            .border(1.dp, AppTheme.colors.outline, radiusL)
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        content = { content() },
    )
}

@Composable
internal fun Subhead(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono.fontFamily, letterSpacing = 0.8.sp),
        color = AppTheme.colors.onSurfaceVariant,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WrapRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        content = { content() },
    )
}

@Composable
internal fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(AppTheme.colors.outline))
}

internal enum class BtnVariant { Filled, Tonal, Elevated, Outlined, Text }

@Composable
internal fun ShowcaseButton(label: String, r: AppRole, variant: BtnVariant, enabled: Boolean = true, inspectCode: String? = null) {
    val rc = role(r)
    val surface = AppTheme.colors.surface
    val alpha = if (enabled) 1f else 0.38f
    var mod = Modifier
        .heightIn(min = 40.dp)
        .clip(pill)
    val fg: Color
    when (variant) {
        BtnVariant.Filled -> {
            mod = mod.background(rc.accent.copy(alpha = alpha)); fg = rc.onAccent
        }
        BtnVariant.Tonal -> {
            mod = mod.background(rc.container.copy(alpha = alpha)); fg = rc.onContainer
        }
        BtnVariant.Elevated -> {
            mod = Modifier.heightIn(min = 40.dp).shadow(2.dp, pill).clip(pill).background(surface); fg = rc.accent
        }
        BtnVariant.Outlined -> {
            mod = mod.border(1.5.dp, rc.accent.copy(alpha = alpha), pill); fg = rc.accent
        }
        BtnVariant.Text -> {
            fg = rc.accent
        }
    }
    Box(
        modifier = mod
            .then(if (inspectCode != null) Modifier.inspectId(inspectCode) else Modifier)
            .padding(horizontal = if (variant == BtnVariant.Text) 12.dp else 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = fg.copy(alpha = alpha))
    }
}

@Composable
internal fun SettingsMock(capRes: Int, groupCode: String? = null, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Subhead(stringResource(capRes))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(radiusL)
                .border(1.dp, AppTheme.colors.outline, radiusL)
                .background(AppTheme.colors.background)
                .then(if (groupCode != null) Modifier.inspectTap(groupCode) else Modifier),
        ) {
            Column(content = { content() })
        }
    }
}
