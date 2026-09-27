package com.wafflehq.base.domain.colortheme.tokens

import androidx.compose.ui.graphics.Color
import com.wafflehq.base.R
import com.wafflehq.base.domain.colortheme.ColorTokenCategory
import com.wafflehq.lib.settings.colors.ColorToken
import com.wafflehq.lib.settings.colors.ColorTokenId
import com.wafflehq.lib.settings.colors.Emerald10
import com.wafflehq.lib.settings.colors.Emerald30
import com.wafflehq.lib.settings.colors.Emerald40
import com.wafflehq.lib.settings.colors.Emerald80
import com.wafflehq.lib.settings.colors.Emerald90

object SuccessColorTokens {

    val success = ColorTokenId("success.success")
    val onSuccess = ColorTokenId("success.onSuccess")
    val successContainer = ColorTokenId("success.successContainer")
    val onSuccessContainer = ColorTokenId("success.onSuccessContainer")

    private fun token(id: ColorTokenId, labelRes: Int, descriptionRes: Int, light: Color, dark: Color) = ColorToken(
        id = id,
        categoryKey = ColorTokenCategory.SUCCESS.name,
        labelRes = labelRes,
        descriptionRes = descriptionRes,
        defaultLight = light,
        defaultDark = dark
    )

    val tokens: List<ColorToken> = listOf(
        token(success, R.string.color_token_success_success, R.string.color_token_success_success_desc, Emerald40, Emerald80),
        token(onSuccess, R.string.color_token_success_on_success, R.string.color_token_success_on_success_desc, Color.White, Emerald10),
        token(successContainer, R.string.color_token_success_container, R.string.color_token_success_container_desc, Emerald90, Emerald30),
        token(onSuccessContainer, R.string.color_token_success_on_container, R.string.color_token_success_on_container_desc, Emerald10, Emerald90)
    )
}
