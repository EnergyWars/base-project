package com.wafflehq.lib.settings.encryption

import com.wafflehq.lib.database.crypto.KeystoreKeyWrapper
import com.wafflehq.lib.database.crypto.WrappedDek

class FakeKeystoreKeyWrapper(var failing: Boolean = false) : KeystoreKeyWrapper {

    override fun wrap(dek: ByteArray): WrappedDek {
        if (failing) throw IllegalStateException("keystore unavailable")
        return WrappedDek(iv = IV, ciphertext = dek.copyOf())
    }

    override fun unwrap(wrapped: WrappedDek): ByteArray {
        if (failing) throw IllegalStateException("keystore unavailable")
        return wrapped.ciphertext.copyOf()
    }

    override fun deleteKey() = Unit

    companion object {
        val IV = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    }
}
