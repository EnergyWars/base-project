package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppRole
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme
import com.wafflehq.base.ui.theme.GeistMono

@Composable
fun Section05Roles() = Section(R.string.sc_s5_title, R.string.sc_s5_desc) {
    val rows = listOf(
        Triple(AppRole.Primary, R.string.sc_role_card_primary, R.string.sc_contrast_primary) to true,
        Triple(AppRole.Secondary, R.string.sc_role_card_secondary, R.string.sc_contrast_secondary) to true,
        Triple(AppRole.Tertiary, R.string.sc_role_card_tertiary, R.string.sc_contrast_tertiary) to true,
        Triple(AppRole.Success, R.string.sc_role_card_success, R.string.sc_contrast_success) to true,
        Triple(AppRole.Warning, R.string.sc_role_card_warning, R.string.sc_contrast_warning) to false,
        Triple(AppRole.Error, R.string.sc_role_card_error, R.string.sc_contrast_error) to true,
        Triple(AppRole.Neutral, R.string.sc_role_card_neutral, R.string.sc_contrast_neutral) to true,
    )
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        rows.forEachIndexed { index, (data, aaa) ->
            val (r, labelRes, contrastRes) = data
            RoleCard(r, labelRes, contrastRes, aaa, "5a.${index + 1}")
        }
    }
}

@Composable
private fun RoleCard(r: AppRole, labelRes: Int, contrastRes: Int, aaa: Boolean, code: String) {
    val rc = role(r)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(radiusM)
            .background(rc.accent)
            .inspectId(code)
            .padding(horizontal = AppSpacing.lg, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(stringResource(labelRes), style = MaterialTheme.typography.titleMedium, color = rc.onAccent)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text(stringResource(contrastRes), style = MaterialTheme.typography.labelSmall.copy(fontFamily = GeistMono), color = rc.onAccent.copy(alpha = 0.85f))
            Box(
                modifier = Modifier
                    .clip(radiusXs)
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    if (aaa) stringResource(R.string.sc_pass_aaa) else stringResource(R.string.sc_pass_aa),
                    style = mono.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }
    }
}
