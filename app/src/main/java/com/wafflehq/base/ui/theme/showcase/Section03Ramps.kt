package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme
import com.wafflehq.base.ui.theme.GeistMono

@Composable
fun Section03Ramps() = Section(R.string.sc_s3_title, R.string.sc_s3_desc) {
    val meta = mapOf(
        "Sapphire" to Triple(R.string.sc_role_primary, R.string.sc_ramp_sapphire, R.string.sc_hue_sapphire),
        "Aquamarine" to Triple(R.string.sc_role_secondary, R.string.sc_ramp_aquamarine, R.string.sc_hue_aquamarine),
        "Amethyst" to Triple(R.string.sc_role_tertiary, R.string.sc_ramp_amethyst, R.string.sc_hue_amethyst),
        "Emerald" to Triple(R.string.sc_role_success, R.string.sc_ramp_emerald, R.string.sc_hue_emerald),
        "Citrine" to Triple(R.string.sc_role_warning, R.string.sc_ramp_citrine, R.string.sc_hue_citrine),
        "Garnet" to Triple(R.string.sc_role_error, R.string.sc_ramp_garnet, R.string.sc_hue_garnet),
        "Graphite" to Triple(R.string.sc_role_neutral, R.string.sc_ramp_graphite, R.string.sc_hue_graphite),
    )
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        AppTheme.colorRamps.forEachIndexed { index, ramp ->
            val m = meta[ramp.name] ?: return@forEachIndexed
            RampCard(ramp.tones, m.first, m.second, m.third, "3" + ('a' + index))
        }
    }
}

@Composable
private fun RampCard(tones: List<Pair<String, Color>>, roleRes: Int, nameRes: Int, hueRes: Int, groupCode: String) {
    val tone90 = tones[8].second
    val tone10 = tones[0].second
    Panel(modifier = Modifier.inspectTap(groupCode)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text(stringResource(roleRes).uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontFamily = GeistMono), color = AppTheme.colors.onSurfaceVariant)
            Text(stringResource(nameRes), style = MaterialTheme.typography.titleMedium, color = AppTheme.colors.onSurface)
            Text(stringResource(hueRes), style = MaterialTheme.typography.labelSmall.copy(fontFamily = GeistMono), color = AppTheme.colors.onSurfaceVariant)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            tones.forEachIndexed { index, (tone, color) ->
                val fg = when {
                    index < 3 -> tone90
                    index < 5 -> Color.White
                    else -> tone10
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(66.dp)
                        .clip(radiusXs)
                        .background(color)
                        .inspectId("$groupCode.${index + 1}")
                        .padding(horizontal = 3.dp, vertical = 4.dp),
                ) {
                    Text(tone, style = mono.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold), color = fg, modifier = Modifier.align(Alignment.TopStart))
                    Text(color.hex(), style = mono.copy(fontSize = 7.sp), color = fg, modifier = Modifier.align(Alignment.BottomStart))
                }
            }
        }
    }
}
