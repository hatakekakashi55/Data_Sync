package com.nexussync.datausage.ui.screens.apps

import com.nexussync.datausage.domain.model.AppDataUsage
import com.nexussync.datausage.domain.model.AppSortOrder
import com.nexussync.datausage.domain.model.NetworkFilter
import com.nexussync.datausage.domain.model.UsageTimeRange

data class AppsUiState(
    val isLoading: Boolean = true,
    val hasUsagePermission: Boolean = false,
    val rawApps: List<AppDataUsage> = emptyList(),
    val filteredApps: List<AppDataUsage> = emptyList(),
    val searchQuery: String = "",
    val timeRange: UsageTimeRange = UsageTimeRange.TODAY,
    val networkFilter: NetworkFilter = NetworkFilter.ALL,
    val sortOrder: AppSortOrder = AppSortOrder.TOTAL_DESC,
    val includeSystemApps: Boolean = true,
    val selectedApp: AppDataUsage? = null
) {
    val totalFilteredBytes: Long
        get() = filteredApps.sumOf { it.bytesForFilter(networkFilter) }
}
