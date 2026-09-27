package com.wafflehq.base.ui.library.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
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
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.csv.CsvEncoding
import com.wafflehq.lib.uicore.csv.CsvFormulaGuard
import com.wafflehq.lib.uicore.csv.CsvParser
import com.wafflehq.lib.uicore.csv.CsvPreviewDialog
import com.wafflehq.lib.uicore.io.csvExportFileName
import com.wafflehq.lib.uicore.io.jsonExportFileName
import com.wafflehq.lib.uicore.query.likeContainsPattern
import com.wafflehq.lib.uicore.serialization.enumFromNameOrDefault
import com.wafflehq.lib.uicore.serialization.parseRemindersCsv
import com.wafflehq.lib.uicore.theme.AppSpacing

internal enum class DemoPriority { LOW, NORMAL, HIGH }

internal data class CsvRoundTrip(
    val rows: List<List<String>>,
    val encoded: String,
    val reparsed: List<List<String>>,
)

internal object UiCoreDataDemoLogic {

    const val DEFAULT_CSV = "name,note\n\"Doe, Jane\",\"says \"\"hi\"\"\"\n=SUM(A1),ok"
    const val DEFAULT_FORMULA = "=1+1"
    const val DEFAULT_REMINDERS = "10, 30, x, -5, 60"
    const val FILE_PREFIX = "libex"

    fun roundTrip(csv: String): CsvRoundTrip {
        val rows = CsvParser.parse(csv)
        val header = rows.firstOrNull().orEmpty()
        val encoded = CsvEncoding.encode(header, rows.drop(1))
        return CsvRoundTrip(rows, encoded, CsvParser.parse(encoded))
    }

    fun rowLabel(row: List<String>): String = row.joinToString(" | ")

    fun priority(name: String): DemoPriority = enumFromNameOrDefault(name.trim().uppercase(), DemoPriority.NORMAL)

    fun reminders(csv: String): List<Int> = parseRemindersCsv(csv)

    fun likePattern(query: String): String = likeContainsPattern(query)

    fun neutralized(value: String): String = CsvFormulaGuard.neutralize(value)

    fun restored(value: String): String = CsvFormulaGuard.restore(value)

    fun fileNames(): Pair<String, String> = csvExportFileName(FILE_PREFIX) to jsonExportFileName(FILE_PREFIX)
}

internal object UiCoreDataTags {
    const val CSV_INPUT = "libex_uicore_csv_input"
    const val CSV_ROWS = "libex_uicore_csv_rows"
    const val CSV_ENCODED = "libex_uicore_csv_encoded"
    const val CSV_PREVIEW = "libex_uicore_csv_preview"
    const val PREVIEW_STATUS = "libex_uicore_csv_preview_status"
    const val FORMULA_INPUT = "libex_uicore_formula_input"
    const val FORMULA_RESULT = "libex_uicore_formula_result"
    const val LIKE_INPUT = "libex_uicore_like_input"
    const val LIKE_RESULT = "libex_uicore_like_result"
    const val ENUM_INPUT = "libex_uicore_enum_input"
    const val ENUM_RESULT = "libex_uicore_enum_result"
    const val REMINDERS_INPUT = "libex_uicore_reminders_input"
    const val REMINDERS_RESULT = "libex_uicore_reminders_result"
}

