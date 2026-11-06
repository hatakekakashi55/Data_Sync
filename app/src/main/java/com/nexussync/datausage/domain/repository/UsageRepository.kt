package com.nexussync.datausage.domain.repository

import com.nexussync.datausage.domain.model.AppDataUsage
import com.nexussync.datausage.domain.model.DailyHistoryAnalytics
import com.nexussync.datausage.domain.model.DataSaverState
import com.nexussync.datausage.domain.model.DataUsageSnapshot
import com.nexussync.datausage.domain.model.LiveSpeedSnapshot
import com.nexussync.datausage.domain.model.NetworkConnectionInfo
import com.nexussync.datausage.domain.model.UsageTimeRange
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    fun hasUsageAccessPermission(): Boolean
    fun hasNotificationPermission(): Boolean
    fun getDataSaverState(): DataSaverState
    fun getActiveConnectionInfo(): NetworkConnectionInfo

    fun observeLiveSpeed(intervalMillis: Long = 1200L): Flow<LiveSpeedSnapshot>

    suspend fun fetchUsageSnapshot(billingCycleStartDay: Int): DataUsageSnapshot
    suspend fun fetchThirtyDayHistory(): DailyHistoryAnalytics
    suspend fun fetchAppUsageList(
        timeRange: UsageTimeRange,
        billingCycleStartDay: Int,
        includeSystemApps: Boolean,
        maxApps: Int = 80
    ): List<AppDataUsage>
}
