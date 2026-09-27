package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.theme.AppSpacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

enum class AppSectionHeaderStyle { Accent, Title, Subtle }

@Composable
fun AppSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    style: AppSectionHeaderStyle = AppSectionHeaderStyle.Accent,
    subtitle: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    pillColor: Color? = null,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    contentPadding: PaddingValues = AppSectionHeaderDefaults.accentPadding,
) {
    val outer = modifier.fillMaxWidth().background(containerColor)
    when (style) {
        AppSectionHeaderStyle.Accent -> Box(modifier = outer) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                modifier = Modifier
                    .padding(contentPadding)
                    .background(pillColor ?: Color.Transparent, RoundedCornerShape(AppRadius.pill))
                    .padding(
                        horizontal = if (pillColor != null) AppSpacing.md else 0.dp,
                        vertical = if (pillColor != null) AppSpacing.xs else 0.dp,
                    ),
            ) {
                if (leadingContent != null) leadingContent()
                Text(text = text, style = textStyle, color = contentColor)
            }
        }
        AppSectionHeaderStyle.Title -> Row(
            modifier = outer.padding(vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        AppSectionHeaderStyle.Subtle -> Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = outer.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        )
    }
}

@Composable
fun AppSectionHeader(
    date: LocalDate,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    pillColor: Color? = null,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    contentPadding: PaddingValues = AppSectionHeaderDefaults.accentPadding,
) {
    val locale = Locale.getDefault()
    val text = remember(date, locale) { AppSectionHeaderDefaults.formatDate(date, locale) }
    AppSectionHeader(
        text = text,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        pillColor = pillColor,
        leadingContent = leadingContent,
        textStyle = textStyle,
        contentPadding = contentPadding,
    )
}

object AppSectionHeaderDefaults {
    val accentPadding = PaddingValues(vertical = AppSpacing.sm, horizontal = AppSpacing.xs)

    fun formatDate(date: LocalDate, locale: Locale): String =
        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale))
}
