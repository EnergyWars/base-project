package com.wafflehq.lib.entrylock

import androidx.fragment.app.FragmentActivity

class GuardedEntryAuthenticator(
    private val delegate: EntryAuthenticator,
    private val session: EntryLockSession
) : EntryAuthenticator {

    override fun isAvailable(): Boolean = delegate.isAvailable()

    override suspend fun authenticate(activity: FragmentActivity, title: String): AuthResult =
        session.guardAuthPrompt { delegate.authenticate(activity, title) }
}
