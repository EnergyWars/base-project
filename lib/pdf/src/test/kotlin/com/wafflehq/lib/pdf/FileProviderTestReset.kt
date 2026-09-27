package com.wafflehq.lib.pdf

import androidx.core.content.FileProvider

object FileProviderTestReset {

    private val cacheField = FileProvider::class.java.getDeclaredField("sCache").apply { isAccessible = true }

    fun clear() {
        (cacheField.get(null) as? MutableMap<*, *>)?.clear()
    }
}
