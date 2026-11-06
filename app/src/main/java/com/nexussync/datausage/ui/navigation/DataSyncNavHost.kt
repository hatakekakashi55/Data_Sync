package com.nexussync.datausage.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nexussync.datausage.di.AppContainer
import com.nexussync.datausage.ui.screens.about.AboutScreen
import com.nexussync.datausage.ui.screens.analytics.AnalyticsScreen
import com.nexussync.datausage.ui.screens.analytics.AnalyticsViewModel
import com.nexussync.datausage.ui.screens.apps.AppsScreen
import com.nexussync.datausage.ui.screens.apps.AppsViewModel
import com.nexussync.datausage.ui.screens.dashboard.DashboardScreen
import com.nexussync.datausage.ui.screens.dashboard.DashboardViewModel
import com.nexussync.datausage.ui.screens.settings.SettingsScreen
import com.nexussync.datausage.ui.screens.settings.SettingsViewModel
import com.nexussync.datausage.ui.theme.DataSyncTheme

@Composable
fun DataSyncAppRoot(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val colors = DataSyncTheme.colors

    val showBottomBar = PrimaryBottomTabs.any { it.route == currentRoute }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)
                    NavigationBar(
                        containerColor = colors.surface,
                        tonalElevation = 0.dp
                    ) {
                        PrimaryBottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        modifier = Modifier.size(21.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = colors.accent,
                                    selectedTextColor = colors.accent,
                                    indicatorColor = colors.accentContainer,
                                    unselectedIconColor = colors.textMuted,
                                    unselectedTextColor = colors.textMuted
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenDestination.Overview.route,
            enterTransition = { fadeIn(animationSpec = tween(180)) },
            exitTransition = { fadeOut(animationSpec = tween(140)) }
        ) {
            composable(ScreenDestination.Overview.route) {
                val vm: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.factory(container)
                )
                DashboardScreen(
                    viewModel = vm,
                    onOpenAnalytics = { navigateToTab(ScreenDestination.Analytics.route) },
                    onOpenApps = { navigateToTab(ScreenDestination.Apps.route) },
                    contentPadding = innerPadding
                )
            }

            composable(ScreenDestination.Analytics.route) {
                val vm: AnalyticsViewModel = viewModel(
                    factory = AnalyticsViewModel.factory(container)
                )
                AnalyticsScreen(
                    viewModel = vm,
                    contentPadding = innerPadding
                )
            }

            composable(ScreenDestination.Apps.route) {
                val vm: AppsViewModel = viewModel(
                    factory = AppsViewModel.factory(container)
                )
                AppsScreen(
                    viewModel = vm,
                    contentPadding = innerPadding
                )
            }

            composable(ScreenDestination.Settings.route) {
                val vm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.factory(container)
                )
                SettingsScreen(
                    viewModel = vm,
                    onOpenAbout = { navController.navigate(ScreenDestination.About.route) },
                    contentPadding = innerPadding
                )
            }

            composable(ScreenDestination.About.route) {
                AboutScreen(
                    onBack = { navController.popBackStack() },
                    contentPadding = innerPadding
                )
            }
        }
    }
}
