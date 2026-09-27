package com.wafflehq.lib.settings.colors

data class ColorCategoryDescriptor(
    val key: String,
    val labelRes: Int,
    val descriptionRes: Int,
    val alwaysVisible: Boolean = false
)
