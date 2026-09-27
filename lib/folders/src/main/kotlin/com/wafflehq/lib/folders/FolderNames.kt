package com.wafflehq.lib.folders

object FolderNames {
    fun normalize(raw: String): String? = raw.trim().takeIf { it.isNotEmpty() }
}
