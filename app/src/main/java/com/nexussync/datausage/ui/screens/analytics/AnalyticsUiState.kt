package com.nexussync.datausage.ui.screens.analytics

import com.nexussync.datausage.domain.model.ChartNetworkSeries
import com.nexussync.datausage.domain.model.DailyHistoryAnalytics
import com.nexussync.datausage.domain.model.DataUsageSnapshot
import com.nexussync.datausage.domain.model.UserPreferences

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val hasUsagePermission: Boolean = false,
    val usageSnapshot: DataUsageSnapshot = DataUsageSnapshot(),
    val history: DailyHistoryAnalytics = DailyHistoryAnalytics(),
    val selectedBarIndex: Int = 29,
    val selectedSeries: ChartNetworkSeries = ChartNetworkSeries.COMBINED,
    val preferences: UserPreferences = UserPreferences()
)
