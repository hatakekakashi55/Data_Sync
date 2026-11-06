package com.nexussync.datausage.di

import android.content.Context
import com.nexussync.datausage.data.local.PreferencesRepositoryImpl
import com.nexussync.datausage.data.repository.UsageRepositoryImpl
import com.nexussync.datausage.data.source.AppUsageDataSource
import com.nexussync.datausage.data.source.NetworkStatsDataSource
import com.nexussync.datausage.data.source.NetworkTelemetryDataSource
import com.nexussync.datausage.data.source.PermissionChecker
import com.nexussync.datausage.domain.repository.PreferencesRepository
import com.nexussync.datausage.domain.repository.UsageRepository
import com.nexussync.datausage.service.NotificationHelper
import com.nexussync.datausage.service.ServiceController

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepositoryImpl(appContext)
    }

    val permissionChecker: PermissionChecker by lazy {
        PermissionChecker(appContext)
    }

    val telemetryDataSource: NetworkTelemetryDataSource by lazy {
        NetworkTelemetryDataSource(appContext)
    }

    val networkStatsDataSource: NetworkStatsDataSource by lazy {
        NetworkStatsDataSource(appContext)
    }

    val appUsageDataSource: AppUsageDataSource by lazy {
        AppUsageDataSource(appContext)
    }

    val usageRepository: UsageRepository by lazy {
        UsageRepositoryImpl(
            permissionChecker = permissionChecker,
            telemetryDataSource = telemetryDataSource,
            networkStatsDataSource = networkStatsDataSource,
            appUsageDataSource = appUsageDataSource
        )
    }

    val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(appContext)
    }

    val serviceController: ServiceController by lazy {
        ServiceController(
            context = appContext,
            preferencesRepository = preferencesRepository,
            permissionChecker = permissionChecker
        )
    }
}
