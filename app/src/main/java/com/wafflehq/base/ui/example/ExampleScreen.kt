package com.wafflehq.base.ui.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wafflehq.base.R
import com.wafflehq.base.ui.components.AppHeaderScaffold
import com.wafflehq.base.ui.components.HeaderItem
import com.wafflehq.lib.uicore.components.AppSectionCard
import com.wafflehq.lib.uicore.theme.AppSpacing

@Composable
fun ExampleScreen(
    titleRes: Int,
    onOpenMenu: () -> Unit,
    onNavigateHome: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    AppHeaderScaffold(
        title = stringResource(titleRes),
        activeItem = HeaderItem.None,
        onOpenMenu = onOpenMenu,
        onNavigateHome = onNavigateHome,
        onOpenSettings = onOpenSettings,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            item {
                Text(
                    text = stringResource(R.string.example_lead),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item { LoremCard(R.string.example_section_a, R.string.lorem_1) }
            item { LoremCard(R.string.example_section_b, R.string.lorem_2) }
            item { LoremCard(R.string.example_section_c, R.string.lorem_3) }
        }
    }
}

@Composable
private fun LoremCard(titleRes: Int, bodyRes: Int) {
    AppSectionCard(title = stringResource(titleRes)) {
        Text(
            text = stringResource(bodyRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
