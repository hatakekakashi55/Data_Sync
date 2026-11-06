package com.nexussync.datausage.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.TrafficStats
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import com.nexussync.datausage.DataSyncApplication
import com.nexussync.datausage.domain.model.PeriodUsage
import com.nexussync.datausage.util.BillingCycleCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DataUsageForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val container = (application as DataSyncApplication).container
        container.notificationHelper.ensureChannelsCreated()

        val prefs = container.preferencesRepository.currentPreferences
        val initialNotification = container.notificationHelper.buildLiveTelemetryNotification(
            todayUsage = PeriodUsage.Zero,
            dailyLimitBytes = prefs.dailyLimitBytes,
            showLiveSpeed = prefs.showLiveSpeedInNotification,
            rxBytesPerSec = 0L,
            txBytesPerSec = 0L
        )
        promoteToForeground(initialNotification)
        startTelemetryLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        if (monitorJob?.isActive != true) {
            startTelemetryLoop()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        monitorJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun promoteToForeground(notification: Notification) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NotificationHelper.LIVE_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NotificationHelper.LIVE_NOTIFICATION_ID, notification)
            }
        }
    }

    private fun startTelemetryLoop() {
        monitorJob?.cancel()
        val container = (application as DataSyncApplication).container

        monitorJob = serviceScope.launch {
            var lastRx = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
            var lastTx = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)
            var lastSampleMs = SystemClock.elapsedRealtime()

            while (isActive) {
                val prefs = container.preferencesRepository.currentPreferences
                if (!prefs.monitoringActive || !prefs.persistentNotification) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    break
                }

                val sample = container.telemetryDataSource.sampleCurrentRates(
                    prevRx = lastRx,
                    prevTx = lastTx,
                    prevElapsedRealtimeMs = lastSampleMs
                )
                lastRx = sample.rxBytes
                lastTx = sample.txBytes
                lastSampleMs = sample.elapsedRealtimeMs

                val todayUsage = if (container.permissionChecker.hasUsageAccessPermission()) {
                    container.networkStatsDataSource.queryTodayUsageBlocking()
                } else {
                    PeriodUsage.Zero
                }

                if (container.permissionChecker.hasNotificationPermission()) {
                    val liveNotif = container.notificationHelper.buildLiveTelemetryNotification(
                        todayUsage = todayUsage,
                        dailyLimitBytes = prefs.dailyLimitBytes,
                        showLiveSpeed = prefs.showLiveSpeedInNotification,
                        rxBytesPerSec = sample.rxBytesPerSec,
                        txBytesPerSec = sample.txBytesPerSec
                    )
                    container.notificationHelper.postLiveUpdate(liveNotif)

                    evaluateDailyQuotaAlert(todayUsage, prefs.dailyLimitBytes, prefs.warningThresholdPercent, prefs.quotaAlertsEnabled)
                }

                delay(UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun evaluateDailyQuotaAlert(
        todayUsage: PeriodUsage,
        dailyLimitBytes: Long,
        thresholdPercent: Int,
        alertsEnabled: Boolean
    ) {
        if (!alertsEnabled || dailyLimitBytes <= 0L) return

        val thresholdBytes = (dailyLimitBytes * thresholdPercent) / 100L
        if (todayUsage.totalBytes < thresholdBytes) return

        val container = (application as DataSyncApplication).container
        val todayEpochDay = BillingCycleCalculator.startOfTodayMillis() / MILLIS_PER_DAY
        val lastAlertDay = container.preferencesRepository.getLastQuotaAlertEpochDay()

        if (lastAlertDay != todayEpochDay) {
            container.preferencesRepository.setLastQuotaAlertEpochDay(todayEpochDay)
            container.notificationHelper.postQuotaWarningNotification(
                usedBytes = todayUsage.totalBytes,
                dailyLimitBytes = dailyLimitBytes,
                thresholdPercent = thresholdPercent
            )
        }
    }

    companion object {
        const val ACTION_STOP_SERVICE = "com.nexussync.datausage.action.STOP_SERVICE"
        private const val UPDATE_INTERVAL_MS = 3000L
        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }
}
