package com.nexussync.datausage.ui.screens.dashboard

import com.nexussync.datausage.domain.model.AppDataUsage
import com.nexussync.datausage.domain.model.DataUsageSnapshot
import com.nexussync.datausage.domain.model.LiveSpeedSnapshot
import com.nexussync.datausage.domain.model.NetworkConnectionInfo
import com.nexussync.datausage.domain.model.UserPreferences

data class DashboardUiState(
    val isLoading: Boolean = true,
    val hasUsagePermission: Boolean = false,
    val hasNotificationPermission: Boolean = true,
    val connection: NetworkConnectionInfo = NetworkConnectionInfo(),
    val usageSnapshot: DataUsageSnapshot = DataUsageSnapshot(),
    val liveSpeed: LiveSpeedSnapshot = LiveSpeedSnapshot(),
    val topAppsToday: List<AppDataUsage> = emptyList(),
    val preferences: UserPreferences = UserPreferences()
)
