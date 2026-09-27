package com.wafflehq.lib.pdf.ui

import androidx.compose.runtime.staticCompositionLocalOf
import java.io.File

val LocalPdfReaderOpenAction = staticCompositionLocalOf<((File) -> Unit)?> { null }
