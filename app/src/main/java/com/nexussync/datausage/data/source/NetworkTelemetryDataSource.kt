package com.nexussync.datausage.data.source

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.os.SystemClock
import com.nexussync.datausage.domain.model.LiveSpeedSnapshot
import com.nexussync.datausage.domain.model.NetworkConnectionInfo
import com.nexussync.datausage.domain.model.NetworkType
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class NetworkTelemetryDataSource(context: Context) {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun getActiveConnectionInfo(): NetworkConnectionInfo {
        val activeNetwork = connectivityManager.activeNetwork
            ?: return NetworkConnectionInfo()
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return NetworkConnectionInfo()

        val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!hasInternet) {
            return NetworkConnectionInfo()
        }

        val networkType = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.OTHER
        }

        val isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val isNotMetered = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        val isVpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)

        return NetworkConnectionInfo(
            type = networkType,
            isConnected = true,
            isValidated = isValidated,
            isMetered = !isNotMetered,
            isVpnActive = isVpn,
            downstreamLinkKbps = caps.linkDownstreamBandwidthKbps.coerceAtLeast(0),
            upstreamLinkKbps = caps.linkUpstreamBandwidthKbps.coerceAtLeast(0)
        )
    }

    fun sampleCurrentRates(
        prevRx: Long,
        prevTx: Long,
        prevElapsedRealtimeMs: Long
    ): RateSample {
        val nowRx = TrafficStats.getTotalRxBytes()
        val nowTx = TrafficStats.getTotalTxBytes()
        val nowMs = SystemClock.elapsedRealtime()

        if (nowRx < 0L || nowTx < 0L || prevRx < 0L || prevTx < 0L || prevElapsedRealtimeMs <= 0L) {
            return RateSample(
                rxBytes = nowRx.coerceAtLeast(0L),
                txBytes = nowTx.coerceAtLeast(0L),
                elapsedRealtimeMs = nowMs,
                rxBytesPerSec = 0L,
                txBytesPerSec = 0L
            )
        }

        val deltaMs = (nowMs - prevElapsedRealtimeMs).coerceAtLeast(250L)
        val deltaRx = (nowRx - prevRx).coerceAtLeast(0L)
        val deltaTx = (nowTx - prevTx).coerceAtLeast(0L)

        val rxPerSec = (deltaRx * 1000L) / deltaMs
        val txPerSec = (deltaTx * 1000L) / deltaMs

        return RateSample(
            rxBytes = nowRx,
            txBytes = nowTx,
            elapsedRealtimeMs = nowMs,
            rxBytesPerSec = rxPerSec,
            txBytesPerSec = txPerSec
        )
    }

    fun observeLiveSpeed(intervalMillis: Long = 1200L): Flow<LiveSpeedSnapshot> = flow {
        val safeInterval = intervalMillis.coerceAtLeast(500L)
        val startElapsedMs = SystemClock.elapsedRealtime()
        val initialRx = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
        val initialTx = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)

        var lastRx = initialRx
        var lastTx = initialTx
        var lastTickMs = startElapsedMs
        var peakRxPerSec = 0L
        var peakTxPerSec = 0L

        val rxHistory = ArrayDeque<Long>(LiveSpeedSnapshot.HISTORY_POINTS)
        val txHistory = ArrayDeque<Long>(LiveSpeedSnapshot.HISTORY_POINTS)
        repeat(LiveSpeedSnapshot.HISTORY_POINTS) {
            rxHistory.addLast(0L)
            txHistory.addLast(0L)
        }

        emit(LiveSpeedSnapshot())

        while (currentCoroutineContext().isActive) {
            delay(safeInterval)

            val sample = sampleCurrentRates(lastRx, lastTx, lastTickMs)
            lastRx = sample.rxBytes
            lastTx = sample.txBytes
            lastTickMs = sample.elapsedRealtimeMs

            peakRxPerSec = maxOf(peakRxPerSec, sample.rxBytesPerSec)
            peakTxPerSec = maxOf(peakTxPerSec, sample.txBytesPerSec)

            if (rxHistory.size >= LiveSpeedSnapshot.HISTORY_POINTS) rxHistory.removeFirst()
            if (txHistory.size >= LiveSpeedSnapshot.HISTORY_POINTS) txHistory.removeFirst()
            rxHistory.addLast(sample.rxBytesPerSec)
            txHistory.addLast(sample.txBytesPerSec)

            val sessionRx = (sample.rxBytes - initialRx).coerceAtLeast(0L)
            val sessionTx = (sample.txBytes - initialTx).coerceAtLeast(0L)
            val durationSec = ((sample.elapsedRealtimeMs - startElapsedMs) / 1000L).coerceAtLeast(0L)

            emit(
                LiveSpeedSnapshot(
                    downloadBytesPerSec = sample.rxBytesPerSec,
                    uploadBytesPerSec = sample.txBytesPerSec,
                    peakDownloadBytesPerSec = peakRxPerSec,
                    peakUploadBytesPerSec = peakTxPerSec,
                    sessionRxBytes = sessionRx,
                    sessionTxBytes = sessionTx,
                    sessionDurationSec = durationSec,
                    recentDownloadHistory = rxHistory.toList(),
                    recentUploadHistory = txHistory.toList()
                )
            )
        }
    }

    data class RateSample(
        val rxBytes: Long,
        val txBytes: Long,
        val elapsedRealtimeMs: Long,
        val rxBytesPerSec: Long,
        val txBytesPerSec: Long
    )
}
