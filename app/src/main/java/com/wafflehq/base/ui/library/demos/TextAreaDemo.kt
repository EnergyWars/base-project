package com.wafflehq.base.ui.library.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.lib.textarea.KeyboardAwareTextArea
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.theme.AppSpacing

internal object TextAreaDemoLogic {

    const val MAX_LENGTH = 280
    const val MIN_LINES = 4
    const val MAX_LINES = 12

    fun isOverLimit(text: String): Boolean = text.length > MAX_LENGTH

    fun lineCount(text: String): Int = if (text.isEmpty()) 0 else text.lines().size
}

internal object TextAreaTags {
    const val INPUT = "libex_textarea_input"
    const val COUNT = "libex_textarea_count"
    const val FILL = "libex_textarea_fill"
    const val CLEAR = "libex_textarea_clear"
}

@Composable
internal fun TextAreaDemo() {
    var text by remember { mutableStateOf("") }
    val sample = stringResource(R.string.libex_textarea_sample)

    DemoSection(
        id = "textarea",
        titleRes = R.string.libex_textarea_title,
        descriptionRes = R.string.libex_textarea_desc,
        moduleRes = R.string.libex_module_textarea,
    ) {
        KeyboardAwareTextArea(
            value = text,
            onValueChange = { text = it },
            minLines = TextAreaDemoLogic.MIN_LINES,
            maxLines = TextAreaDemoLogic.MAX_LINES,
            isError = TextAreaDemoLogic.isOverLimit(text),
            label = { Text(stringResource(R.string.libex_textarea_label)) },
            placeholder = { Text(stringResource(R.string.libex_textarea_placeholder)) },
            supportingText = {
                Text(
                    text = stringResource(
                        R.string.libex_textarea_count,
                        text.length,
                        TextAreaDemoLogic.MAX_LENGTH,
                        TextAreaDemoLogic.lineCount(text),
                    ),
                    modifier = Modifier.testTag(TextAreaTags.COUNT),
                )
            },
            modifier = Modifier.fillMaxWidth().testTag(TextAreaTags.INPUT),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppButton(
                text = stringResource(R.string.libex_textarea_fill),
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Tonal,
                onClick = { text = sample },
                modifier = Modifier.testTag(TextAreaTags.FILL),
            )
            AppButton(
                text = stringResource(R.string.libex_action_clear),
                role = AppButtonRole.Neutral,
                variant = AppButtonVariant.Text,
                enabled = text.isNotEmpty(),
                onClick = { text = "" },
                modifier = Modifier.testTag(TextAreaTags.CLEAR),
            )
        }
    }
}
