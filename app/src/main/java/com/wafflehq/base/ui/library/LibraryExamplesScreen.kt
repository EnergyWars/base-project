package com.wafflehq.base.ui.library

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.wafflehq.base.R
import com.wafflehq.uikit.astronomy.MoonPhaseCalculator
import com.wafflehq.uikit.astronomy.SunCalculator
import com.wafflehq.uikit.components.AppButton
import com.wafflehq.uikit.components.AppCard
import com.wafflehq.uikit.components.AppScaffold
import com.wafflehq.uikit.components.AppTextField
import com.wafflehq.uikit.components.HeaderItem
import com.wafflehq.uikit.components.SettingsSwitchRow
import com.wafflehq.uikit.database.crypto.AndroidKeystoreKeyWrapper
import com.wafflehq.uikit.database.open.DatabaseOpenPlanner
import com.wafflehq.uikit.database.state.EncryptionStateStore
import com.wafflehq.uikit.drafts.runDraftAutosaveLoop
import com.wafflehq.uikit.entrylock.AuthResult
import com.wafflehq.uikit.entrylock.BiometricEntryAuthenticator
import com.wafflehq.uikit.folders.ui.FolderCard
import com.wafflehq.uikit.maintenance.MaintenanceTask
import com.wafflehq.uikit.maintenance.MaintenanceTaskRunner
import com.wafflehq.uikit.modules.FeatureModule
import com.wafflehq.uikit.modules.FeatureModuleRegistry
import com.wafflehq.uikit.modules.ModuleNavigationEntry
import com.wafflehq.uikit.modules.ModuleSettingsEntry
import com.wafflehq.uikit.navigation.EditorScaffold
import com.wafflehq.uikit.pdf.PdfExportUtils
import com.wafflehq.uikit.pdf.PdfPageState
import com.wafflehq.uikit.pdf.drawTitleBlock
import com.wafflehq.uikit.pdf.drawWrappedText
import com.wafflehq.uikit.qr.generateQrBitmap
import com.wafflehq.uikit.quickpicker.QuickDateInputDialog
import com.wafflehq.uikit.quickpicker.QuickTimeInputDialog
import com.wafflehq.uikit.textarea.KeyboardAwareTextArea
import com.wafflehq.uikit.theme.AppRole
import com.wafflehq.uikit.theme.AppSpacing
import com.wafflehq.uikit.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.graphics.pdf.PdfDocument

@Composable
fun LibraryExamplesScreen(
    onOpenMenu: () -> Unit,
    onNavigateHome: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    AppScaffold(
        activeItem = HeaderItem.None,
        onOpenMenu = onOpenMenu,
        onNavigateHome = onNavigateHome,
        onOpenSettings = onOpenSettings,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .padding(padding),
            contentPadding = PaddingValues(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            item {
                Text(
                    text = stringResource(R.string.library_examples_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppTheme.colors.onBackground,
                )
            }
            item {
                Text(
                    text = stringResource(R.string.library_examples_lead),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.colors.onSurfaceVariant,
                )
            }
            item { AstronomyDemo() }
            item { QrDemo() }
            item { MaintenanceDemo() }
            item { DraftsDemo() }
            item { ModulesDemo() }
            item { EntryLockDemo() }
            item { PdfDemo() }
            item { FoldersDemo() }
            item { NavigationDemo() }
            item { QuickPickerDemo() }
            item { DatabaseDemo() }
            item { TextAreaDemo() }
        }
    }
}

@Composable
private fun DemoSection(titleRes: Int, descRes: Int, content: @Composable () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.onSurface,
            )
            Text(
                text = stringResource(descRes),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
            )
            content()
        }
    }
}

