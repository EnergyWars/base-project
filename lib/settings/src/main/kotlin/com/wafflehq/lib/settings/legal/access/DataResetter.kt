package com.wafflehq.lib.settings.legal.access

fun interface DataResetter {
    suspend fun deleteAllData()
}
