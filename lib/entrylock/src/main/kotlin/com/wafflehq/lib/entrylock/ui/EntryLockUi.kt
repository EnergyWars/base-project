package com.wafflehq.lib.entrylock.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.wafflehq.lib.entrylock.R
import com.wafflehq.lib.entrylock.AuthResult
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppIconButton
import kotlinx.coroutines.launch

@Composable
fun LockedEntryRow(
    dateText: String,
    timeText: String? = null,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (timeText != null) stringResource(R.string.entry_lock_date_time_format, dateText, timeText) else dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        trailingContent?.invoke()
    }
}

@Composable
fun EntrySelectionTopBarActions(
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onLockSelected: () -> Unit,
    onUnlockSelected: () -> Unit,
    contentColor: Color = LocalContentColor.current
) {
    AppIconButton(
        icon = Icons.Default.SelectAll,
        contentDescription = stringResource(R.string.entry_lock_select_all),
        containerColor = contentColor,
        contentColor = contentColor,
        onClick = onSelectAll,
    )
    AppIconButton(
        icon = Icons.Default.Lock,
        contentDescription = stringResource(R.string.entry_lock_bulk_lock),
        containerColor = contentColor,
        contentColor = contentColor,
        onClick = onLockSelected,
        enabled = selectedCount > 0,
    )
    AppIconButton(
        icon = Icons.Default.LockOpen,
        contentDescription = stringResource(R.string.entry_lock_bulk_unlock),
        containerColor = contentColor,
        contentColor = contentColor,
        onClick = onUnlockSelected,
        enabled = selectedCount > 0,
    )
}

@Composable
fun EntryLockStatusIcon(
    isLocked: Boolean,
    isTempRevealed: Boolean,
    selectionMode: Boolean,
    onRelock: () -> Unit
) {
    if (!isLocked) return
    if (isTempRevealed && !selectionMode) {
        AppIconButton(
            icon = Icons.Default.VisibilityOff,
            contentDescription = stringResource(R.string.entry_lock_relock_row),
            role = AppButtonRole.Neutral,
            onClick = onRelock,
            modifier = Modifier.size(28.dp),
            iconSize = 18.dp,
        )
    } else {
        Icon(
            Icons.Default.LockOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = AppSpacing.xs).size(18.dp)
        )
    }
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}

@Composable
fun entryAuthLauncher(
    authenticate: suspend (FragmentActivity, String) -> AuthResult,
    onResult: (AuthResult) -> Unit
): (String) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return { title ->
        val activity = context.findFragmentActivity()
        if (activity == null) {
            onResult(AuthResult.NOT_AVAILABLE)
        } else {
            scope.launch { onResult(authenticate(activity, title)) }
        }
    }
}