@Composable
private fun AstronomyDemo() {
    DemoSection(R.string.lib_astronomy_title, R.string.lib_astronomy_desc) {
        val today = remember { LocalDate.now() }
        val sunTimes = remember(today) { SunCalculator.calculate(today, 52.52, 13.405) }
        val moonPhase = remember(today) { MoonPhaseCalculator.nearestPhase(today) }
        val fmt = remember { DateTimeFormatter.ofPattern("HH:mm") }
        Text(
            text = stringResource(
                R.string.lib_astronomy_result,
                sunTimes.sunrise?.format(fmt) ?: "–",
                sunTimes.sunset?.format(fmt) ?: "–",
                moonPhase.symbol,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurface,
        )
    }
}

@Composable
private fun QrDemo() {
    DemoSection(R.string.lib_qr_title, R.string.lib_qr_desc) {
        var text by remember { mutableStateOf("https://wafflehq.com") }
        AppTextField(
            value = text,
            onValueChange = { text = it },
            label = stringResource(R.string.lib_qr_input_label),
            role = AppRole.Primary,
        )
        val bitmap = remember(text) { if (text.isNotBlank()) generateQrBitmap(text, 400) else null }
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.lib_qr_input_label),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(160.dp),
            )
        } else {
            Text(
                text = stringResource(R.string.lib_qr_empty),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.error.accent,
            )
        }
    }
}

private class DemoMaintenanceTask(
    override val id: String,
    private val shouldFail: Boolean,
) : MaintenanceTask {
    override suspend fun isEnabled(): Boolean = true
    override suspend fun run() {
        if (shouldFail) error("demo failure")
    }
}

@Composable
private fun MaintenanceDemo() {
    DemoSection(R.string.lib_maintenance_title, R.string.lib_maintenance_desc) {
        val scope = rememberCoroutineScope()
        val results = remember { mutableStateListOf<MaintenanceTaskRunner.Outcome>() }
        AppButton(
            text = stringResource(R.string.lib_maintenance_run),
            role = AppRole.Primary,
            onClick = {
                scope.launch {
                    val runner = MaintenanceTaskRunner(
                        setOf(
                            DemoMaintenanceTask("cleanup", shouldFail = false),
                            DemoMaintenanceTask("sync", shouldFail = false),
                            DemoMaintenanceTask("broken-task", shouldFail = true),
                        ),
                    )
                    results.clear()
                    results.addAll(runner.runAll())
                }
            },
        )
        results.forEach { outcome ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Icon(
                    imageVector = if (outcome.error == null) Icons.Filled.CheckCircle else Icons.Filled.Error,
                    contentDescription = null,
                    tint = if (outcome.error == null) AppTheme.colors.success.accent else AppTheme.colors.error.accent,
                )
                Text(outcome.taskId, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurface)
            }
        }
    }
}

@Composable
private fun DraftsDemo() {
    DemoSection(R.string.lib_drafts_title, R.string.lib_drafts_desc) {
        var draft by remember { mutableStateOf("") }
        var savedAt by remember { mutableStateOf<String?>(null) }
        val fmt = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
        AppTextField(
            value = draft,
            onValueChange = { draft = it },
            label = stringResource(R.string.lib_drafts_field_label),
            role = AppRole.Secondary,
        )
        androidx.compose.runtime.LaunchedEffect(Unit) {
            runDraftAutosaveLoop(intervalMs = 3_000L) {
                savedAt = LocalTime.now().format(fmt)
            }
        }
        Text(
            text = savedAt?.let { stringResource(R.string.lib_drafts_saved_at, it) }
                ?: stringResource(R.string.lib_drafts_not_saved_yet),
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.colors.onSurfaceVariant,
        )
    }
}

private class DemoFeatureModule(
    override val id: String,
    val labelRes: Int,
    private val initiallyEnabled: Boolean,
) : FeatureModule {
    private val enabledFlow = MutableStateFlow(initiallyEnabled)
    override val settingsOrder: Int = 0
    override val isEnabled: StateFlow<Boolean> get() = enabledFlow
    override val navigation: ModuleNavigationEntry? = null
    override val settings: ModuleSettingsEntry? = null
    override suspend fun setEnabled(enabled: Boolean) {
        enabledFlow.value = enabled
    }
}

