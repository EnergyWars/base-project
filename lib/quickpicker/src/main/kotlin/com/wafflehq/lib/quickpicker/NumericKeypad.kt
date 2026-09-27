package com.wafflehq.lib.quickpicker

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.button.AppIconButton
import com.wafflehq.lib.uicore.button.AppIconButtonVariant
import com.wafflehq.lib.uicore.theme.AppRadius

@Composable
internal fun NumericKeypad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    leadingKey: @Composable RowScope.() -> Unit = { Spacer(Modifier.weight(1f)) }
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                row.forEach { digit ->
                    AppButton(
                        text = digit.toString(),
                        role = AppButtonRole.Primary,
                        variant = AppButtonVariant.Tonal,
                        textStyle = MaterialTheme.typography.titleLarge,
                        onClick = { onDigit(digit) },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag(QuickPickerTestTags.digit(digit)),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            leadingKey()
            AppButton(
                text = "0",
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                textStyle = MaterialTheme.typography.titleLarge,
                onClick = { onDigit(0) },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag(QuickPickerTestTags.digit(0)),
            )
            AppIconButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = null,
                role = AppButtonRole.Primary,
                variant = AppIconButtonVariant.Tonal,
                shape = RoundedCornerShape(AppRadius.button),
                onClick = onBackspace,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag(QuickPickerTestTags.BACKSPACE),
            )
        }
    }
}
