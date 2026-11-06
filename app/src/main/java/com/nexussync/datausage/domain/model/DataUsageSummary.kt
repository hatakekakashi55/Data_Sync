package com.nexussync.datausage.domain.model

data class TrafficBreakdown(
    val rxBytes: Long = 0L,
    val txBytes: Long = 0L
) {
    val totalBytes: Long
        get() = rxBytes + txBytes

    operator fun plus(other: TrafficBreakdown): TrafficBreakdown = TrafficBreakdown(
        rxBytes = rxBytes + other.rxBytes,
        txBytes = txBytes + other.txBytes
    )

    companion object {
        val Zero = TrafficBreakdown(0L, 0L)
    }
}

data class PeriodUsage(
    val mobile: TrafficBreakdown = TrafficBreakdown.Zero,
    val wifi: TrafficBreakdown = TrafficBreakdown.Zero
) {
    val downloadBytes: Long
        get() = mobile.rxBytes + wifi.rxBytes

    val uploadBytes: Long
        get() = mobile.txBytes + wifi.txBytes

    val totalBytes: Long
        get() = mobile.totalBytes + wifi.totalBytes

    companion object {
        val Zero = PeriodUsage()
    }
}

data class BillingCycleWindow(
    val startMillis: Long,
    val endMillis: Long,
    val previousStartMillis: Long,
    val previousEndMillis: Long,
    val daysElapsed: Int,
    val daysRemaining: Int,
    val totalCycleDays: Int
)

data class DataUsageSnapshot(
    val today: PeriodUsage = PeriodUsage.Zero,
    val currentCycle: PeriodUsage = PeriodUsage.Zero,
    val previousCycle: PeriodUsage = PeriodUsage.Zero,
    val cycleWindow: BillingCycleWindow? = null,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
