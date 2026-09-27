package com.wafflehq.lib.uicore.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.theme.AppRadius
import com.wafflehq.lib.uicore.theme.AppSpacing

enum class AppButtonVariant { Filled, Tonal, Elevated, Outlined, Text }

@Composable
fun AppButton(
    text: String,
    role: AppButtonRole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Filled,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentPadding: PaddingValues? = null,
    singleLine: Boolean = false,
    textStyle: TextStyle? = null,
    iconSize: Dp = 18.dp,
) = AppButtonImpl(
    { TextButtonContent(text, leadingIcon, trailingIcon, singleLine, textStyle, iconSize) },
    appButtonRoleColors(role), onClick,
    if (role == AppButtonRole.Error) modifier.filterObscuredTouches() else modifier,
    variant, enabled, contentPadding
)

@Composable
fun AppButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Filled,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentPadding: PaddingValues? = null,
    singleLine: Boolean = false,
    textStyle: TextStyle? = null,
    iconSize: Dp = 18.dp,
) = AppButtonImpl(
    { TextButtonContent(text, leadingIcon, trailingIcon, singleLine, textStyle, iconSize) },
    customButtonRoleColors(containerColor, contentColor), onClick, modifier, variant, enabled, contentPadding
)

@Composable
fun AppButton(
    role: AppButtonRole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Filled,
    enabled: Boolean = true,
    contentPadding: PaddingValues? = null,
    content: @Composable () -> Unit,
) = AppButtonImpl(
    content, appButtonRoleColors(role), onClick,
    if (role == AppButtonRole.Error) modifier.filterObscuredTouches() else modifier,
    variant, enabled, contentPadding
)

@Composable
fun AppButton(
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Filled,
    enabled: Boolean = true,
    contentPadding: PaddingValues? = null,
    content: @Composable () -> Unit,
) = AppButtonImpl(content, customButtonRoleColors(containerColor, contentColor), onClick, modifier, variant, enabled, contentPadding)

@Composable
private fun TextButtonContent(
    text: String,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    singleLine: Boolean,
    textStyle: TextStyle?,
    iconSize: Dp,
) {
    val label: @Composable () -> Unit = {
        when {
            textStyle != null -> Text(text, style = textStyle, maxLines = if (singleLine) 1 else Int.MAX_VALUE, softWrap = !singleLine, overflow = TextOverflow.Clip)
            singleLine -> Text(text, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip)
            else -> Text(text)
        }
    }
    if (leadingIcon != null || trailingIcon != null) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(iconSize))
            label()
            if (trailingIcon != null) Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(iconSize))
        }
    } else {
        label()
    }
}

@Composable
private fun AppButtonImpl(
    content: @Composable () -> Unit,
    r: ButtonRoleColors,
    onClick: () -> Unit,
    modifier: Modifier,
    variant: AppButtonVariant,
    enabled: Boolean,
    contentPadding: PaddingValues?,
) {
    val disabledBackground = MaterialTheme.colorScheme.surfaceVariant
    val disabledContent = MaterialTheme.colorScheme.onSurfaceVariant
    val shape = RoundedCornerShape(AppRadius.button)
    val resolvedPadding = contentPadding
        ?: if (variant == AppButtonVariant.Text) ButtonDefaults.TextButtonContentPadding else ButtonDefaults.ContentPadding

    when (variant) {
        AppButtonVariant.Filled -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            contentPadding = resolvedPadding,
            colors = ButtonColors(
                containerColor = r.filledBackground,
                contentColor = r.filledContent,
                disabledContainerColor = disabledBackground,
                disabledContentColor = disabledContent,
            ),
        ) { content() }

        AppButtonVariant.Tonal -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            contentPadding = resolvedPadding,
            colors = ButtonColors(
                containerColor = r.tonalBackground,
                contentColor = r.tonalContent,
                disabledContainerColor = disabledBackground,
                disabledContentColor = disabledContent,
            ),
            border = BorderStroke(1.dp, if (enabled) r.tonalBorder else disabledContent),
        ) { content() }

        AppButtonVariant.Elevated -> ElevatedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            contentPadding = resolvedPadding,
            colors = ButtonColors(
                containerColor = r.elevatedBackground,
                contentColor = r.elevatedContent,
                disabledContainerColor = disabledBackground,
                disabledContentColor = disabledContent,
            ),
        ) { content() }

        AppButtonVariant.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            contentPadding = resolvedPadding,
            colors = ButtonColors(
                containerColor = Color.Transparent,
                contentColor = r.outlinedContent,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = disabledContent,
            ),
            border = BorderStroke(1.dp, if (enabled) r.outlinedBorder else disabledContent),
        ) { content() }

        AppButtonVariant.Text -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            contentPadding = resolvedPadding,
            colors = ButtonColors(
                containerColor = Color.Transparent,
                contentColor = r.textContent,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = disabledContent,
            ),
        ) { content() }
    }
}
