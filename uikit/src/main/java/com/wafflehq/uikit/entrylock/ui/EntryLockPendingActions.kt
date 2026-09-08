package com.wafflehq.uikit.entrylock.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.wafflehq.uikit.entrylock.AuthResult

class EntryLockPendingActions<T> internal constructor(
    private val idOf: (T) -> Long,
    private val isRevealed: (T) -> Boolean,
    private val onOpenViewer: (T) -> Unit,
    private val onEditEntry: (T) -> Unit,
    private val launchAuth: (String) -> Unit,
    private val authTitle: String,
    private val setPendingView: (T?) -> Unit,
    private val setPendingEdit: (T?) -> Unit,
    private val setPendingTemporaryUnlockId: (Long?) -> Unit,
    private val setPendingUnlockAll: (Boolean) -> Unit,
    private val setPendingSessionUnlock: (Boolean) -> Unit,
    private val setPendingBulkUnlock: (Boolean) -> Unit,
) {
    fun requestView(entry: T) {
        if (!isRevealed(entry)) {
            setPendingView(entry)
            launchAuth(authTitle)
        } else {
            onOpenViewer(entry)
        }
    }

    fun requestEdit(entry: T) {
        if (!isRevealed(entry)) {
            setPendingEdit(entry)
            launchAuth(authTitle)
        } else {
            onEditEntry(entry)
        }
    }

    fun requestTemporaryUnlock(entry: T) {
        setPendingTemporaryUnlockId(idOf(entry))
        launchAuth(authTitle)
    }

    fun requestUnlockAll() {
        setPendingUnlockAll(true)
        launchAuth(authTitle)
    }

    fun requestSessionUnlock() {
        setPendingSessionUnlock(true)
        launchAuth(authTitle)
    }

    fun requestBulkUnlock() {
        setPendingBulkUnlock(true)
        launchAuth(authTitle)
    }
}

@Composable
fun <T> rememberEntryLockPendingActions(
    authenticate: suspend (FragmentActivity, String) -> AuthResult,
    authTitle: String,
    unavailableMessage: String,
    isRevealed: (T) -> Boolean,
    idOf: (T) -> Long,
    onOpenViewer: (T) -> Unit,
    onEditEntry: (T) -> Unit,
    onTemporaryUnlockConfirmed: (Long) -> Unit,
    onSessionUnlockConfirmed: () -> Unit,
    onUnlockAllEntriesConfirmed: () -> Unit,
    onBulkUnlockSelectedConfirmed: () -> Unit,
): EntryLockPendingActions<T> {
    val context = LocalContext.current
    var pendingViewEntry by remember { mutableStateOf<T?>(null) }
    var pendingEditEntry by remember { mutableStateOf<T?>(null) }
    var pendingUnlockAll by remember { mutableStateOf(false) }
    var pendingSessionUnlock by remember { mutableStateOf(false) }
    var pendingBulkUnlock by remember { mutableStateOf(false) }
    var pendingTemporaryUnlockId by remember { mutableStateOf<Long?>(null) }

    val launchAuth = entryAuthLauncher(authenticate = authenticate) { result ->
        when (result) {
            AuthResult.SUCCESS -> {
                pendingViewEntry?.let(onOpenViewer)
                pendingEditEntry?.let(onEditEntry)
                pendingTemporaryUnlockId?.let(onTemporaryUnlockConfirmed)
                if (pendingUnlockAll) onUnlockAllEntriesConfirmed()
                if (pendingSessionUnlock) onSessionUnlockConfirmed()
                if (pendingBulkUnlock) onBulkUnlockSelectedConfirmed()
            }
            AuthResult.NOT_AVAILABLE -> Toast.makeText(context, unavailableMessage, Toast.LENGTH_LONG).show()
            AuthResult.FAILED, AuthResult.CANCELLED -> Unit
        }
        pendingViewEntry = null
        pendingEditEntry = null
        pendingUnlockAll = false
        pendingSessionUnlock = false
        pendingBulkUnlock = false
        pendingTemporaryUnlockId = null
    }

    return EntryLockPendingActions(
        idOf = idOf,
        isRevealed = isRevealed,
        onOpenViewer = onOpenViewer,
        onEditEntry = onEditEntry,
        launchAuth = launchAuth,
        authTitle = authTitle,
        setPendingView = { pendingViewEntry = it },
        setPendingEdit = { pendingEditEntry = it },
        setPendingTemporaryUnlockId = { pendingTemporaryUnlockId = it },
        setPendingUnlockAll = { pendingUnlockAll = it },
        setPendingSessionUnlock = { pendingSessionUnlock = it },
        setPendingBulkUnlock = { pendingBulkUnlock = it },
    )
}
