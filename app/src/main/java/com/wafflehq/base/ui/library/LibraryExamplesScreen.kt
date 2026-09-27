package com.wafflehq.base.ui.library

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.base.ui.components.AppHeaderScaffold
import com.wafflehq.base.ui.components.HeaderItem
import com.wafflehq.base.ui.library.demos.AstronomyDemo
import com.wafflehq.base.ui.library.demos.BackupCoreDemo
import com.wafflehq.base.ui.library.demos.ChartsDemo
import com.wafflehq.base.ui.library.demos.DatabaseDemo
import com.wafflehq.base.ui.library.demos.DiagnosticsDemo
import com.wafflehq.base.ui.library.demos.DraftsDemo
import com.wafflehq.base.ui.library.demos.EntryLockDemo
import com.wafflehq.base.ui.library.demos.FoldersDemo
import com.wafflehq.base.ui.library.demos.MaintenanceDemo
import com.wafflehq.base.ui.library.demos.MediaDemo
import com.wafflehq.base.ui.library.demos.ModulesDemo
import com.wafflehq.base.ui.library.demos.NavigationDemo
import com.wafflehq.base.ui.library.demos.NotificationsDemo
import com.wafflehq.base.ui.library.demos.PdfDemo
import com.wafflehq.base.ui.library.demos.PrefsBackupDemo
import com.wafflehq.base.ui.library.demos.QrDemo
import com.wafflehq.base.ui.library.demos.QuickPickerDemo
import com.wafflehq.base.ui.library.demos.SettingsDemo
import com.wafflehq.base.ui.library.demos.TextAreaDemo
import com.wafflehq.base.ui.library.demos.UiCoreDataDemo
import com.wafflehq.base.ui.library.demos.UiCoreFeedbackDemo
import com.wafflehq.base.ui.library.demos.UiCoreGesturesDemo
import com.wafflehq.base.ui.library.demos.UiCoreInputDemo
import com.wafflehq.base.ui.library.demos.UiCoreTimeFinanceDemo
import com.wafflehq.lib.uicore.components.AppEmptyState
import com.wafflehq.lib.uicore.components.AppSearchField
import com.wafflehq.lib.uicore.components.AppSnackbarHost
import com.wafflehq.lib.uicore.theme.AppSpacing

internal class LibraryDemoEntry(
    val id: String,
    @StringRes val titleRes: Int,
    val content: @Composable (SnackbarHostState) -> Unit,
)

internal object LibraryDemos {

    val all: List<LibraryDemoEntry> = listOf(
        LibraryDemoEntry("astronomy", R.string.libex_astronomy_title) { AstronomyDemo() },
        LibraryDemoEntry("qr", R.string.libex_qr_title) { QrDemo() },
        LibraryDemoEntry("maintenance", R.string.libex_maintenance_title) { MaintenanceDemo() },
        LibraryDemoEntry("drafts", R.string.libex_drafts_title) { DraftsDemo() },
        LibraryDemoEntry("modules", R.string.libex_modules_title) { ModulesDemo() },
        LibraryDemoEntry("entrylock", R.string.libex_entrylock_title) { EntryLockDemo() },
        LibraryDemoEntry("pdf", R.string.libex_pdf_title) { snackbar -> PdfDemo(snackbar) },
        LibraryDemoEntry("folders", R.string.libex_folders_title) { FoldersDemo() },
        LibraryDemoEntry("navigation", R.string.libex_navigation_title) { NavigationDemo() },
        LibraryDemoEntry("quickpicker", R.string.libex_quickpicker_title) { QuickPickerDemo() },
        LibraryDemoEntry("database", R.string.libex_database_title) { DatabaseDemo() },
        LibraryDemoEntry("textarea", R.string.libex_textarea_title) { TextAreaDemo() },
        LibraryDemoEntry("charts", R.string.libex_charts_title) { ChartsDemo() },
        LibraryDemoEntry("backupcore", R.string.libex_backupcore_title) { BackupCoreDemo() },
        LibraryDemoEntry("diagnostics", R.string.libex_diagnostics_title) { DiagnosticsDemo() },
        LibraryDemoEntry("media", R.string.libex_media_title) { MediaDemo() },
        LibraryDemoEntry("notifications", R.string.libex_notifications_title) { NotificationsDemo() },
        LibraryDemoEntry("prefsbackup", R.string.libex_prefsbackup_title) { PrefsBackupDemo() },
        LibraryDemoEntry("settings", R.string.libex_settings_title) { SettingsDemo() },
        LibraryDemoEntry("uicore_data", R.string.libex_uicore_data_title) { UiCoreDataDemo() },
        LibraryDemoEntry("uicore_time", R.string.libex_uicore_time_title) { UiCoreTimeFinanceDemo() },
        LibraryDemoEntry("uicore_feedback", R.string.libex_uicore_feedback_title) { snackbar -> UiCoreFeedbackDemo(snackbar) },
        LibraryDemoEntry("uicore_input", R.string.libex_uicore_input_title) { UiCoreInputDemo() },
        LibraryDemoEntry("uicore_gestures", R.string.libex_uicore_gestures_title) { UiCoreGesturesDemo() },
    )

    fun filter(entries: List<LibraryDemoEntry>, titles: List<String>, query: String): List<LibraryDemoEntry> {
        val needle = query.trim()
        if (needle.isEmpty()) return entries
        return entries.filterIndexed { index, _ -> titles[index].contains(needle, ignoreCase = true) }
    }
}

object LibraryExamplesTags {
    const val LIST = "libex_list"
    const val SEARCH = "libex_search"
    const val EMPTY = "libex_empty"
}

@Composable
fun LibraryExamplesScreen(
    onOpenMenu: () -> Unit,
    onNavigateHome: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var query by rememberSaveable { mutableStateOf("") }
    val entries = LibraryDemos.all
    val titles = entries.map { stringResource(it.titleRes) }
    val visible = LibraryDemos.filter(entries, titles, query)

    AppHeaderScaffold(
        title = stringResource(R.string.libex_title),
        activeItem = HeaderItem.None,
        onOpenMenu = onOpenMenu,
        onNavigateHome = onNavigateHome,
        onOpenSettings = onOpenSettings,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag(LibraryExamplesTags.LIST),
                contentPadding = PaddingValues(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                item(key = "lead") {
                    Text(
                        text = stringResource(R.string.libex_lead),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item(key = "search") {
                    AppSearchField(
                        query = query,
                        onQueryChange = { query = it },
                        placeholder = stringResource(R.string.libex_search_placeholder),
                        modifier = Modifier.fillMaxWidth().testTag(LibraryExamplesTags.SEARCH),
                    )
                }
                items(visible, key = { it.id }) { entry -> entry.content(snackbarHostState) }
                if (visible.isEmpty()) {
                    item(key = "empty") {
                        AppEmptyState(
                            text = stringResource(R.string.libex_no_results),
                            icon = Icons.Outlined.SearchOff,
                            modifier = Modifier.fillMaxWidth().testTag(LibraryExamplesTags.EMPTY),
                        )
                    }
                }
            }
            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
