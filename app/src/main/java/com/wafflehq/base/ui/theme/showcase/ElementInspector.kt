package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import android.content.ClipData
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme
import kotlinx.coroutines.launch

val LocalElementInspector = staticCompositionLocalOf<(String) -> Unit> { {} }

@Composable
fun ElementInspectorHost(content: @Composable () -> Unit) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    CompositionLocalProvider(LocalElementInspector provides { selected = it }) {
        content()
    }
    selected?.let { id ->
        ElementIdDialog(id = id, onDismiss = { selected = null })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun Modifier.inspectId(id: String, onClick: () -> Unit = {}): Modifier {
    val inspector = LocalElementInspector.current
    return this.combinedClickable(onClick = onClick, onDoubleClick = { inspector(id) })
}

@Composable
internal fun Modifier.inspectTap(id: String): Modifier {
    val inspector = LocalElementInspector.current
    return this.pointerInput(id) { detectTapGestures(onDoubleTap = { inspector(id) }) }
}

@Composable
fun InspectSection(id: String, content: @Composable () -> Unit) {
    val inspector = LocalElementInspector.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(id) { detectTapGestures(onDoubleTap = { inspector(id) }) },
        content = { content() },
    )
}

@Composable
private fun ElementIdDialog(id: String, onDismiss: () -> Unit) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(radiusL)
                .background(AppTheme.colors.surface)
                .border(1.dp, AppTheme.colors.outline, radiusL)
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                stringResource(R.string.inspect_title),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono.fontFamily, letterSpacing = 0.8.sp),
                color = AppTheme.colors.onSurfaceVariant,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(radiusS)
                    .background(AppTheme.colors.surfaceVariant)
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            ) {
                Text(
                    id,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = mono.fontFamily),
                    color = AppTheme.colors.onSurface,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                TextButton(onClick = {
                    scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("", id))) }
                }) {
                    Text(stringResource(R.string.inspect_copy))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.inspect_close))
                }
            }
        }
    }
}
