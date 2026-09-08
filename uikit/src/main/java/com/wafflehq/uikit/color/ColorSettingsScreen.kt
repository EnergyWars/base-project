package com.wafflehq.uikit.color

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.uikit.R
import com.wafflehq.uikit.theme.AppRole
import com.wafflehq.uikit.theme.AppSpacing
import com.wafflehq.uikit.theme.AppTheme
import com.wafflehq.uikit.theme.ColorRamp

private fun AppRole.labelRes(): Int = when (this) {
    AppRole.Primary -> R.string.uikit_role_primary
    AppRole.Secondary -> R.string.uikit_role_secondary
    AppRole.Tertiary -> R.string.uikit_role_tertiary
    AppRole.Success -> R.string.uikit_role_success
    AppRole.Warning -> R.string.uikit_role_warning
    AppRole.Error -> R.string.uikit_role_error
    AppRole.Neutral -> R.string.uikit_role_neutral
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSettingsScreen(
    state: WafflePaletteState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    topBarActions: @Composable RowScope.() -> Unit = {},
) {
    var editingRole by remember { mutableStateOf<AppRole?>(null) }
    var confirmResetAll by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.uikit_color_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.inspect_close))
                    }
                },
                actions = topBarActions,
            )
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(padding)) {
            items(AppRole.entries) { role ->
                RoleColorRow(role = role, ramp = state.palette.forRole(role), onClick = { editingRole = role })
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(AppSpacing.lg),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(onClick = { confirmResetAll = true }) {
                        Text(stringResource(R.string.uikit_color_reset_all))
                    }
                }
            }
        }
    }

    editingRole?.let { role ->
        RampAccentPickerDialog(
            role = role,
            initialAccent = state.palette.forRole(role)["40"],
            onColorChanged = { state.setRoleAccent(role, it) },
            onReset = { state.resetRole(role) },
            onDismiss = { editingRole = null },
        )
    }

    if (confirmResetAll) {
        AlertDialog(
            onDismissRequest = { confirmResetAll = false },
            title = { Text(stringResource(R.string.uikit_color_reset_all_confirm_title)) },
            text = { Text(stringResource(R.string.uikit_color_reset_all_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { state.resetAll(); confirmResetAll = false }) {
                    Text(stringResource(R.string.uikit_color_reset_all))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmResetAll = false }) {
                    Text(stringResource(R.string.inspect_close))
                }
            },
        )
    }
}

@Composable
private fun RoleColorRow(
    role: AppRole,
    ramp: ColorRamp,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ramp["40"]),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(role.labelRes()), style = MaterialTheme.typography.bodyLarge, color = AppTheme.colors.onSurface)
            Text(ramp["40"].toHexArgb(), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun RampAccentPickerDialog(
    role: AppRole,
    initialAccent: Color,
    onColorChanged: (Color) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(role.labelRes())) },
        text = {
            Column {
                ColorCanvasPicker(initialColor = initialAccent, onColorChanged = onColorChanged)
            }
        },
        shape = RoundedCornerShape(28.dp),
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.uikit_color_done)) }
        },
        dismissButton = {
            TextButton(onClick = { onReset(); onDismiss() }) { Text(stringResource(R.string.uikit_color_reset_token)) }
        },
    )
}