@Composable
private fun ModulesDemo() {
    DemoSection(R.string.lib_modules_title, R.string.lib_modules_desc) {
        val scope = rememberCoroutineScope()
        val registry = remember {
            FeatureModuleRegistry(
                listOf(
                    DemoFeatureModule("weather", R.string.lib_modules_entry_weather, initiallyEnabled = true),
                    DemoFeatureModule("reminders", R.string.lib_modules_entry_reminders, initiallyEnabled = false),
                    DemoFeatureModule("backup", R.string.lib_modules_entry_backup, initiallyEnabled = false),
                ),
            )
        }
        val states = remember { mutableStateMapOf<String, Boolean>() }
        androidx.compose.runtime.LaunchedEffect(registry) {
            registry.all.forEach { module ->
                launch {
                    module.isEnabled.collect { states[module.id] = it }
                }
            }
        }
        registry.all.forEach { module ->
            val demo = module as DemoFeatureModule
            SettingsSwitchRow(
                title = stringResource(demo.labelRes),
                subtitle = module.id,
                checked = states[module.id] ?: false,
                onCheckedChange = { checked -> scope.launch { module.setEnabled(checked) } },
            )
        }
    }
}

@Composable
private fun EntryLockDemo() {
    DemoSection(R.string.lib_entrylock_title, R.string.lib_entrylock_desc) {
        val context = LocalContext.current
        val activity = context as? FragmentActivity
        val authenticator = remember { BiometricEntryAuthenticator(context) }
        var status by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()
        val available = remember { authenticator.isAvailable() }
        val needsFragmentActivityText = stringResource(R.string.lib_entrylock_needs_fragment_activity)
        val promptTitleText = stringResource(R.string.lib_entrylock_prompt_title)
        val successText = stringResource(R.string.lib_entrylock_success)
        val failedText = stringResource(R.string.lib_entrylock_failed)
        val cancelledText = stringResource(R.string.lib_entrylock_cancelled)
        val unavailableText = stringResource(R.string.lib_entrylock_unavailable)
        if (!available) {
            Text(
                text = unavailableText,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
            )
        } else {
            AppButton(
                text = stringResource(R.string.lib_entrylock_reveal),
                role = AppRole.Warning,
                onClick = {
                    val currentActivity = activity
                    if (currentActivity == null) {
                        status = needsFragmentActivityText
                    } else {
                        scope.launch {
                            val result = authenticator.authenticate(currentActivity, promptTitleText)
                            status = when (result) {
                                AuthResult.SUCCESS -> successText
                                AuthResult.FAILED -> failedText
                                AuthResult.CANCELLED -> cancelledText
                                AuthResult.NOT_AVAILABLE -> unavailableText
                            }
                        }
                    }
                },
            )
        }
        status?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurface)
        }
    }
}

private fun buildDemoPdf(context: Context, watermark: String): String {
    val document = PdfDocument()
    val state = PdfPageState(document, watermarkText = watermark)
    state.drawTitleBlock("WaffleHQ uikit", "PDF demo export")
    state.drawWrappedText(
        "This PDF was generated by com.wafflehq.uikit.pdf.PdfPageState from the example app.",
        android.graphics.Paint().apply { textSize = 14f },
    )
    state.finish()
    val file = PdfExportUtils.writeToCache(context, document, "uikit_demo.pdf")
    return file.absolutePath
}

@Composable
private fun PdfDemo() {
    DemoSection(R.string.lib_pdf_title, R.string.lib_pdf_desc) {
        val context = LocalContext.current
        var status by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()
        val watermark = stringResource(R.string.lib_pdf_watermark)
        AppButton(
            text = stringResource(R.string.lib_pdf_generate),
            role = AppRole.Success,
            onClick = {
                scope.launch {
                    val path = withContext(Dispatchers.IO) { buildDemoPdf(context, watermark) }
                    status = path
                }
            },
        )
        status?.let {
            Text(
                text = stringResource(R.string.lib_pdf_saved, it),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
            )
        }
    }
}

private data class DemoFolder(val name: String, val count: Int, var expanded: Boolean)

@Composable
private fun FoldersDemo() {
    DemoSection(R.string.lib_folders_title, R.string.lib_folders_desc) {
        val folders = remember {
            mutableStateListOf(
                DemoFolder("Rezepte", 4, false),
                DemoFolder("Notizen", 12, false),
                DemoFolder("Archiv", 0, false),
            )
        }
        folders.forEachIndexed { index, folder ->
            FolderCard(
                name = folder.name,
                countText = stringResource(R.string.lib_folders_count, folder.count),
                lastActivityAt = remember { LocalDateTime.now() },
                isDropTarget = false,
                onOpen = {},
                onRename = {},
                onDelete = { folders.removeAt(index) },
                onPositioned = {},
                isExpanded = folder.expanded,
                onToggleExpand = {
                    folders[index] = folder.copy(expanded = !folder.expanded)
                },
            )
        }
    }
}

