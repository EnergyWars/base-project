package com.wafflehq.uikit.entrylock.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wafflehq.uikit.R

@Composable
fun EntryLockOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    hasLockedEntries: Boolean,
    sessionUnlocked: Boolean,
    onSelect: () -> Unit,
    onLockAll: () -> Unit,
    onUnlockAll: () -> Unit,
    onSessionUnlock: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.entry_lock_menu_select)) },
            leadingIcon = { Icon(Icons.Default.Checklist, contentDescription = null) },
            onClick = {
                onDismiss()
                onSelect()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.entry_lock_menu_lock_all)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            onClick = {
                onDismiss()
                onLockAll()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.entry_lock_menu_unlock_all)) },
            leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) },
            onClick = {
                onDismiss()
                onUnlockAll()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.entry_lock_menu_session_unlock)) },
            leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
            enabled = hasLockedEntries && !sessionUnlocked,
            onClick = {
                onDismiss()
                onSessionUnlock()
            },
        )
    }
}
