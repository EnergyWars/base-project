package com.wafflehq.base.domain.colortheme

import com.wafflehq.base.domain.colortheme.tokens.GlobalColorTokens
import com.wafflehq.base.domain.colortheme.tokens.SuccessColorTokens
import com.wafflehq.lib.settings.colors.ColorCategoryDescriptor
import com.wafflehq.lib.settings.colors.ColorToken
import com.wafflehq.lib.settings.colors.ColorTokenRegistry

object ColorTokenCatalog {

    val all: List<ColorToken> = GlobalColorTokens.tokens + SuccessColorTokens.tokens

    private val AlwaysVisibleCategories = ColorTokenCategory.entries.toSet()

    val registry: ColorTokenRegistry = ColorTokenRegistry(
        categories = ColorTokenCategory.entries.map { category ->
            ColorCategoryDescriptor(
                key = category.name,
                labelRes = category.labelRes,
                descriptionRes = category.descriptionRes,
                alwaysVisible = category in AlwaysVisibleCategories
            )
        },
        all = all
    )
}
