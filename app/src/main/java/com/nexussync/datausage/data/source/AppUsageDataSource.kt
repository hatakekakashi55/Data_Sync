@file:Suppress("DEPRECATION")

package com.nexussync.datausage.data.source

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Process
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import com.nexussync.datausage.domain.model.AppDataUsage
import com.nexussync.datausage.domain.model.UsageTimeRange
import com.nexussync.datausage.util.BillingCycleCalculator
import com.nexussync.datausage.util.DrawableIconConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class AppUsageDataSource(context: Context) {

    private val appContext = context.applicationContext
    private val networkStatsManager =
        appContext.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    private val packageManager: PackageManager = appContext.packageManager

    private val iconCache = LruCache<String, ImageBitmap>(64)

    private class MutableUidAccumulator {
        var mobileRx: Long = 0L
        var mobileTx: Long = 0L
        var wifiRx: Long = 0L
        var wifiTx: Long = 0L

        val total: Long
            get() = mobileRx + mobileTx + wifiRx + wifiTx
    }

    suspend fun fetchAppUsage(
        timeRange: UsageTimeRange,
        billingCycleStartDay: Int,
        includeSystemApps: Boolean,
        maxApps: Int = 80
    ): List<AppDataUsage> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val startMillis = when (timeRange) {
            UsageTimeRange.TODAY -> BillingCycleCalculator.startOfTodayMillis(now)
            UsageTimeRange.LAST_7_DAYS -> Calendar.getInstance().apply {
                timeInMillis = BillingCycleCalculator.startOfTodayMillis(now)
                add(Calendar.DAY_OF_YEAR, -6)
            }.timeInMillis
            UsageTimeRange.BILLING_CYCLE ->
                BillingCycleCalculator.computeWindow(billingCycleStartDay, now).startMillis
        }

        val uidMap = HashMap<Int, MutableUidAccumulator>()
        accumulateNetworkSummary(ConnectivityManager.TYPE_MOBILE, startMillis, now, isMobile = true, uidMap)
        accumulateNetworkSummary(ConnectivityManager.TYPE_WIFI, startMillis, now, isMobile = false, uidMap)

        if (uidMap.isEmpty()) return@withContext emptyList()

        val sortedEntries = uidMap.entries
            .filter { it.value.total > 0L }
            .sortedByDescending { it.value.total }

        val resolvedApps = ArrayList<AppDataUsage>(minOf(sortedEntries.size, maxApps))

        for ((uid, acc) in sortedEntries) {
            if (resolvedApps.size >= maxApps) break

            val resolved = resolveUidMetadata(uid, loadIcon = resolvedApps.size < 45) ?: continue
            if (!includeSystemApps && resolved.isSystemApp) continue

            resolvedApps.add(
                AppDataUsage(
                    uid = uid,
                    packageName = resolved.packageName,
                    appName = resolved.appName,
                    isSystemApp = resolved.isSystemApp,
                    isSpecialBucket = resolved.isSpecialBucket,
                    mobileRxBytes = acc.mobileRx,
                    mobileTxBytes = acc.mobileTx,
                    wifiRxBytes = acc.wifiRx,
                    wifiTxBytes = acc.wifiTx,
                    iconBitmap = resolved.iconBitmap
                )
            )
        }

        val grandTotal = resolvedApps.sumOf { it.totalBytes }.coerceAtLeast(1L)
        resolvedApps.map { item ->
            item.copy(
                shareFraction = (item.totalBytes.toDouble() / grandTotal.toDouble())
                    .toFloat()
                    .coerceIn(0f, 1f)
            )
        }
    }

    private fun accumulateNetworkSummary(
        networkType: Int,
        startMillis: Long,
        endMillis: Long,
        isMobile: Boolean,
        uidMap: MutableMap<Int, MutableUidAccumulator>
    ) {
        var stats: NetworkStats? = null
        try {
            stats = networkStatsManager.querySummary(networkType, null, startMillis, endMillis)
            if (stats == null) return
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                val rx = bucket.rxBytes.coerceAtLeast(0L)
                val tx = bucket.txBytes.coerceAtLeast(0L)
                if (rx == 0L && tx == 0L) continue

                val acc = uidMap.getOrPut(bucket.uid) { MutableUidAccumulator() }
                if (isMobile) {
                    acc.mobileRx += rx
                    acc.mobileTx += tx
                } else {
                    acc.wifiRx += rx
                    acc.wifiTx += tx
                }
            }
        } catch (_: Exception) {
            // Gracefully ignore missing permission or transient binder exceptions
        } finally {
            runCatching { stats?.close() }
        }
    }

    private data class ResolvedAppMeta(
        val packageName: String,
        val appName: String,
        val isSystemApp: Boolean,
        val isSpecialBucket: Boolean,
        val iconBitmap: ImageBitmap?
    )

    private fun resolveUidMetadata(uid: Int, loadIcon: Boolean): ResolvedAppMeta? {
        when (uid) {
            NetworkStats.Bucket.UID_TETHERING -> return ResolvedAppMeta(
                packageName = "android.net.tethering",
                appName = "Hotspot & Tethering",
                isSystemApp = true,
                isSpecialBucket = true,
                iconBitmap = null
            )
            NetworkStats.Bucket.UID_REMOVED -> return ResolvedAppMeta(
                packageName = "android.uid.removed",
                appName = "Uninstalled Applications",
                isSystemApp = true,
                isSpecialBucket = true,
                iconBitmap = null
            )
            Process.SYSTEM_UID -> return ResolvedAppMeta(
                packageName = "android.system.os",
                appName = "Android System OS",
                isSystemApp = true,
                isSpecialBucket = true,
                iconBitmap = null
            )
        }

        val packages = runCatching { packageManager.getPackagesForUid(uid) }.getOrNull()
        val primaryPkg = packages?.firstOrNull() ?: return null

        return runCatching {
            val appInfo = packageManager.getApplicationInfo(primaryPkg, 0)
            val label = packageManager.getApplicationLabel(appInfo).toString().ifBlank { primaryPkg }
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0

            val iconBitmap = if (loadIcon) {
                iconCache.get(primaryPkg) ?: runCatching {
                    val drawable = packageManager.getApplicationIcon(appInfo)
                    DrawableIconConverter.toImageBitmap(drawable)?.also { bmp ->
                        iconCache.put(primaryPkg, bmp)
                    }
                }.getOrNull()
            } else {
                iconCache.get(primaryPkg)
            }

            ResolvedAppMeta(
                packageName = primaryPkg,
                appName = label,
                isSystemApp = isSystem,
                isSpecialBucket = false,
                iconBitmap = iconBitmap
            )
        }.getOrNull()
    }
}
