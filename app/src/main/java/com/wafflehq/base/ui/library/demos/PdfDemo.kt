package com.wafflehq.base.ui.library.demos

import android.content.Context
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.lib.pdf.PdfExportUtils
import com.wafflehq.lib.pdf.PdfPageState
import com.wafflehq.lib.pdf.PdfPaints
import com.wafflehq.lib.pdf.drawDivider
import com.wafflehq.lib.pdf.drawSectionHeading
import com.wafflehq.lib.pdf.drawTitleBlock
import com.wafflehq.lib.pdf.drawWrappedText
import com.wafflehq.lib.pdf.pdfExportFileName
import com.wafflehq.lib.pdf.ui.PdfExportChoiceDialog
import com.wafflehq.lib.pdf.ui.PdfPreviewDialog
import com.wafflehq.lib.pdf.ui.rememberPdfSaveLauncher
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppTextField
import com.wafflehq.lib.uicore.theme.AppSpacing
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal data class PdfDemoResult(val file: File, val pageCount: Int, val sizeBytes: Long)

internal object PdfDemoLogic {

    const val FILE_PREFIX = "libex"
    const val MIN_PAGES = 1
    const val MAX_PAGES = 5
    private const val BYTES_PER_KIB = 1024L

    fun fileName(title: String): String = pdfExportFileName(FILE_PREFIX, title)

    fun sizeInKib(bytes: Long): Long = (bytes + BYTES_PER_KIB - 1) / BYTES_PER_KIB

    fun build(
        context: Context,
        title: String,
        subtitle: String,
        body: String,
        pageHeading: (Int) -> String,
        pages: Int,
        withWatermark: Boolean,
    ): PdfDemoResult {
        val document = PdfDocument()
        val state = PdfPageState(
            document = document,
            watermarkText = if (withWatermark) PdfExportUtils.watermarkText(context) else null,
        )
        state.drawTitleBlock(title, subtitle)
        repeat(pages.coerceIn(MIN_PAGES, MAX_PAGES)) { index ->
            if (index > 0) state.newPage()
            state.drawSectionHeading(pageHeading(index + 1))
            state.drawWrappedText(body, PdfPaints.body())
            state.drawDivider()
        }
        val pageCount = state.pageNumber
        state.finish()
        val file = PdfExportUtils.writeToCache(context, document, fileName(title))
        return PdfDemoResult(file = file, pageCount = pageCount, sizeBytes = file.length())
    }
}

internal object PdfTags {
    const val TITLE = "libex_pdf_title"
    const val PAGES = "libex_pdf_pages"
    const val WATERMARK = "libex_pdf_watermark"
    const val GENERATE = "libex_pdf_generate"
    const val RESULT = "libex_pdf_result"
    const val FILE_NAME = "libex_pdf_file_name"
    const val OPTIONS = "libex_pdf_options"
    const val SAVE = "libex_pdf_save"
}

@Composable
internal fun PdfDemo(
    snackbarHostState: SnackbarHostState,
    workDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var pages by remember { mutableIntStateOf(2) }
    var watermark by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf<PdfDemoResult?>(null) }
    var showOptions by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    val savePdf = rememberPdfSaveLauncher(snackbarHostState)
    val defaultTitle = stringResource(R.string.libex_pdf_default_title)
    val subtitle = stringResource(R.string.libex_pdf_subtitle)
    val body = stringResource(R.string.libex_pdf_body)
    val shareUnavailable = stringResource(R.string.libex_pdf_share_unavailable)
    val shareTitle = stringResource(R.string.libex_pdf_share_title)
    val effectiveTitle = title.ifBlank { defaultTitle }

    DemoSection(
        id = "pdf",
        titleRes = R.string.libex_pdf_title,
        descriptionRes = R.string.libex_pdf_desc,
        moduleRes = R.string.libex_module_pdf,
    ) {
        AppTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.libex_pdf_title_label)) },
            placeholder = { Text(defaultTitle) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(PdfTags.TITLE),
        )
        DemoStepperRow(
            label = stringResource(R.string.libex_pdf_pages),
            value = pages,
            range = PdfDemoLogic.MIN_PAGES..PdfDemoLogic.MAX_PAGES,
            step = 1,
            onValueChange = { pages = it },
            tag = PdfTags.PAGES,
        )
        DemoSwitchRow(
            label = stringResource(R.string.libex_pdf_watermark),
            checked = watermark,
            onCheckedChange = { watermark = it },
            tag = PdfTags.WATERMARK,
        )
        DemoMetaText(
            text = stringResource(R.string.libex_pdf_file_name, PdfDemoLogic.fileName(effectiveTitle)),
            modifier = Modifier.testTag(PdfTags.FILE_NAME),
        )
        AppButton(
            text = stringResource(R.string.libex_pdf_generate),
            role = AppButtonRole.Primary,
            variant = AppButtonVariant.Tonal,
            onClick = {
                scope.launch {
                    result = withContext(workDispatcher) {
                        PdfDemoLogic.build(
                            context = context,
                            title = effectiveTitle,
                            subtitle = subtitle,
                            body = body,
                            pageHeading = { number -> context.getString(R.string.libex_pdf_page_heading, number) },
                            pages = pages,
                            withWatermark = watermark,
                        )
                    }
                }
            },
            modifier = Modifier.testTag(PdfTags.GENERATE),
        )
        result?.let { generated ->
            DemoBodyText(
                text = stringResource(
                    R.string.libex_pdf_result,
                    generated.file.name,
                    generated.pageCount,
                    PdfDemoLogic.sizeInKib(generated.sizeBytes),
                ),
                modifier = Modifier.testTag(PdfTags.RESULT),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                AppButton(
                    text = stringResource(R.string.libex_pdf_options),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Outlined,
                    onClick = { showOptions = true },
                    modifier = Modifier.testTag(PdfTags.OPTIONS),
                )
                AppButton(
                    text = stringResource(R.string.libex_pdf_save),
                    role = AppButtonRole.Primary,
                    variant = AppButtonVariant.Text,
                    onClick = { savePdf(generated.file) },
                    modifier = Modifier.testTag(PdfTags.SAVE),
                )
            }
            if (showOptions) {
                PdfExportChoiceDialog(
                    title = generated.file.name,
                    onDismissRequest = { showOptions = false },
                    onShare = {
                        showOptions = false
                        val shared = runCatching { PdfExportUtils.sharePdf(context, generated.file, shareTitle) }
                        if (shared.isFailure) scope.launch { snackbarHostState.showSnackbar(shareUnavailable) }
                    },
                    onSave = {
                        showOptions = false
                        savePdf(generated.file)
                    },
                    onPreview = {
                        showOptions = false
                        showPreview = true
                    },
                )
            }
            if (showPreview) {
                PdfPreviewDialog(
                    file = generated.file,
                    onDismiss = { showPreview = false },
                    onShare = {
                        val shared = runCatching { PdfExportUtils.sharePdf(context, generated.file, shareTitle) }
                        if (shared.isFailure) scope.launch { snackbarHostState.showSnackbar(shareUnavailable) }
                    },
                )
            }
        }
    }
}
