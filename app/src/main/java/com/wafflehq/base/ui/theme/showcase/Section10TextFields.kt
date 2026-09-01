package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

@Composable
fun Section10TextFields() = Section(R.string.sc_s10_title, R.string.sc_s10_desc) {
    Panel {
        FieldOutlined(R.string.sc_tf_name_lbl, value = "", placeholder = stringResource(R.string.sc_tf_name_ph), inspectCode = "10a.1")
        FieldOutlined(R.string.sc_tf_search_lbl, value = "", placeholder = stringResource(R.string.sc_tf_search_ph), leading = Icons.Outlined.Search, inspectCode = "10a.2")
        FieldOutlined(R.string.sc_tf_email_lbl, value = stringResource(R.string.sc_tf_email_val), placeholder = "", error = true, inspectCode = "10a.3")
        FieldFilled(R.string.sc_tf_note_lbl, value = stringResource(R.string.sc_tf_note_val), inspectCode = "10a.4")
        FieldOutlined(R.string.sc_tf_phone_lbl, value = stringResource(R.string.sc_tf_phone_val), placeholder = "", disabled = true, inspectCode = "10a.5")
    }
}

@Composable
private fun FieldOutlined(
    labelRes: Int,
    value: String,
    placeholder: String,
    leading: ImageVector? = null,
    error: Boolean = false,
    disabled: Boolean = false,
    inspectCode: String,
) {
    val borderColor = when {
        error -> AppTheme.colors.error.accent
        else -> AppTheme.colors.outline
    }
    val labelColor = if (error) AppTheme.colors.error.accent else AppTheme.colors.onSurfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.inspectId(inspectCode).then(if (disabled) Modifier.background(Color.Transparent) else Modifier)) {
        Text(stringResource(labelRes), style = MaterialTheme.typography.bodySmall, color = labelColor, modifier = Modifier.padding(start = 4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(radiusXs)
                .border(1.5.dp, borderColor.copy(alpha = if (disabled) 0.5f else 1f), radiusXs)
                .padding(horizontal = AppSpacing.lg, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            if (leading != null) Icon(leading, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
            val text = value.ifEmpty { placeholder }
            val color = if (value.isEmpty()) AppTheme.colors.onSurfaceVariant else AppTheme.colors.onSurface
            Text(text, style = MaterialTheme.typography.bodyLarge, color = color.copy(alpha = if (disabled) 0.5f else 1f))
        }
    }
}

@Composable
private fun FieldFilled(labelRes: Int, value: String, inspectCode: String) {
    Column(modifier = Modifier.inspectId(inspectCode), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(stringResource(labelRes), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(AppTheme.colors.surfaceVariant)
                .padding(AppSpacing.lg),
        ) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.onSurface)
        }
    }
}
