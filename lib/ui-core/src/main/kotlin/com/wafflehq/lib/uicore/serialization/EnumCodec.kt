package com.wafflehq.lib.uicore.serialization

inline fun <reified T : Enum<T>> enumFromNameOrDefault(name: String?, default: T): T =
    enumFromNameOrNull<T>(name) ?: default

inline fun <reified T : Enum<T>> enumFromNameOrNull(name: String?): T? =
    name?.let { candidate -> enumValues<T>().firstOrNull { it.name == candidate } }
