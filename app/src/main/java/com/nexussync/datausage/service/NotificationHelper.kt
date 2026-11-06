package com.nexussync.datausage.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.nexussync.datausage.MainActivity
import com.nexussync.datausage.R
import com.nexussync.datausage.domain.model.PeriodUsage
import com.nexussync.datausage.util.DataFormatter

class NotificationHelper(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun ensureChannelsCreated() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val liveChannel = NotificationChannel(
            CHANNEL_LIVE_TELEMETRY,
            context.getString(R.string.notif_channel_live_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notif_channel_live_desc)
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
        }

        val alertChannel = NotificationChannel(
            CHANNEL_QUOTA_ALERTS,
            context.getString(R.string.notif_channel_alert_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notif_channel_alert_desc)
            setShowBadge(true)
        }

        notificationManager.createNotificationChannels(listOf(liveChannel, alertChannel))
    }

    fun buildLiveTelemetryNotification(
        todayUsage: PeriodUsage,
        dailyLimitBytes: Long,
        showLiveSpeed: Boolean,
        rxBytesPerSec: Long,
        txBytesPerSec: Long
    ): Notification {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_OPEN_APP,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val totalFormatted = DataFormatter.formatBytes(todayUsage.totalBytes).full
        val limitFormatted = DataFormatter.formatBytes(dailyLimitBytes).full
        val quotaPct = DataFormatter.calculatePercentage(todayUsage.totalBytes, dailyLimitBytes)

        val title = if (showLiveSpeed) {
            val dl = DataFormatter.formatSpeed(rxBytesPerSec).full
            val ul = DataFormatter.formatSpeed(txBytesPerSec).full
            "Today: $totalFormatted  •  DL $dl  |  UL $ul"
        } else {
            "Today's Data Usage: $totalFormatted ($quotaPct%)"
        }

        val mobileStr = DataFormatter.formatBytes(todayUsage.mobile.totalBytes).full
        val wifiStr = DataFormatter.formatBytes(todayUsage.wifi.totalBytes).full
        val body = "Mobile $mobileStr  •  Wi-Fi $wifiStr  •  Limit $limitFormatted"

        return NotificationCompat.Builder(context, CHANNEL_LIVE_TELEMETRY)
            .setSmallIcon(R.drawable.ic_stat_data_sync)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setProgress(100, quotaPct.coerceIn(0, 100), false)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    fun postLiveUpdate(notification: Notification) {
        runCatching {
            notificationManager.notify(LIVE_NOTIFICATION_ID, notification)
        }
    }

    fun postQuotaWarningNotification(
        usedBytes: Long,
        dailyLimitBytes: Long,
        thresholdPercent: Int
    ) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_OPEN_ALERT,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val usedText = DataFormatter.formatBytes(usedBytes).full
        val limitText = DataFormatter.formatBytes(dailyLimitBytes).full
        val actualPct = DataFormatter.calculatePercentage(usedBytes, dailyLimitBytes)

        val isExceeded = usedBytes >= dailyLimitBytes
        val title = if (isExceeded) {
            "Daily Data Limit Reached ($actualPct%)"
        } else {
            "Data Usage Alert ($actualPct% of Daily Limit)"
        }
        val message = if (isExceeded) {
            "You have used $usedText today, exceeding your $limitText daily quota."
        } else {
            "You have reached $usedText out of your $limitText daily limit ($thresholdPercent% threshold)."
        }

        val alertNotification = NotificationCompat.Builder(context, CHANNEL_QUOTA_ALERTS)
            .setSmallIcon(R.drawable.ic_stat_data_sync)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .build()

        runCatching {
            notificationManager.notify(ALERT_NOTIFICATION_ID, alertNotification)
        }
    }

    companion object {
        const val CHANNEL_LIVE_TELEMETRY = "data_sync_live_telemetry"
        const val CHANNEL_QUOTA_ALERTS = "data_sync_quota_alerts"
        const val LIVE_NOTIFICATION_ID = 1001
        const val ALERT_NOTIFICATION_ID = 1002
        private const val REQUEST_OPEN_APP = 2001
        private const val REQUEST_OPEN_ALERT = 2002
    }
}
