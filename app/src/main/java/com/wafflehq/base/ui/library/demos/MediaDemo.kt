package com.wafflehq.base.ui.library.demos

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.base.R
import com.wafflehq.lib.media.MediaDirs
import com.wafflehq.lib.media.MediaFileManager
import com.wafflehq.lib.media.UprightBitmapLoader
import com.wafflehq.lib.qr.generateQrBitmap
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppBanner
import com.wafflehq.lib.uicore.components.AppBannerRole
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

internal data class MediaDemoResult(
    val width: Int,
    val height: Int,
    val storedBytes: Long,
    val bitmap: Bitmap,
)

internal object MediaDemoLogic {

    const val SAMPLE_TEXT = "libex-media"
    const val SAMPLE_SIZE_PX = 256
    const val MAX_DIMENSION_PX = 128
    private const val JPEG_QUALITY = 90

    fun directories(context: Context): List<String> = MediaDirs.ALL.map { File(context.filesDir, it).path }

    fun storeAndLoad(context: Context): MediaDemoResult? {
        val source = generateQrBitmap(SAMPLE_TEXT, SAMPLE_SIZE_PX) ?: return null
        val bytes = ByteArrayOutputStream().use { output ->
            source.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            output.toByteArray()
        }
        val manager = MediaFileManager(context)
        val path = manager.saveVocabularyImageBytes(bytes) ?: return null
        val storedBytes = File(path).length()
        val loaded = UprightBitmapLoader.load(path, MAX_DIMENSION_PX)
        manager.deleteFile(path)
        return loaded?.let { MediaDemoResult(it.width, it.height, storedBytes, it) }
    }
}

internal object MediaTags {
    const val RUN = "libex_media_run"
    const val RESULT = "libex_media_result"
    const val IMAGE = "libex_media_image"
    const val ERROR = "libex_media_error"
    const val DIRECTORIES = "libex_media_directories"
}

@Composable
internal fun MediaDemo(workDispatcher: CoroutineDispatcher = Dispatchers.Default) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val directories = remember { MediaDemoLogic.directories(context) }
    var result by remember { mutableStateOf<MediaDemoResult?>(null) }
    var failed by remember { mutableStateOf(false) }
    val description = stringResource(R.string.libex_media_image_description)

    DemoSection(
        id = "media",
        titleRes = R.string.libex_media_title,
        descriptionRes = R.string.libex_media_desc,
        moduleRes = R.string.libex_module_media,
    ) {
        DemoMetaText(
            text = stringResource(R.string.libex_media_directories, MediaDirs.ROOTS.joinToString(), directories.size),
            modifier = Modifier.testTag(MediaTags.DIRECTORIES),
        )
        directories.forEach { DemoMetaText(it) }
        AppButton(
            text = stringResource(R.string.libex_media_run),
            role = AppButtonRole.Primary,
            variant = AppButtonVariant.Tonal,
            onClick = {
                scope.launch {
                    val outcome = withContext(workDispatcher) { MediaDemoLogic.storeAndLoad(context) }
                    result = outcome
                    failed = outcome == null
                }
            },
            modifier = Modifier.testTag(MediaTags.RUN),
        )
        result?.let { outcome ->
            DemoBodyText(
                text = stringResource(R.string.libex_media_result, outcome.width, outcome.height, outcome.storedBytes),
                modifier = Modifier.testTag(MediaTags.RESULT),
            )
            Image(
                bitmap = outcome.bitmap.asImageBitmap(),
                contentDescription = description,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(PREVIEW_SIZE).testTag(MediaTags.IMAGE),
            )
        }
        if (failed) {
            AppBanner(
                text = stringResource(R.string.libex_media_failed),
                role = AppBannerRole.Error,
                modifier = Modifier.fillMaxWidth().testTag(MediaTags.ERROR),
            )
        }
    }
}

private val PREVIEW_SIZE = 96.dp
