package com.wafflehq.lib.settings.legal.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.wafflehq.lib.navigation.settings.SettingsScaffold
import com.wafflehq.lib.settings.R
import com.wafflehq.lib.navigation.R as NavR
import com.wafflehq.lib.uicore.components.AppHorizontalDivider

data class ManualOssEntry(
    val name: String,
    val license: String,
    @param:StringRes val licenseTextRes: Int? = null
)

object OpenSourceLicensesTestTags {
    fun manualEntry(name: String) = "oss_manual_entry_$name"
    fun manualEntryLicenseText(name: String) = "oss_manual_entry_license_text_$name"
}

@Composable
fun OpenSourceLicensesScreen(
    @RawRes aboutLibrariesRes: Int,
    manualEntries: List<ManualOssEntry>,
    onBack: () -> Unit
) {
    SettingsScaffold(
        title = stringResource(R.string.appsettings_legal_open_source_licenses),
        onBack = onBack,
        backDescription = stringResource(NavR.string.navigation_navigate_back)
    ) { padding ->
        val libraries by produceLibraries(aboutLibrariesRes)
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ManualOssEntryList(
                entries = manualEntries,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
            )
            LibrariesContainer(
                libraries = libraries,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun ManualOssEntryList(
    entries: List<ManualOssEntry>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.appsettings_legal_open_source_licenses_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        entries.forEach { entry ->
            var showFullText by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .let { if (entry.licenseTextRes != null) it.clickable { showFullText = !showFullText } else it }
                    .padding(vertical = AppSpacing.md)
                    .testTag(OpenSourceLicensesTestTags.manualEntry(entry.name)),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = entry.name,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = entry.license,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.licenseTextRes != null) {
                    Text(
                        text = stringResource(
                            if (showFullText) {
                                R.string.appsettings_oss_license_hide_full_text
                            } else {
                                R.string.appsettings_oss_license_view_full_text
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (showFullText) {
                        Text(
                            text = stringResource(entry.licenseTextRes),
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(top = AppSpacing.sm)
                                .testTag(OpenSourceLicensesTestTags.manualEntryLicenseText(entry.name))
                        )
                    }
                }
            }
            AppHorizontalDivider()
        }
        Text(
            text = stringResource(R.string.appsettings_legal_open_source_licenses_generated_section),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = AppSpacing.sm)
        )
    }
}
