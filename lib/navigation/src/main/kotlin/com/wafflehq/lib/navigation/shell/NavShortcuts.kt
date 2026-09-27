package com.wafflehq.lib.navigation.shell

import java.text.Collator
import java.util.Locale

const val MAX_NAV_SHORTCUTS = 4
const val MAX_RECENT_NAV_VISITS = 20

enum class NavShortcutMode(val sortsAlphabetically: Boolean) {
    RECENT(sortsAlphabetically = true),
    CUSTOM(sortsAlphabetically = false);

    companion object {
        fun fromName(name: String?): NavShortcutMode =
            entries.firstOrNull { it.name == name } ?: RECENT
    }
}

object NavShortcuts {

    private const val SEPARATOR = '\n'

    fun recordVisit(recent: List<String>, id: String, capacity: Int = MAX_RECENT_NAV_VISITS): List<String> {
        if (id.isBlank()) return recent
        return (listOf(id) + recent.filter { it != id }).take(capacity)
    }

    fun resolve(
        mode: NavShortcutMode,
        recent: List<String>,
        custom: List<String>,
        available: Set<String>,
        limit: Int = MAX_NAV_SHORTCUTS
    ): List<String> {
        val source = when (mode) {
            NavShortcutMode.RECENT -> recent
            NavShortcutMode.CUSTOM -> custom
        }
        return source.asSequence()
            .filter { it in available }
            .distinct()
            .take(limit.coerceAtLeast(0))
            .toList()
    }

    fun <T> sortAlphabetically(items: List<T>, locale: Locale, label: (T) -> String): List<T> {
        val collator = Collator.getInstance(locale).apply { strength = Collator.SECONDARY }
        return items.sortedWith { a, b -> collator.compare(label(a), label(b)) }
    }

    fun toggleCustom(custom: List<String>, id: String, limit: Int = MAX_NAV_SHORTCUTS): List<String> = when {
        id in custom -> custom - id
        custom.size >= limit -> custom
        else -> custom + id
    }

    fun move(custom: List<String>, id: String, offset: Int): List<String> {
        val from = custom.indexOf(id)
        if (from < 0) return custom
        val to = (from + offset).coerceIn(0, custom.lastIndex)
        if (to == from) return custom
        val result = custom.toMutableList()
        result.removeAt(from)
        result.add(to, id)
        return result
    }

    fun encode(ids: List<String>): String = ids.filter { it.isNotBlank() && SEPARATOR !in it }.joinToString(SEPARATOR.toString())

    fun decode(raw: String?): List<String> =
        raw.orEmpty().split(SEPARATOR).filter { it.isNotBlank() }.distinct()
}
