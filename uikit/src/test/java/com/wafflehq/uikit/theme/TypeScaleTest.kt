package com.wafflehq.uikit.theme

import androidx.compose.ui.text.font.FontFamily
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeScaleTest {

    @Test
    fun `buildTypography maps every style with the given font family`() {
        val scale = AppTypeScale.Default
        val family = FontFamily.SansSerif
        val typography = buildTypography(scale, family)

        assertEquals(scale.displayLarge.fontSize, typography.displayLarge.fontSize)
        assertEquals(scale.displayLarge.lineHeight, typography.displayLarge.lineHeight)
        assertEquals(scale.displayLarge.fontWeight, typography.displayLarge.fontWeight)
        assertEquals(scale.displayLarge.letterSpacing, typography.displayLarge.letterSpacing)
        assertEquals(family, typography.displayLarge.fontFamily)

        assertEquals(scale.bodyMedium.fontSize, typography.bodyMedium.fontSize)
        assertEquals(family, typography.bodyMedium.fontFamily)

        assertEquals(scale.labelSmall.fontSize, typography.labelSmall.fontSize)
        assertEquals(scale.labelSmall.letterSpacing, typography.labelSmall.letterSpacing)

        assertEquals(scale.titleSmall.fontSize, typography.titleSmall.fontSize)
        assertEquals(scale.displayMedium.fontSize, typography.displayMedium.fontSize)
        assertEquals(scale.displaySmall.fontSize, typography.displaySmall.fontSize)
        assertEquals(scale.headlineLarge.fontSize, typography.headlineLarge.fontSize)
        assertEquals(scale.headlineMedium.fontSize, typography.headlineMedium.fontSize)
        assertEquals(scale.headlineSmall.fontSize, typography.headlineSmall.fontSize)
        assertEquals(scale.titleLarge.fontSize, typography.titleLarge.fontSize)
        assertEquals(scale.titleMedium.fontSize, typography.titleMedium.fontSize)
        assertEquals(scale.bodyLarge.fontSize, typography.bodyLarge.fontSize)
        assertEquals(scale.bodySmall.fontSize, typography.bodySmall.fontSize)
        assertEquals(scale.labelLarge.fontSize, typography.labelLarge.fontSize)
        assertEquals(scale.labelMedium.fontSize, typography.labelMedium.fontSize)
    }
}
