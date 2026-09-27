package com.wafflehq.lib.uicore.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.theme.AppSpacing

object AppDropdownMenuItemDefaults {

    val shape = RoundedCornerShape(AppRadius.chip)

    val outerPadding = AppSpacing.xs

    @Composable
    fun colors(selected: Boolean): MenuItemColors {
        val scheme = MaterialTheme.colorScheme
        return if (selected) {
            MenuDefaults.itemColors(
                textColor = scheme.onPrimary,
                leadingIconColor = scheme.onPrimary,
                trailingIconColor = scheme.onPrimary,
            )
        } else {
            MenuDefaults.itemColors(
                textColor = scheme.onSurface,
                leadingIconColor = scheme.onSurfaceVariant,
                trailingIconColor = scheme.onSurfaceVariant,
            )
        }
    }

    @Composable
    fun containerColor(selected: Boolean): Color {
        val scheme = MaterialTheme.colorScheme
        return if (selected) scheme.primary else Color.Transparent
    }
}

@Composable
fun AppDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    DropdownMenuItem(
        text = text,
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = AppDropdownMenuItemDefaults.outerPadding)
            .clip(AppDropdownMenuItemDefaults.shape)
            .background(AppDropdownMenuItemDefaults.containerColor(selected))
            .semantics(mergeDescendants = true) { this.selected = selected },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = AppDropdownMenuItemDefaults.colors(selected),
    )
}
