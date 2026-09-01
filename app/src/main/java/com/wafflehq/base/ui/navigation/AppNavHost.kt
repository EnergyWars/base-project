package com.wafflehq.base.ui.navigation

import android.net.Uri
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wafflehq.base.R
import com.wafflehq.base.ui.components.AppDrawer
import com.wafflehq.base.ui.example.ExampleScreen
import com.wafflehq.base.ui.features.FeatureFileDetailScreen
import com.wafflehq.base.ui.features.FeatureFilesListScreen
import com.wafflehq.base.ui.home.HomeScreen
import com.wafflehq.base.ui.settings.DisplaySettingsScreen
import com.wafflehq.base.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val SETTINGS_DISPLAY = "settings_display"
    const val EXAMPLE_1 = "example_1"
    const val EXAMPLE_2 = "example_2"
    const val EXAMPLE_3 = "example_3"
    const val FEATURE_FILES = "feature_files"
    const val FEATURE_FILE_DETAIL = "feature_file_detail/{fileName}"

    fun featureFileDetail(fileName: String): String =
        "feature_file_detail/${Uri.encode(fileName)}"
}

private fun NavController.switchTo(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(Routes.HOME) { saveState = true }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val openMenu: () -> Unit = { scope.launch { drawerState.open() } }
    val navigateHome: () -> Unit = { navController.switchTo(Routes.HOME) }
    val openSettings: () -> Unit = {
        navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                onSelect = { route ->
                    scope.launch { drawerState.close() }
                    if (route == Routes.SETTINGS) {
                        navController.navigate(route) { launchSingleTop = true }
                    } else {
                        navController.switchTo(route)
                    }
                },
            )
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenMenu = openMenu,
                    onNavigateHome = navigateHome,
                    onOpenSettings = openSettings,
                )
            }
            composable(Routes.EXAMPLE_1) {
                ExampleScreen(
                    titleRes = R.string.example_1_title,
                    onOpenMenu = openMenu,
                    onNavigateHome = navigateHome,
                    onOpenSettings = openSettings,
                )
            }
            composable(Routes.EXAMPLE_2) {
                ExampleScreen(
                    titleRes = R.string.example_2_title,
                    onOpenMenu = openMenu,
                    onNavigateHome = navigateHome,
                    onOpenSettings = openSettings,
                )
            }
            composable(Routes.EXAMPLE_3) {
                ExampleScreen(
                    titleRes = R.string.example_3_title,
                    onOpenMenu = openMenu,
                    onNavigateHome = navigateHome,
                    onOpenSettings = openSettings,
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenDisplay = { navController.navigate(Routes.SETTINGS_DISPLAY) },
                    onOpenFeatureFiles = { navController.navigate(Routes.FEATURE_FILES) },
                )
            }
            composable(Routes.SETTINGS_DISPLAY) {
                DisplaySettingsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.FEATURE_FILES) {
                FeatureFilesListScreen(
                    onBack = { navController.popBackStack() },
                    onOpenFile = { fileName ->
                        navController.navigate(Routes.featureFileDetail(fileName))
                    },
                )
            }
            composable(
                route = Routes.FEATURE_FILE_DETAIL,
                arguments = listOf(navArgument("fileName") { type = NavType.StringType }),
            ) {
                FeatureFileDetailScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
