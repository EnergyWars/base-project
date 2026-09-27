package com.wafflehq.base.ui.library.demos

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.lib.qr.generateQrBitmap
import com.wafflehq.lib.uicore.components.AppBanner
import com.wafflehq.lib.uicore.components.AppBannerRole
import com.wafflehq.lib.uicore.components.AppTextField

internal object QrDemoLogic {

    const val DEFAULT_TEXT = "https://wafflehq.com"
    const val BITMAP_SIZE_PX = 400
    const val MAX_TEXT_LENGTH = 2_000

    fun render(text: String, sizePx: Int = BITMAP_SIZE_PX): Bitmap? =
        if (text.isBlank() || text.length > MAX_TEXT_LENGTH) null else generateQrBitmap(text, sizePx)
}

internal object QrTags {
    const val INPUT = "libex_qr_input"
    const val IMAGE = "libex_qr_image"
    const val EMPTY = "libex_qr_empty"
}

@Composable
internal fun QrDemo(initialText: String = QrDemoLogic.DEFAULT_TEXT) {
    var text by remember { mutableStateOf(initialText) }
    val bitmap = remember(text) { QrDemoLogic.render(text) }
    val description = stringResource(R.string.libex_qr_image_description)

    DemoSection(
        id = "qr",
        titleRes = R.string.libex_qr_title,
        descriptionRes = R.string.libex_qr_desc,
        moduleRes = R.string.libex_module_qr,
    ) {
        AppTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.libex_qr_input_label)) },
            modifier = Modifier.fillMaxWidth().testTag(QrTags.INPUT),
        )
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = description,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(QR_IMAGE_SIZE).testTag(QrTags.IMAGE),
            )
        } else {
            AppBanner(
                text = stringResource(R.string.libex_qr_empty),
                role = AppBannerRole.Warning,
                modifier = Modifier.fillMaxWidth().testTag(QrTags.EMPTY),
            )
        }
    }
}

private val QR_IMAGE_SIZE = 168.dp
