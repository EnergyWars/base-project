package com.wafflehq.lib.uicore.testing

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.testTagsAsResourceIds(enabled: Boolean): Modifier =
    semantics { testTagsAsResourceId = enabled }

@Composable
fun Modifier.exposeTestTagsInDebugBuilds(): Modifier =
    testTagsAsResourceIds(LocalContext.current.isDebuggableApp())

fun Context.isDebuggableApp(): Boolean =
    applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
