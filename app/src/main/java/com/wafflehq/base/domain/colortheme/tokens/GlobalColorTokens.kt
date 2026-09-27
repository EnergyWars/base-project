package com.wafflehq.base.domain.colortheme.tokens

import androidx.compose.ui.graphics.Color
import com.wafflehq.base.R
import com.wafflehq.base.domain.colortheme.ColorTokenCategory
import com.wafflehq.lib.settings.colors.Amethyst10
import com.wafflehq.lib.settings.colors.Amethyst30
import com.wafflehq.lib.settings.colors.Amethyst40
import com.wafflehq.lib.settings.colors.Amethyst80
import com.wafflehq.lib.settings.colors.Amethyst90
import com.wafflehq.lib.settings.colors.Citrine10
import com.wafflehq.lib.settings.colors.Citrine30
import com.wafflehq.lib.settings.colors.Citrine40
import com.wafflehq.lib.settings.colors.Citrine80
import com.wafflehq.lib.settings.colors.Citrine90
import com.wafflehq.lib.settings.colors.ColorToken
import com.wafflehq.lib.settings.colors.ColorTokenId
import com.wafflehq.lib.settings.colors.DarkBackground
import com.wafflehq.lib.settings.colors.Emerald10
import com.wafflehq.lib.settings.colors.Emerald30
import com.wafflehq.lib.settings.colors.Emerald40
import com.wafflehq.lib.settings.colors.Emerald80
import com.wafflehq.lib.settings.colors.Emerald90
import com.wafflehq.lib.settings.colors.Garnet10
import com.wafflehq.lib.settings.colors.Garnet30
import com.wafflehq.lib.settings.colors.Garnet40
import com.wafflehq.lib.settings.colors.Garnet80
import com.wafflehq.lib.settings.colors.Garnet90
import com.wafflehq.lib.settings.colors.HairlineStrongDark
import com.wafflehq.lib.settings.colors.HairlineStrongLight
import com.wafflehq.lib.settings.colors.InkDark
import com.wafflehq.lib.settings.colors.InkLight
import com.wafflehq.lib.settings.colors.LightBackground
import com.wafflehq.lib.settings.colors.Sapphire10
import com.wafflehq.lib.settings.colors.Sapphire30
import com.wafflehq.lib.settings.colors.Sapphire40
import com.wafflehq.lib.settings.colors.Sapphire80
import com.wafflehq.lib.settings.colors.Sapphire90

object GlobalColorTokens {

    val primary = ColorTokenId("global.primary")
    val onPrimary = ColorTokenId("global.onPrimary")
    val primaryContainer = ColorTokenId("global.primaryContainer")
    val onPrimaryContainer = ColorTokenId("global.onPrimaryContainer")
    val secondary = ColorTokenId("global.secondary")
    val onSecondary = ColorTokenId("global.onSecondary")
    val secondaryContainer = ColorTokenId("global.secondaryContainer")
    val onSecondaryContainer = ColorTokenId("global.onSecondaryContainer")
    val tertiary = ColorTokenId("global.tertiary")
    val onTertiary = ColorTokenId("global.onTertiary")
    val tertiaryContainer = ColorTokenId("global.tertiaryContainer")
    val onTertiaryContainer = ColorTokenId("global.onTertiaryContainer")
    val error = ColorTokenId("global.error")
    val onError = ColorTokenId("global.onError")
    val errorContainer = ColorTokenId("global.errorContainer")
    val onErrorContainer = ColorTokenId("global.onErrorContainer")
    val background = ColorTokenId("global.background")
    val onBackground = ColorTokenId("global.onBackground")
    val outline = ColorTokenId("global.outline")
    val warning = ColorTokenId("global.warning")
    val onWarning = ColorTokenId("global.onWarning")
    val warningContainer = ColorTokenId("global.warningContainer")
    val onWarningContainer = ColorTokenId("global.onWarningContainer")

    private fun token(id: ColorTokenId, labelRes: Int, descriptionRes: Int, light: Color, dark: Color) =
        ColorToken(id, ColorTokenCategory.GLOBAL.name, labelRes, descriptionRes, defaultLight = light, defaultDark = dark)

    val tokens: List<ColorToken> = listOf(
        token(primary, R.string.color_token_global_primary, R.string.color_token_global_primary_desc, Sapphire40, Sapphire80),
        token(onPrimary, R.string.color_token_global_on_primary, R.string.color_token_global_on_primary_desc, Color.White, Sapphire10),
        token(primaryContainer, R.string.color_token_global_primary_container, R.string.color_token_global_primary_container_desc, Sapphire90, Sapphire30),
        token(onPrimaryContainer, R.string.color_token_global_on_primary_container, R.string.color_token_global_on_primary_container_desc, Sapphire10, Sapphire90),
        token(secondary, R.string.color_token_global_secondary, R.string.color_token_global_secondary_desc, Emerald40, Emerald80),
        token(onSecondary, R.string.color_token_global_on_secondary, R.string.color_token_global_on_secondary_desc, Color.White, Emerald10),
        token(secondaryContainer, R.string.color_token_global_secondary_container, R.string.color_token_global_secondary_container_desc, Emerald90, Emerald30),
        token(onSecondaryContainer, R.string.color_token_global_on_secondary_container, R.string.color_token_global_on_secondary_container_desc, Emerald10, Emerald90),
        token(tertiary, R.string.color_token_global_tertiary, R.string.color_token_global_tertiary_desc, Amethyst40, Amethyst80),
        token(onTertiary, R.string.color_token_global_on_tertiary, R.string.color_token_global_on_tertiary_desc, Color.White, Amethyst10),
        token(tertiaryContainer, R.string.color_token_global_tertiary_container, R.string.color_token_global_tertiary_container_desc, Amethyst90, Amethyst30),
        token(onTertiaryContainer, R.string.color_token_global_on_tertiary_container, R.string.color_token_global_on_tertiary_container_desc, Amethyst10, Amethyst90),
        token(error, R.string.color_token_global_error, R.string.color_token_global_error_desc, Garnet40, Garnet80),
        token(onError, R.string.color_token_global_on_error, R.string.color_token_global_on_error_desc, Color.White, Garnet10),
        token(errorContainer, R.string.color_token_global_error_container, R.string.color_token_global_error_container_desc, Garnet90, Garnet30),
        token(onErrorContainer, R.string.color_token_global_on_error_container, R.string.color_token_global_on_error_container_desc, Garnet10, Garnet90),
        token(background, R.string.color_token_global_background, R.string.color_token_global_background_desc, LightBackground, DarkBackground),
        token(onBackground, R.string.color_token_global_on_background, R.string.color_token_global_on_background_desc, InkLight, InkDark),
        token(outline, R.string.color_token_global_outline, R.string.color_token_global_outline_desc, HairlineStrongLight, HairlineStrongDark),
        token(warning, R.string.color_token_global_warning, R.string.color_token_global_warning_desc, Citrine40, Citrine80),
        token(onWarning, R.string.color_token_global_on_warning, R.string.color_token_global_on_warning_desc, Color.White, Citrine10),
        token(warningContainer, R.string.color_token_global_warning_container, R.string.color_token_global_warning_container_desc, Citrine90, Citrine30),
        token(onWarningContainer, R.string.color_token_global_on_warning_container, R.string.color_token_global_on_warning_container_desc, Citrine10, Citrine90)
    )
}
