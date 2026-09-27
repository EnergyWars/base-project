package com.wafflehq.lib.uicore.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.theme.AppSpacing

object ErrorDetailDialogTestTags {
    const val DIALOG = "error_detail_dialog"
    const val MESSAGE = "error_detail_message"
    const val DETAIL = "error_detail_detail"
    const val DISMISS = "error_detail_dismiss"
}

object ErrorDetailDefaults {
    const val DETAIL_BOX_ALPHA = 0.4f
    val maxContentHeight = 300.dp
}

fun Throwable.detailText(message: String? = null): String = buildString {
    append(this@detailText.javaClass.simpleName)
    val ownMessage = this@detailText.message
    if (ownMessage != null && ownMessage != message) {
        append(": ")
        append(ownMessage)
    }
    this@detailText.cause?.let { c ->
        append("\nCause: ")
        append(c.javaClass.simpleName)
        c.message?.let { append(": ").append(it) }
    }
}

@Composable
fun ErrorDetailDialog(
    title: String,
    message: String,
    dismissText: String,
    onDismiss: () -> Unit,
    icon: ImageVector,
    detail: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    AppDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(ErrorDetailDialogTestTags.DIALOG),
        icon = { Icon(icon, contentDescription = null, tint = scheme.error) },
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = ErrorDetailDefaults.maxContentHeight)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag(ErrorDetailDialogTestTags.MESSAGE)
                )
                if (!detail.isNullOrEmpty()) {
                    Spacer(Modifier.height(AppSpacing.sm))
                    AppCard(
                        containerColor = scheme.errorContainer.copy(alpha = ErrorDetailDefaults.DETAIL_BOX_ALPHA),
                        contentColor = scheme.onErrorContainer,
                        modifier = Modifier.testTag(ErrorDetailDialogTestTags.DETAIL)
                    ) {
                        SelectionContainer {
                            Text(
                                text = detail,
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onErrorContainer,
                                modifier = Modifier.padding(AppSpacing.sm)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            AppButton(
                text = dismissText,
                role = AppButtonRole.Primary,
                variant = AppButtonVariant.Text,
                onClick = onDismiss,
                modifier = Modifier.testTag(ErrorDetailDialogTestTags.DISMISS)
            )
        }
    )
}
