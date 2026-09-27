package com.wafflehq.lib.uicore.button

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.wafflehq.lib.uicore.theme.AppRadius

@Composable
fun AppFab(
    icon: ImageVector,
    contentDescription: String?,
    role: AppButtonRole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    small: Boolean = false,
) = AppFabImpl(icon, contentDescription, appButtonRoleColors(role), onClick, modifier, small)

@Composable
fun AppFab(
    icon: ImageVector,
    contentDescription: String?,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    small: Boolean = false,
) = AppFabImpl(icon, contentDescription, customButtonRoleColors(containerColor, contentColor), onClick, modifier, small)

@Composable
private fun AppFabImpl(
    icon: ImageVector,
    contentDescription: String?,
    r: ButtonRoleColors,
    onClick: () -> Unit,
    modifier: Modifier,
    small: Boolean,
) {
    val shape = RoundedCornerShape(AppRadius.pill)
    if (small) {
        SmallFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            containerColor = r.tonalBackground,
            contentColor = r.tonalContent,
        ) { Icon(icon, contentDescription) }
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            containerColor = r.tonalBackground,
            contentColor = r.tonalContent,
        ) { Icon(icon, contentDescription) }
    }
}

@Composable
fun AppExtendedFab(
    text: String,
    role: AppButtonRole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) = AppExtendedFabImpl(text, icon, appButtonRoleColors(role), onClick, modifier)

@Composable
fun AppExtendedFab(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) = AppExtendedFabImpl(text, icon, customButtonRoleColors(containerColor, contentColor), onClick, modifier)

@Composable
private fun AppExtendedFabImpl(
    text: String,
    icon: ImageVector?,
    r: ButtonRoleColors,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val shape = RoundedCornerShape(AppRadius.pill)
    if (icon != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            containerColor = r.tonalBackground,
            contentColor = r.tonalContent,
            icon = { Icon(icon, contentDescription = null) },
            text = { Text(text) },
        )
    } else {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            containerColor = r.tonalBackground,
            contentColor = r.tonalContent,
        ) { Text(text) }
    }
}
