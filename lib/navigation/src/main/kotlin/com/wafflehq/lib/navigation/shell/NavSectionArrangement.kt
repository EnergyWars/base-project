package com.wafflehq.lib.navigation.shell

import java.text.Collator
import java.util.Locale

fun arrangeNavSections(
    sections: List<AppNavSection>,
    pinnedLabel: String,
    locale: Locale = Locale.getDefault()
): List<AppNavSection> {
    val collator = Collator.getInstance(locale)
    val alphabetical = Comparator<AppNavItem> { first, second -> collator.compare(first.label, second.label) }
    val sorted = sections
        .map { section -> section.copy(items = section.items.sortedWith(alphabetical)) }
        .filter { it.items.isNotEmpty() }
    val pinned = sorted.flatMap { it.items }.filter { it.pinned }.sortedWith(alphabetical)
    return if (pinned.isEmpty()) sorted else listOf(AppNavSection(label = pinnedLabel, items = pinned, pinned = true)) + sorted
}

fun filterNavSections(sections: List<AppNavSection>, query: String): List<AppNavSection> {
    val needle = query.trim()
    if (needle.isEmpty()) return sections
    return sections.filterNot { it.pinned }.mapNotNull { section ->
        val matches = section.items.filter { it.label.contains(needle, ignoreCase = true) }
        if (matches.isEmpty()) null else section.copy(items = matches)
    }
}

fun withoutHiddenNavItems(sections: List<AppNavSection>, showHidden: Boolean): List<AppNavSection> {
    if (showHidden) return sections
    return sections.mapNotNull { section ->
        val visible = section.items.filterNot { it.hidden }
        if (visible.isEmpty()) null else section.copy(items = visible)
    }
}

fun hasHiddenNavItems(sections: List<AppNavSection>): Boolean =
    sections.any { section -> section.items.any { it.hidden } }
