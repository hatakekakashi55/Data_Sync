package com.nexussync.datausage.data.repository

import com.nexussync.datausage.data.source.AppUsageDataSource
import com.nexussync.datausage.data.source.NetworkStatsDataSource
import com.nexussync.datausage.data.source.NetworkTelemetryDataSource
import com.nexussync.datausage.data.source.PermissionChecker
import com.nexussync.datausage.domain.model.AppDataUsage
import com.nexussync.datausage.domain.model.DailyHistoryAnalytics
import com.nexussync.datausage.domain.model.DataSaverState
import com.nexussync.datausage.domain.model.DataUsageSnapshot
import com.nexussync.datausage.domain.model.LiveSpeedSnapshot
import com.nexussync.datausage.domain.model.NetworkConnectionInfo
import com.nexussync.datausage.domain.model.UsageTimeRange
import com.nexussync.datausage.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow

class UsageRepositoryImpl(
    private val permissionChecker: PermissionChecker,
    private val telemetryDataSource: NetworkTelemetryDataSource,
    private val networkStatsDataSource: NetworkStatsDataSource,
    private val appUsageDataSource: AppUsageDataSource
) : UsageRepository {

    override fun hasUsageAccessPermission(): Boolean =
        permissionChecker.hasUsageAccessPermission()

    override fun hasNotificationPermission(): Boolean =
        permissionChecker.hasNotificationPermission()

    override fun getDataSaverState(): DataSaverState =
        permissionChecker.getDataSaverState()

    override fun getActiveConnectionInfo(): NetworkConnectionInfo =
        telemetryDataSource.getActiveConnectionInfo()

    override fun observeLiveSpeed(intervalMillis: Long): Flow<LiveSpeedSnapshot> =
        telemetryDataSource.observeLiveSpeed(intervalMillis)

    override suspend fun fetchUsageSnapshot(billingCycleStartDay: Int): DataUsageSnapshot {
        if (!permissionChecker.hasUsageAccessPermission()) {
            return DataUsageSnapshot()
        }
        return networkStatsDataSource.fetchUsageSnapshot(billingCycleStartDay)
    }

    override suspend fun fetchThirtyDayHistory(): DailyHistoryAnalytics {
        if (!permissionChecker.hasUsageAccessPermission()) {
            return DailyHistoryAnalytics()
        }
        return networkStatsDataSource.fetchThirtyDayHistory(days = 30)
    }

    override suspend fun fetchAppUsageList(
        timeRange: UsageTimeRange,
        billingCycleStartDay: Int,
        includeSystemApps: Boolean,
        maxApps: Int
    ): List<AppDataUsage> {
        if (!permissionChecker.hasUsageAccessPermission()) {
            return emptyList()
        }
        return appUsageDataSource.fetchAppUsage(
            timeRange = timeRange,
            billingCycleStartDay = billingCycleStartDay,
            includeSystemApps = includeSystemApps,
            maxApps = maxApps
        )
    }
}
