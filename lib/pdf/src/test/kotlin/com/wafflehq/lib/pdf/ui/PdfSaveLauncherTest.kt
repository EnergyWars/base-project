package com.wafflehq.lib.pdf.ui

import android.app.Application
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class PdfSaveLauncherTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `save launcher is created during composition`() {
        var save: ((File) -> Unit)? = null
        rule.setContent { save = rememberPdfSaveLauncher(SnackbarHostState()) }

        rule.runOnIdle { assertNotNull(save) }
    }
}
