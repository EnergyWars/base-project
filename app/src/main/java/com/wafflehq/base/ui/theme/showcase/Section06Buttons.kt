package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppRole
import com.wafflehq.base.ui.theme.AppSpacing

@Composable
fun Section06Buttons() = Section(R.string.sc_s6_title, R.string.sc_s6_desc) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        ButtonPanel("6a", AppRole.Primary, R.string.sc_btn_sub_primary, listOf(R.string.sc_b_save, R.string.sc_b_edit, R.string.sc_b_share, R.string.sc_b_filter, R.string.sc_b_more), withTwoDisabled = true)
        ButtonPanel("6b", AppRole.Secondary, R.string.sc_btn_sub_secondary, listOf(R.string.sc_b_reminder, R.string.sc_b_mark, R.string.sc_b_note, R.string.sc_b_filter, R.string.sc_b_more), withOneDisabled = true)
        ButtonPanel("6c", AppRole.Tertiary, R.string.sc_btn_sub_tertiary, listOf(R.string.sc_b_predict, R.string.sc_b_cycle, R.string.sc_b_stats, R.string.sc_b_options, R.string.sc_b_more))
        ButtonPanel("6d", AppRole.Success, R.string.sc_btn_sub_success, listOf(R.string.sc_b_confirm, R.string.sc_b_successful, R.string.sc_b_callable, R.string.sc_b_active, R.string.sc_b_details))
        ButtonPanel("6e", AppRole.Warning, R.string.sc_btn_sub_warning, listOf(R.string.sc_b_battery, R.string.sc_b_holiday, R.string.sc_b_check, R.string.sc_b_verify, R.string.sc_b_more))
        ButtonPanel("6f", AppRole.Error, R.string.sc_btn_sub_error, listOf(R.string.sc_b_delete, R.string.sc_b_error, R.string.sc_b_discard, R.string.sc_b_stop, R.string.sc_b_more))
        ButtonPanel("6g", AppRole.Neutral, R.string.sc_btn_sub_neutral, listOf(R.string.sc_b_standard, R.string.sc_b_metadata, R.string.sc_b_archive, R.string.sc_b_secondary, R.string.sc_b_more))
    }
}

@Composable
private fun ButtonPanel(
    groupCode: String,
    r: AppRole,
    subRes: Int,
    labels: List<Int>,
    withOneDisabled: Boolean = false,
    withTwoDisabled: Boolean = false,
) {
    val variants = listOf(BtnVariant.Filled, BtnVariant.Tonal, BtnVariant.Elevated, BtnVariant.Outlined, BtnVariant.Text)
    Panel(modifier = Modifier.inspectTap(groupCode)) {
        Subhead(stringResource(subRes))
        WrapRow {
            labels.forEachIndexed { i, res -> ShowcaseButton(stringResource(res), r, variants[i], inspectCode = "$groupCode.${i + 1}") }
            if (withTwoDisabled) {
                ShowcaseButton(stringResource(R.string.sc_b_disabled), r, BtnVariant.Filled, enabled = false, inspectCode = "$groupCode.${labels.size + 1}")
                ShowcaseButton(stringResource(R.string.sc_b_disabled), r, BtnVariant.Outlined, enabled = false, inspectCode = "$groupCode.${labels.size + 2}")
            }
            if (withOneDisabled) {
                ShowcaseButton(stringResource(R.string.sc_b_disabled), r, BtnVariant.Filled, enabled = false, inspectCode = "$groupCode.${labels.size + 1}")
            }
        }
    }
}
