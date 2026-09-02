package com.wafflehq.base.ui.theme.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wafflehq.base.R
import com.wafflehq.base.ui.theme.AppSpacing
import com.wafflehq.base.ui.theme.AppTheme

private enum class MockBadge { None, Dot, Num }

private data class MockTab(
    val icon: ImageVector,
    val labelRes: Int,
    val active: Boolean = false,
    val badge: MockBadge = MockBadge.None,
)

@Composable
fun Section22AppHeader() = Section(R.string.sc_s22_title, R.string.sc_s22_desc) {
    val menu = Icons.Outlined.Menu
    val home = Icons.Outlined.Home
    val gear = Icons.Outlined.Settings
    val list = Icons.AutoMirrored.Outlined.FormatListBulleted
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        PhoneMock(R.string.sc_ah_cap_a, "22a") {
            MockBar(
                listOf(
                    MockTab(list, R.string.sc_ah_list),
                    MockTab(home, R.string.header_home, active = true),
                    MockTab(gear, R.string.label_settings),
                ),
                inspectCode = "22a.1",
            )
        }
        PhoneMock(R.string.sc_ah_cap_b, "22b") {
            MockBar(
                listOf(
                    MockTab(menu, R.string.header_menu),
                    MockTab(home, R.string.header_home, active = true),
                    MockTab(gear, R.string.label_settings),
                ),
                inspectCode = "22b.1",
            )
        }
        PhoneMock(R.string.sc_ah_cap_default, "22c") {
            MockBar(
                listOf(
                    MockTab(menu, R.string.header_menu),
                    MockTab(home, R.string.header_home),
                    MockTab(gear, R.string.label_settings),
                ),
                inspectCode = "22c.1",
            )
        }
        PhoneMock(R.string.sc_ah_cap_elevated, "22d") {
            MockBar(
                listOf(
                    MockTab(menu, R.string.header_menu),
                    MockTab(home, R.string.header_home),
                    MockTab(gear, R.string.label_settings),
                ),
                elevated = true,
                inspectCode = "22d.1",
            )
        }
        PhoneMock(R.string.sc_ah_cap_badge, "22e") {
            MockBar(
                listOf(
                    MockTab(menu, R.string.header_menu, badge = MockBadge.Num),
                    MockTab(home, R.string.header_home, active = true),
                    MockTab(gear, R.string.label_settings, badge = MockBadge.Dot),
                ),
                inspectCode = "22e.1",
            )
        }
        PhoneMock(R.string.sc_ah_cap_drawer, "22f", drawer = true) {
            MockBar(
                listOf(
                    MockTab(menu, R.string.header_menu, active = true),
                    MockTab(home, R.string.header_home),
                    MockTab(gear, R.string.label_settings),
                ),
                elevated = true,
                inspectCode = "22f.1",
            )
        }
    }
}

@Composable
private fun PhoneMock(capRes: Int, groupCode: String, drawer: Boolean = false, bar: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Subhead(stringResource(capRes))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(radiusL)
                .border(1.dp, AppTheme.colors.outline, radiusL)
                .background(AppTheme.colors.background)
                .inspectTap(groupCode),
        ) {
            Column {
                bar()
                MockBody()
            }
            if (drawer) MockDrawerOverlay()
        }
    }
}

@Composable
private fun MockBar(tabs: List<MockTab>, elevated: Boolean = false, inspectCode: String? = null) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (elevated) Modifier.shadow(4.dp) else Modifier)
            .background(colors.surface)
            .then(if (inspectCode != null) Modifier.inspectId(inspectCode) else Modifier),
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
            tabs.forEach { MockTabCell(Modifier.weight(1f), it) }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.outline))
    }
}

@Composable
private fun MockTabCell(modifier: Modifier, tab: MockTab) {
    val colors = AppTheme.colors
    val bg = if (tab.active) colors.secondary.container else Color.Transparent
    val fg = if (tab.active) colors.secondary.onContainer else colors.onSurface
    val labelColor = if (tab.active) colors.secondary.onContainer else colors.onSurfaceVariant
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(bg)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(tab.icon, null, tint = fg, modifier = Modifier.size(20.dp))
            when (tab.badge) {
                MockBadge.Dot -> Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.error.accent)
                        .border(1.5.dp, colors.surface, CircleShape),
                )
                MockBadge.Num -> Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 9.dp, y = (-6).dp)
                        .clip(pill)
                        .background(colors.error.accent)
                        .border(1.5.dp, colors.surface, pill)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                ) {
                    Text("3", style = mono.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = colors.error.onAccent)
                }
                MockBadge.None -> Unit
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(tab.labelRes),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.sp),
            color = labelColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun MockBody() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background)
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Box(Modifier.fillMaxWidth().height(11.dp).clip(pill).background(AppTheme.colors.surfaceVariant))
        Box(Modifier.fillMaxWidth().height(52.dp).clip(radiusM).background(AppTheme.colors.surfaceVariant))
        Box(Modifier.fillMaxWidth(0.55f).height(11.dp).clip(pill).background(AppTheme.colors.surfaceVariant))
    }
}

@Composable
private fun BoxScope.MockDrawerOverlay() {
    Box(modifier = Modifier.matchParentSize().padding(top = 65.dp)) {
        Box(Modifier.matchParentSize().background(AppTheme.colors.onSurface.copy(alpha = 0.42f)))
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.74f)
                .background(AppTheme.colors.surface)
                .padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            MockDrawerItem(Icons.Outlined.Home, R.string.header_home, active = true)
            MockDrawerItem(Icons.Outlined.CalendarMonth, R.string.sc_ah_calendar)
            MockDrawerItem(Icons.Outlined.Schedule, R.string.sc_ah_history)
            MockDrawerItem(Icons.Outlined.Settings, R.string.label_settings)
        }
    }
}

@Composable
private fun MockDrawerItem(icon: ImageVector, labelRes: Int, active: Boolean = false) {
    val colors = AppTheme.colors
    val bg = if (active) colors.secondary.container else Color.Transparent
    val fg = if (active) colors.secondary.onContainer else colors.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(pill)
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(16.dp))
        Text(stringResource(labelRes), style = MaterialTheme.typography.labelMedium, color = fg, maxLines = 1)
    }
}