@Composable
private fun NavigationDemo() {
    DemoSection(R.string.lib_navigation_title, R.string.lib_navigation_desc) {
        var showEditor by remember { mutableStateOf(false) }
        AppButton(
            text = stringResource(R.string.lib_navigation_open),
            role = AppRole.Tertiary,
            onClick = { showEditor = true },
        )
        if (showEditor) {
            var fieldValue by remember { mutableStateOf("") }
            EditorScaffold(
                title = stringResource(R.string.lib_navigation_editor_title),
                onBack = { showEditor = false },
                onSave = { showEditor = false },
                canSave = fieldValue.isNotBlank(),
                backDescription = stringResource(R.string.label_back),
                saveLabel = stringResource(R.string.lib_navigation_save),
            ) { padding ->
                Column(modifier = Modifier.padding(padding).padding(AppSpacing.lg)) {
                    AppTextField(
                        value = fieldValue,
                        onValueChange = { fieldValue = it },
                        label = stringResource(R.string.lib_navigation_editor_field),
                        role = AppRole.Tertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickPickerDemo() {
    DemoSection(R.string.lib_quickpicker_title, R.string.lib_quickpicker_desc) {
        var showDate by remember { mutableStateOf(false) }
        var showTime by remember { mutableStateOf(false) }
        var pickedDate by remember { mutableStateOf<LocalDate?>(null) }
        var pickedTime by remember { mutableStateOf<LocalTime?>(null) }

        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            AppButton(
                text = pickedDate?.toString() ?: stringResource(R.string.lib_quickpicker_pick_date),
                role = AppRole.Primary,
                onClick = { showDate = true },
            )
            AppButton(
                text = pickedTime?.toString() ?: stringResource(R.string.lib_quickpicker_pick_time),
                role = AppRole.Secondary,
                onClick = { showTime = true },
            )
        }

        if (showDate) {
            QuickDateInputDialog(
                label = stringResource(R.string.lib_quickpicker_pick_date),
                onDismiss = { showDate = false },
                onConfirm = { pickedDate = it; showDate = false },
            )
        }
        if (showTime) {
            QuickTimeInputDialog(
                label = stringResource(R.string.lib_quickpicker_pick_time),
                onDismiss = { showTime = false },
                onConfirm = { pickedTime = it; showTime = false },
            )
        }
    }
}

@Composable
private fun DatabaseDemo() {
    DemoSection(R.string.lib_database_title, R.string.lib_database_desc) {
        val context = LocalContext.current
        var status by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()
        val readyTemplate = stringResource(R.string.lib_database_ready)
        val errorTemplate = stringResource(R.string.lib_database_error)
        AppButton(
            text = stringResource(R.string.lib_database_plan),
            role = AppRole.Neutral,
            onClick = {
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        runCatching {
                            val stateStore = EncryptionStateStore(context, prefsName = "uikit_demo_encryption_state")
                            val keyWrapper = AndroidKeystoreKeyWrapper("uikit_demo_key")
                            val planner = DatabaseOpenPlanner(
                                context = context,
                                databaseFileName = "uikit_demo.db",
                                legacyDatabaseFileName = null,
                                recoveryPlaceholderFileName = "uikit_demo_recovery.db",
                                stateStore = stateStore,
                                keyWrapper = keyWrapper,
                            )
                            planner.plan()
                        }
                    }
                    status = result.fold(
                        onSuccess = { plan -> readyTemplate.format(plan.fileName) },
                        onFailure = { e -> errorTemplate.format(e.message ?: "?") },
                    )
                }
            },
        )
        status?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun TextAreaDemo() {
    DemoSection(R.string.lib_textarea_title, R.string.lib_textarea_desc) {
        var text by remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, AppTheme.colors.outline, RoundedCornerShape(8.dp))
                .padding(AppSpacing.sm),
        ) {
            KeyboardAwareTextArea(
                value = text,
                onValueChange = { text = it },
                minLines = 3,
                label = { Text(stringResource(R.string.lib_textarea_label)) },
                placeholder = { Text(stringResource(R.string.lib_textarea_placeholder)) },
            )
        }
    }
}
