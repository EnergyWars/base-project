package com.wafflehq.lib.entrylock.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.entrylock.R
import com.wafflehq.lib.uicore.menu.AppDropdownMenu
import com.wafflehq.lib.uicore.menu.AppDropdownMenuItem

@Composable
fun EntryLockOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    hasLockedEntries: Boolean,
    sessionUnlocked: Boolean,
    onSelect: () -> Unit,
    onLockAll: () -> Unit,
    onUnlockAll: () -> Unit,
    onSessionUnlock: () -> Unit
) {
    AppDropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        AppDropdownMenuItem(
            text = { Text(stringResource(R.string.entry_lock_menu_select)) },
            leadingIcon = { Icon(Icons.Default.Checklist, contentDescription = null) },
            onClick = {
                onDismiss()
                onSelect()
            }
        )
        AppDropdownMenuItem(
            text = { Text(stringResource(R.string.entry_lock_menu_lock_all)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            onClick = {
                onDismiss()
                onLockAll()
            }
        )
        AppDropdownMenuItem(
            text = { Text(stringResource(R.string.entry_lock_menu_unlock_all)) },
            leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) },
            onClick = {
                onDismiss()
                onUnlockAll()
            }
        )
        AppDropdownMenuItem(
            text = { Text(stringResource(R.string.entry_lock_menu_session_unlock)) },
            leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
            enabled = hasLockedEntries && !sessionUnlocked,
            onClick = {
                onDismiss()
                onSessionUnlock()
            }
        )
    }
}
