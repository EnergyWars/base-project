package com.wafflehq.lib.uicore.csv

import android.app.Application
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.lib.uicore.R
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CsvExportIconButtonTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `export button is displayed with its content description`() {
        rule.setContent {
            CsvExportIconButton(
                fileName = "export.csv",
                snackbarHostState = SnackbarHostState(),
                csvContent = { "a,b" }
            )
        }

        rule.onNodeWithContentDescription(context.getString(R.string.uicore_csv_export_action)).assertExists()
    }

    @Test
    fun `save launcher is created during composition`() {
        var save: ((String, String) -> Unit)? = null
        rule.setContent { save = rememberCsvSaveLauncher(SnackbarHostState()) }

        rule.runOnIdle { assertNotNull(save) }
    }
}
