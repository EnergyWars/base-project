package com.wafflehq.lib.settings.legal.ui

import com.wafflehq.lib.uicore.theme.AppSpacing
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wafflehq.lib.navigation.settings.SettingsScaffold
import com.wafflehq.lib.navigation.R as NavR

data class LegalSection(@param:StringRes val titleRes: Int, @param:StringRes val bodyRes: Int)

object LegalTextScreenTestTags {
    const val CONTENT = "legal_text_content"
    fun section(@StringRes titleRes: Int) = "legal_text_section_$titleRes"
}

@Composable
fun LegalTextScreen(
    title: String,
    onBack: () -> Unit,
    sections: List<LegalSection>
) {
    SettingsScaffold(
        title = title,
        onBack = onBack,
        backDescription = stringResource(NavR.string.navigation_navigate_back)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
                .verticalScroll(rememberScrollState())
                .testTag(LegalTextScreenTestTags.CONTENT),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            sections.forEach { section ->
                Column(
                    modifier = Modifier.testTag(LegalTextScreenTestTags.section(section.titleRes)),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(section.titleRes),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(section.bodyRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
