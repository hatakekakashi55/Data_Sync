package com.nexussync.datausage.domain.model

import androidx.compose.ui.graphics.ImageBitmap

enum class UsageTimeRange(val label: String) {
    TODAY("Today"),
    LAST_7_DAYS("7 Days"),
    BILLING_CYCLE("This Cycle")
}

enum class NetworkFilter(val label: String) {
    ALL("All Networks"),
    MOBILE("Mobile"),
    WIFI("Wi-Fi")
}

enum class AppSortOrder(val label: String) {
    TOTAL_DESC("Total Data"),
    DOWNLOAD_DESC("Downloaded"),
    UPLOAD_DESC("Uploaded"),
    MOBILE_DESC("Mobile Data")
}

data class AppDataUsage(
    val uid: Int,
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isSpecialBucket: Boolean = false,
    val mobileRxBytes: Long,
    val mobileTxBytes: Long,
    val wifiRxBytes: Long,
    val wifiTxBytes: Long,
    val shareFraction: Float = 0f,
    val iconBitmap: ImageBitmap? = null
) {
    val mobileTotalBytes: Long
        get() = mobileRxBytes + mobileTxBytes

    val wifiTotalBytes: Long
        get() = wifiRxBytes + wifiTxBytes

    val downloadTotalBytes: Long
        get() = mobileRxBytes + wifiRxBytes

    val uploadTotalBytes: Long
        get() = mobileTxBytes + wifiTxBytes

    val totalBytes: Long
        get() = mobileTotalBytes + wifiTotalBytes

    fun bytesForFilter(filter: NetworkFilter): Long = when (filter) {
        NetworkFilter.ALL -> totalBytes
        NetworkFilter.MOBILE -> mobileTotalBytes
        NetworkFilter.WIFI -> wifiTotalBytes
    }
}
