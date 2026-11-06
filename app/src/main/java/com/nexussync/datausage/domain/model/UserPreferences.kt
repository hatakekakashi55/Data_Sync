package com.nexussync.datausage.domain.model

enum class ThemeMode(val label: String, val subtitle: String) {
    MIDNIGHT("Midnight Slate", "Signature deep slate & electric teal"),
    AMOLED("Pure AMOLED", "True black surface for OLED displays"),
    LIGHT("Clean Daylight", "High-contrast light workspace"),
    SYSTEM("System Default", "Follow Android device appearance")
}

data class UserPreferences(
    val dailyLimitGb: Float = 2.0f,
    val monthlyLimitGb: Float = 60.0f,
    val warningThresholdPercent: Int = 80,
    val billingCycleStartDay: Int = 1,
    val monitoringActive: Boolean = true,
    val persistentNotification: Boolean = true,
    val showLiveSpeedInNotification: Boolean = true,
    val quotaAlertsEnabled: Boolean = true,
    val autoStartOnBoot: Boolean = true,
    val includeSystemApps: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.MIDNIGHT
) {
    val dailyLimitBytes: Long
        get() = (dailyLimitGb.toDouble() * BYTES_PER_GB).toLong()

    val monthlyLimitBytes: Long
        get() = (monthlyLimitGb.toDouble() * BYTES_PER_GB).toLong()

    companion object {
        const val BYTES_PER_GB = 1024.0 * 1024.0 * 1024.0
    }
}
