package com.nexussync.datausage.domain.model

data class LiveSpeedSnapshot(
    val downloadBytesPerSec: Long = 0L,
    val uploadBytesPerSec: Long = 0L,
    val peakDownloadBytesPerSec: Long = 0L,
    val peakUploadBytesPerSec: Long = 0L,
    val sessionRxBytes: Long = 0L,
    val sessionTxBytes: Long = 0L,
    val sessionDurationSec: Long = 0L,
    val recentDownloadHistory: List<Long> = List(HISTORY_POINTS) { 0L },
    val recentUploadHistory: List<Long> = List(HISTORY_POINTS) { 0L }
) {
    val combinedBytesPerSec: Long
        get() = downloadBytesPerSec + uploadBytesPerSec

    val sessionTotalBytes: Long
        get() = sessionRxBytes + sessionTxBytes

    companion object {
        const val HISTORY_POINTS = 30
    }
}
