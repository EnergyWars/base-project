package com.wafflehq.lib.settings.colors

class ColorTokenRegistry(
    val categories: List<ColorCategoryDescriptor>,
    val all: List<ColorToken>
) {
    val byId: Map<ColorTokenId, ColorToken> = all.associateBy { it.id }

    val byCategory: Map<String, List<ColorToken>> = all.groupBy { it.categoryKey }

    val categoryByKey: Map<String, ColorCategoryDescriptor> = categories.associateBy { it.key }

    val alwaysVisibleCategoryKeys: Set<String> =
        categories.filter { it.alwaysVisible }.map { it.key }.toSet()

    val shortIds: Map<ColorTokenId, String> = categories
        .flatMapIndexed { index, category ->
            val letters = categoryLetters(index)
            byCategory[category.key].orEmpty()
                .mapIndexed { position, token -> token.id to "$letters.${position + 1}" }
        }
        .toMap()

    fun shortId(id: ColorTokenId): String = shortIds.getValue(id)

    fun tokensOf(categoryKey: String): List<ColorToken> = byCategory[categoryKey].orEmpty()

    fun contrastCounterpartOrNull(id: ColorTokenId): ColorTokenId? {
        val dot = id.value.indexOf('.')
        if (dot < 0) return null
        val namespace = id.value.substring(0, dot)
        val suffix = id.value.substring(dot + 1)
        if (suffix.isEmpty()) return null

        val counterpartSuffix = if (suffix.startsWith("on") && suffix.length > 2 && suffix[2].isUpperCase()) {
            suffix.removePrefix("on").replaceFirstChar { it.lowercaseChar() }
        } else {
            "on" + suffix.replaceFirstChar { it.uppercaseChar() }
        }

        val counterpartId = ColorTokenId("$namespace.$counterpartSuffix")
        return counterpartId.takeIf { it != id && byId.containsKey(it) }
    }

    private fun categoryLetters(index: Int): String {
        var n = index
        val letters = StringBuilder()
        do {
            letters.insert(0, 'a' + n % 26)
            n = n / 26 - 1
        } while (n >= 0)
        return letters.toString()
    }
}