@Composable
internal fun UiCoreDataDemo() {
    var csv by remember { mutableStateOf(UiCoreDataDemoLogic.DEFAULT_CSV) }
    var formula by remember { mutableStateOf(UiCoreDataDemoLogic.DEFAULT_FORMULA) }
    var likeQuery by remember { mutableStateOf("50%_off") }
    var priorityName by remember { mutableStateOf("high") }
    var remindersCsv by remember { mutableStateOf(UiCoreDataDemoLogic.DEFAULT_REMINDERS) }
    var showPreview by remember { mutableStateOf(false) }
    var previewShared by remember { mutableStateOf(false) }
    val roundTrip = remember(csv) { UiCoreDataDemoLogic.roundTrip(csv) }
    val fileNames = remember { UiCoreDataDemoLogic.fileNames() }

    DemoSection(
        id = "uicore_data",
        titleRes = R.string.libex_uicore_data_title,
        descriptionRes = R.string.libex_uicore_data_desc,
        moduleRes = R.string.libex_module_uicore,
    ) {
        AppTextField(
            value = csv,
            onValueChange = { csv = it },
            label = { Text(stringResource(R.string.libex_uicore_csv_input)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth().testTag(UiCoreDataTags.CSV_INPUT),
        )
        Column(
            modifier = Modifier.fillMaxWidth().testTag(UiCoreDataTags.CSV_ROWS),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            DemoLabelText(stringResource(R.string.libex_uicore_csv_parsed, roundTrip.rows.size))
            roundTrip.rows.forEach { DemoBodyText(UiCoreDataDemoLogic.rowLabel(it)) }
        }
        DemoLabelText(stringResource(R.string.libex_uicore_csv_encoded))
        Text(
            text = roundTrip.encoded,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().testTag(UiCoreDataTags.CSV_ENCODED),
        )
        AppButton(
            text = stringResource(R.string.libex_uicore_csv_preview),
            role = AppButtonRole.Primary,
            variant = AppButtonVariant.Tonal,
            enabled = roundTrip.rows.isNotEmpty(),
            onClick = { showPreview = true },
            modifier = Modifier.testTag(UiCoreDataTags.CSV_PREVIEW),
        )
        if (previewShared) {
            DemoMetaText(
                text = stringResource(R.string.libex_uicore_csv_preview_shared),
                modifier = Modifier.testTag(UiCoreDataTags.PREVIEW_STATUS),
            )
        }
        AppHorizontalDivider()
        AppTextField(
            value = formula,
            onValueChange = { formula = it },
            label = { Text(stringResource(R.string.libex_uicore_formula_input)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(UiCoreDataTags.FORMULA_INPUT),
        )
        val neutralized = UiCoreDataDemoLogic.neutralized(formula)
        DemoBodyText(
            text = stringResource(R.string.libex_uicore_formula_result, neutralized, UiCoreDataDemoLogic.restored(neutralized)),
            modifier = Modifier.testTag(UiCoreDataTags.FORMULA_RESULT),
        )
        AppHorizontalDivider()
        AppTextField(
            value = likeQuery,
            onValueChange = { likeQuery = it },
            label = { Text(stringResource(R.string.libex_uicore_like_input)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(UiCoreDataTags.LIKE_INPUT),
        )
        DemoBodyText(
            text = stringResource(R.string.libex_uicore_like_result, UiCoreDataDemoLogic.likePattern(likeQuery)),
            modifier = Modifier.testTag(UiCoreDataTags.LIKE_RESULT),
        )
        AppHorizontalDivider()
        AppTextField(
            value = priorityName,
            onValueChange = { priorityName = it },
            label = { Text(stringResource(R.string.libex_uicore_enum_input)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(UiCoreDataTags.ENUM_INPUT),
        )
        DemoBodyText(
            text = stringResource(R.string.libex_uicore_enum_result, UiCoreDataDemoLogic.priority(priorityName).name),
            modifier = Modifier.testTag(UiCoreDataTags.ENUM_RESULT),
        )
        AppHorizontalDivider()
        AppTextField(
            value = remindersCsv,
            onValueChange = { remindersCsv = it },
            label = { Text(stringResource(R.string.libex_uicore_reminders_input)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(UiCoreDataTags.REMINDERS_INPUT),
        )
        DemoBodyText(
            text = stringResource(R.string.libex_uicore_reminders_result, UiCoreDataDemoLogic.reminders(remindersCsv).joinToString()),
            modifier = Modifier.testTag(UiCoreDataTags.REMINDERS_RESULT),
        )
        AppHorizontalDivider()
        DemoMetaText(stringResource(R.string.libex_uicore_file_names, fileNames.first, fileNames.second))
    }

    if (showPreview) {
        CsvPreviewDialog(
            header = roundTrip.rows.firstOrNull().orEmpty(),
            rows = roundTrip.rows.drop(1),
            onDismiss = { showPreview = false },
            onShare = {
                previewShared = true
                showPreview = false
            },
        )
    }
}
