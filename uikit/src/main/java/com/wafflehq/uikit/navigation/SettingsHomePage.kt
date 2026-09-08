package com.wafflehq.uikit.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wafflehq.uikit.components.SettingsGroupDivider
import com.wafflehq.uikit.components.SettingsScaffold
import com.wafflehq.uikit.theme.AppTheme

object SettingsHomePageTestTags {
    const val LIST = "settings_home_list"
    fun entry(title: String) = "settings_home_entry_$title"
}

@Immutable
data class SettingsHomeEntry(
    val title: String,
    val subtitle: String? = null,
    val highlighted: Boolean = false,
    val onClick: () -> Unit,
)

@Composable
fun SettingsHomePage(
    title: String,
    entries: List<SettingsHomeEntry>,
    onBack: () -> Unit,
    backDescription: String,
    modifier: Modifier = Modifier,
) {
    SettingsScaffold(
        title = title,
        onBack = onBack,
        backDescription = backDescription,
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag(SettingsHomePageTestTags.LIST),
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth().background(AppTheme.colors.surface)) {
                    entries.forEachIndexed { index, entry ->
                        SettingsHomeRow(
                            entry = entry,
                            modifier = Modifier.testTag(SettingsHomePageTestTags.entry(entry.title)),
                        )
                        if (index < entries.lastIndex) SettingsGroupDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHomeRow(entry: SettingsHomeEntry, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val titleColor = if (entry.highlighted) colors.primary.accent else colors.onSurface
    val subtitleColor = if (entry.highlighted) colors.primary.accent else colors.onSurfaceVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (entry.highlighted) colors.primary.container else Color.Transparent)
            .clickable(onClick = entry.onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = entry.title,
                style = TextStyle(
                    fontWeight = if (entry.highlighted) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = if (entry.highlighted) 19.sp else 17.sp,
                    lineHeight = if (entry.highlighted) 24.sp else 22.sp,
                ),
                color = titleColor,
            )
            if (entry.subtitle != null) {
                Text(
                    text = entry.subtitle,
                    style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 19.sp),
                    color = subtitleColor,
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = subtitleColor,
            modifier = Modifier.size(20.dp),
        )
    }
}
