package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.theme.AppSpacing

@Composable
fun AppSectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    containerColor: Color? = null,
    contentColor: Color? = null,
    borderColor: Color? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    contentPadding: PaddingValues = PaddingValues(AppSpacing.lg),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(AppSpacing.md),
    content: @Composable ColumnScope.() -> Unit,
) {
    val body: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth().padding(contentPadding),
            verticalArrangement = verticalArrangement,
        ) {
            if (title != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    if (icon != null) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                    Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
            content()
        }
    }
    if (containerColor != null) {
        AppCard(
            containerColor = containerColor,
            contentColor = contentColor ?: MaterialTheme.colorScheme.onSurface,
            borderColor = borderColor,
            modifier = modifier.fillMaxWidth(),
            content = body,
        )
    } else {
        AppCard(modifier = modifier.fillMaxWidth(), content = body)
    }
}
