package com.wafflehq.base.domain.colortheme

import com.wafflehq.base.R

val ColorTokenCategory.labelRes: Int
    get() = when (this) {
        ColorTokenCategory.GLOBAL -> R.string.color_category_global
        ColorTokenCategory.SUCCESS -> R.string.color_category_success
    }

val ColorTokenCategory.descriptionRes: Int
    get() = when (this) {
        ColorTokenCategory.GLOBAL -> R.string.color_category_global_desc
        ColorTokenCategory.SUCCESS -> R.string.color_category_success_desc
    }
