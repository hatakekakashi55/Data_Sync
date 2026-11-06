package com.nexussync.datausage.service

import android.content.Context
import android.content.Intent
import android.os.Build
import com.nexussync.datausage.data.source.PermissionChecker
import com.nexussync.datausage.domain.repository.PreferencesRepository

class ServiceController(
    private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val permissionChecker: PermissionChecker
) {

    fun syncServiceWithPreferences() {
        val prefs = preferencesRepository.currentPreferences
        val shouldRun = prefs.monitoringActive &&
            prefs.persistentNotification &&
            permissionChecker.hasNotificationPermission()

        if (shouldRun) {
            startMonitoringService()
        } else {
            stopMonitoringService()
        }
    }

    fun startMonitoringService() {
        if (!permissionChecker.hasNotificationPermission()) return
        val intent = Intent(context, DataUsageForegroundService::class.java)
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun stopMonitoringService() {
        val intent = Intent(context, DataUsageForegroundService::class.java)
        runCatching {
            context.stopService(intent)
        }
    }
}
