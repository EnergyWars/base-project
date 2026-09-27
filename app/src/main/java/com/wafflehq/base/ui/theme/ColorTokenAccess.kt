package com.wafflehq.base.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.wafflehq.base.domain.colortheme.ColorTokenCatalog
import com.wafflehq.lib.settings.colors.ColorTokenId
import com.wafflehq.lib.settings.colors.ColorTokenResolver
import com.wafflehq.lib.settings.colors.LocalColorTokens
import com.wafflehq.lib.settings.colors.LocalIsDarkTheme

@Composable
fun colorToken(id: ColorTokenId): Color {
    LocalColorTokens.current[id]?.let { return it }
    val token = ColorTokenCatalog.registry.byId.getValue(id)
    return ColorTokenResolver.resolve(token, override = null, isDark = LocalIsDarkTheme.current)
}
