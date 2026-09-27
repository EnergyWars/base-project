package com.wafflehq.lib.uicore.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppIconButtonVariant { Standard, Filled, Tonal, Outlined }

@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String?,
    role: AppButtonRole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppIconButtonVariant = AppIconButtonVariant.Standard,
    enabled: Boolean = true,
    iconRotation: Float = 0f,
    shape: Shape? = null,
    iconSize: Dp? = null,
    interactionSource: MutableInteractionSource? = null,
) = AppIconButtonImpl(icon, contentDescription, appButtonRoleColors(role), onClick, modifier, variant, enabled, iconRotation, shape, iconSize, interactionSource)

@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String?,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppIconButtonVariant = AppIconButtonVariant.Standard,
    enabled: Boolean = true,
    iconRotation: Float = 0f,
    shape: Shape? = null,
    iconSize: Dp? = null,
    interactionSource: MutableInteractionSource? = null,
) = AppIconButtonImpl(icon, contentDescription, customButtonRoleColors(containerColor, contentColor), onClick, modifier, variant, enabled, iconRotation, shape, iconSize, interactionSource)

@Composable
private fun AppIconButtonImpl(
    icon: ImageVector,
    contentDescription: String?,
    r: ButtonRoleColors,
    onClick: () -> Unit,
    modifier: Modifier,
    variant: AppIconButtonVariant,
    enabled: Boolean,
    iconRotation: Float,
    shape: Shape?,
    iconSize: Dp?,
    interactionSource: MutableInteractionSource?,
) {
    var iconModifier = if (iconRotation != 0f) Modifier.rotate(iconRotation) else Modifier
    if (iconSize != null) iconModifier = iconModifier.size(iconSize)
    val disabledBackground = MaterialTheme.colorScheme.surfaceVariant
    val disabledContent = MaterialTheme.colorScheme.onSurfaceVariant

    when (variant) {
        AppIconButtonVariant.Standard -> IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            interactionSource = interactionSource,
            shape = shape ?: IconButtonDefaults.standardShape,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) r.outlinedContent else disabledContent,
                modifier = iconModifier,
            )
        }

        AppIconButtonVariant.Filled -> FilledIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            interactionSource = interactionSource,
            shape = shape ?: IconButtonDefaults.filledShape,
            colors = IconButtonColors(
                containerColor = r.filledBackground,
                contentColor = r.filledContent,
                disabledContainerColor = disabledBackground,
                disabledContentColor = disabledContent,
            ),
        ) { Icon(imageVector = icon, contentDescription = contentDescription, modifier = iconModifier) }

        AppIconButtonVariant.Tonal -> FilledTonalIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            interactionSource = interactionSource,
            shape = shape ?: IconButtonDefaults.filledShape,
            colors = IconButtonColors(
                containerColor = r.tonalBackground,
                contentColor = r.tonalContent,
                disabledContainerColor = disabledBackground,
                disabledContentColor = disabledContent,
            ),
        ) { Icon(imageVector = icon, contentDescription = contentDescription, modifier = iconModifier) }

        AppIconButtonVariant.Outlined -> OutlinedIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            interactionSource = interactionSource,
            shape = shape ?: IconButtonDefaults.outlinedShape,
            border = BorderStroke(1.dp, if (enabled) r.outlinedBorder else disabledContent),
            colors = IconButtonColors(
                containerColor = Color.Transparent,
                contentColor = r.outlinedContent,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = disabledContent,
            ),
        ) { Icon(imageVector = icon, contentDescription = contentDescription, modifier = iconModifier) }
    }
}
