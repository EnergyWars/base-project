package com.wafflehq.lib.pdf.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.pdf.PdfPageSource
import com.wafflehq.lib.pdf.R
import com.wafflehq.lib.uicore.theme.AppSpacing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.wafflehq.lib.uicore.components.AppCircularProgress

internal object PdfViewerTestTags {
    const val PAGES = "pdf_viewer_pages"
}

private const val RENDER_DEBOUNCE_MS = 250L

@Composable
fun PdfPagesView(source: PdfPageSource, modifier: Modifier = Modifier) {
    val zoomState = remember(source) { PdfZoomState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var viewportWidthPx by remember { mutableIntStateOf(0) }
    var renderZoom by remember(source) { mutableFloatStateOf(PdfZoomState.DEFAULT_MIN_ZOOM) }

    LaunchedEffect(zoomState) {
        snapshotFlow { zoomState.zoom }.collectLatest { zoom ->
            delay(RENDER_DEBOUNCE_MS)
            renderZoom = PdfZoomState.renderZoomFor(zoom)
        }
    }

    val targetWidthPx = (viewportWidthPx * renderZoom).roundToInt()

    PdfZoomContainer(
        state = zoomState,
        onScrollBy = { delta ->
            scope.launch { listState.scroll(MutatePriority.UserInput) { scrollBy(delta) } }
        },
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { viewportWidthPx = it.width }
            .testTag(PdfViewerTestTags.PAGES)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
        ) {
            items(source.pageCount) { index ->
                PdfReaderPage(source = source, index = index, targetWidthPx = targetWidthPx)
            }
        }
    }
}

@Composable
private fun PdfReaderPage(source: PdfPageSource, index: Int, targetWidthPx: Int) {
    var bitmap by remember(source, index) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(source, index, targetWidthPx) {
        if (targetWidthPx <= 0) return@LaunchedEffect
        val rendered = try {
            source.renderPageToWidth(index, targetWidthPx)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } catch (e: OutOfMemoryError) {
            null
        }
        if (rendered != null) {
            val previous = bitmap
            bitmap = rendered
            if (previous != null) {
                withFrameNanos { }
                withFrameNanos { }
                previous.recycle()
            }
        }
    }

    DisposableEffect(source, index) {
        onDispose { bitmap?.recycle() }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            val imageBitmap = remember(currentBitmap) { currentBitmap.asImageBitmap() }
            Image(
                bitmap = imageBitmap,
                contentDescription = stringResource(R.string.pdf_page_label, index + 1),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                AppCircularProgress()
            }
        }
        Spacer(Modifier.height(AppSpacing.xs))
        Text(
            stringResource(R.string.pdf_page_label, index + 1),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
