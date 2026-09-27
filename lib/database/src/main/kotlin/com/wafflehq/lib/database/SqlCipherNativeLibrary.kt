package com.wafflehq.lib.database

object SqlCipherNativeLibrary {
    fun load() {
        System.loadLibrary("sqlcipher")
    }
}
