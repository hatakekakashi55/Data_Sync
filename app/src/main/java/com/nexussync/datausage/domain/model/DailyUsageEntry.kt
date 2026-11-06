package com.nexussync.datausage.domain.model

data class DailyUsageEntry(
    val dayStartMillis: Long,
    val shortDateLabel: String,
    val fullDateLabel: String,
    val mobileRxBytes: Long,
    val mobileTxBytes: Long,
    val wifiRxBytes: Long,
    val wifiTxBytes: Long,
    val isToday: Boolean
) {
    val mobileBytes: Long
        get() = mobileRxBytes + mobileTxBytes

    val wifiBytes: Long
        get() = wifiRxBytes + wifiTxBytes

    val downloadBytes: Long
        get() = mobileRxBytes + wifiRxBytes

    val uploadBytes: Long
        get() = mobileTxBytes + wifiTxBytes

    val totalBytes: Long
        get() = mobileBytes + wifiBytes
}

enum class ChartNetworkSeries(val label: String) {
    COMBINED("Combined"),
    MOBILE("Mobile"),
    WIFI("Wi-Fi")
}

data class DailyHistoryAnalytics(
    val entries: List<DailyUsageEntry> = emptyList(),
    val thirtyDayTotalBytes: Long = 0L,
    val thirtyDayMobileBytes: Long = 0L,
    val thirtyDayWifiBytes: Long = 0L,
    val averageDailyBytes: Long = 0L,
    val peakDay: DailyUsageEntry? = null
)
