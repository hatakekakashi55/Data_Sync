@file:Suppress("DEPRECATION")

package com.nexussync.datausage.data.source

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import com.nexussync.datausage.domain.model.DailyHistoryAnalytics
import com.nexussync.datausage.domain.model.DailyUsageEntry
import com.nexussync.datausage.domain.model.DataUsageSnapshot
import com.nexussync.datausage.domain.model.PeriodUsage
import com.nexussync.datausage.domain.model.TrafficBreakdown
import com.nexussync.datausage.util.BillingCycleCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NetworkStatsDataSource(context: Context) {

    private val networkStatsManager =
        context.applicationContext.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    suspend fun fetchUsageSnapshot(billingCycleStartDay: Int): DataUsageSnapshot =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val startOfToday = BillingCycleCalculator.startOfTodayMillis(now)
            val cycleWindow = BillingCycleCalculator.computeWindow(billingCycleStartDay, now)

            val todayUsage = queryPeriodUsage(startOfToday, now)
            val currentCycleUsage = queryPeriodUsage(cycleWindow.startMillis, cycleWindow.endMillis)
            val previousCycleUsage = queryPeriodUsage(
                cycleWindow.previousStartMillis,
                cycleWindow.previousEndMillis
            )

            DataUsageSnapshot(
                today = todayUsage,
                currentCycle = currentCycleUsage,
                previousCycle = previousCycleUsage,
                cycleWindow = cycleWindow,
                updatedAtMillis = now
            )
        }

    fun queryTodayUsageBlocking(): PeriodUsage {
        val now = System.currentTimeMillis()
        val startOfToday = BillingCycleCalculator.startOfTodayMillis(now)
        return queryPeriodUsage(startOfToday, now)
    }

    suspend fun fetchThirtyDayHistory(days: Int = 30): DailyHistoryAnalytics =
        withContext(Dispatchers.IO) {
            val safeDays = days.coerceIn(7, 60)
            val shortFmt = SimpleDateFormat("MMM d", Locale.getDefault())
            val fullFmt = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, -(safeDays - 1))
            }

            val entries = ArrayList<DailyUsageEntry>(safeDays)
            var totalMobile = 0L
            var totalWifi = 0L

            for (index in 0 until safeDays) {
                val dayStart = cal.timeInMillis
                val isToday = index == safeDays - 1
                val nextDayCal = (cal.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                val dayEnd = if (isToday) now else (nextDayCal.timeInMillis - 1L)

                val period = queryPeriodUsage(dayStart, dayEnd)
                val dateObj = Date(dayStart)

                val entry = DailyUsageEntry(
                    dayStartMillis = dayStart,
                    shortDateLabel = if (isToday) "Today" else shortFmt.format(dateObj),
                    fullDateLabel = if (isToday) "Today (${shortFmt.format(dateObj)})" else fullFmt.format(dateObj),
                    mobileRxBytes = period.mobile.rxBytes,
                    mobileTxBytes = period.mobile.txBytes,
                    wifiRxBytes = period.wifi.rxBytes,
                    wifiTxBytes = period.wifi.txBytes,
                    isToday = isToday
                )

                totalMobile += entry.mobileBytes
                totalWifi += entry.wifiBytes
                entries.add(entry)

                cal.add(Calendar.DAY_OF_YEAR, 1)
            }

            val combinedTotal = totalMobile + totalWifi
            val avgDaily = if (entries.isNotEmpty()) combinedTotal / entries.size else 0L
            val peak = entries.maxByOrNull { it.totalBytes }?.takeIf { it.totalBytes > 0L }

            DailyHistoryAnalytics(
                entries = entries,
                thirtyDayTotalBytes = combinedTotal,
                thirtyDayMobileBytes = totalMobile,
                thirtyDayWifiBytes = totalWifi,
                averageDailyBytes = avgDaily,
                peakDay = peak
            )
        }

    private fun queryPeriodUsage(startMillis: Long, endMillis: Long): PeriodUsage {
        if (endMillis <= startMillis) return PeriodUsage.Zero

        val mobile = queryDeviceTraffic(ConnectivityManager.TYPE_MOBILE, startMillis, endMillis)
        val wifi = queryDeviceTraffic(ConnectivityManager.TYPE_WIFI, startMillis, endMillis)
        return PeriodUsage(mobile = mobile, wifi = wifi)
    }

    private fun queryDeviceTraffic(
        networkType: Int,
        startMillis: Long,
        endMillis: Long
    ): TrafficBreakdown {
        return runCatching {
            val bucket: NetworkStats.Bucket? = networkStatsManager.querySummaryForDevice(
                networkType,
                null,
                startMillis,
                endMillis
            )
            if (bucket != null) {
                TrafficBreakdown(
                    rxBytes = bucket.rxBytes.coerceAtLeast(0L),
                    txBytes = bucket.txBytes.coerceAtLeast(0L)
                )
            } else {
                TrafficBreakdown.Zero
            }
        }.getOrDefault(TrafficBreakdown.Zero)
    }
}
