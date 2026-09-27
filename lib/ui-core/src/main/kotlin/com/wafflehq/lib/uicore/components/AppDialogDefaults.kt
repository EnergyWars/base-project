package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.wafflehq.lib.uicore.theme.AppRadius

object AppDialogDefaults {

    val tonalElevation: Dp = 1.dp

    val shape: Shape = RoundedCornerShape(AppRadius.dialog)

    val containerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surface

    val properties: DialogProperties = DialogProperties(dismissOnClickOutside = false)

    val fullWidthProperties: DialogProperties = DialogProperties(
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false
    )
}
