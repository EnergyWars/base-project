package com.wafflehq.base.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import com.wafflehq.lib.uicore.theme.AppRadius

val BaseAppShapes = Shapes(
    extraSmall = RoundedCornerShape(AppRadius.textField),
    small = RoundedCornerShape(AppRadius.button),
    medium = RoundedCornerShape(AppRadius.textField),
    large = RoundedCornerShape(AppRadius.card),
    extraLarge = RoundedCornerShape(AppRadius.dialog)
)
