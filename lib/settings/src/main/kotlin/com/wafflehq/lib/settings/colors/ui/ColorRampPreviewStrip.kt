package com.wafflehq.lib.settings.colors.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import com.wafflehq.lib.uicore.theme.AppRadius
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.settings.colors.ColorRampSteps
import com.wafflehq.lib.settings.colors.ColorRampTable

@Composable
fun ColorRampPreviewStrip(
    seed: Color,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val ramp = ColorRampTable.generatedRamp(seed)
    Row(
        modifier = (if (testTag != null) modifier.testTag(testTag) else modifier).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        ColorRampSteps.forEach { step ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(AppRadius.small))
                    .background(ramp.getValue(step))
                    .border(1.dp, outlineColor.copy(alpha = 0.4f), RoundedCornerShape(AppRadius.small))
            )
        }
    }
}
