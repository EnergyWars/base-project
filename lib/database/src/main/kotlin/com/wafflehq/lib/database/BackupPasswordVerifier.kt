package com.wafflehq.lib.database

import com.wafflehq.lib.database.crypto.PasswordKeyWrapper
import com.wafflehq.lib.database.state.EncryptionStateStore

class BackupPasswordVerifier(
    private val stateStore: EncryptionStateStore,
    private val passwordWrapper: PasswordKeyWrapper
) {
    fun verify(password: CharArray): Boolean {
        val wrapped = stateStore.readPasswordWrappedDek() ?: return false
        return runCatching { passwordWrapper.unwrap(wrapped, password) }.isSuccess
    }
}
