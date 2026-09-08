package com.wafflehq.uikit.folders

object FolderNames {
    fun normalize(raw: String): String? = raw.trim().takeIf { it.isNotEmpty() }
}
