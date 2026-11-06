package com.nexussync.datausage.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.nexussync.datausage.ui.icons.DataSyncIcons

sealed class ScreenDestination(val route: String) {
    data object Overview : ScreenDestination("overview")
    data object Analytics : ScreenDestination("analytics")
    data object Apps : ScreenDestination("apps")
    data object Settings : ScreenDestination("settings")
    data object About : ScreenDestination("about")
}

data class BottomNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val PrimaryBottomTabs: List<BottomNavTab> = listOf(
    BottomNavTab(
        route = ScreenDestination.Overview.route,
        label = "Overview",
        icon = DataSyncIcons.Overview
    ),
    BottomNavTab(
        route = ScreenDestination.Analytics.route,
        label = "Analytics",
        icon = DataSyncIcons.Analytics
    ),
    BottomNavTab(
        route = ScreenDestination.Apps.route,
        label = "Apps",
        icon = DataSyncIcons.Apps
    ),
    BottomNavTab(
        route = ScreenDestination.Settings.route,
        label = "Settings",
        icon = DataSyncIcons.Settings
    )
)
