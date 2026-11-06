package com.nexussync.datausage.domain.repository

import com.nexussync.datausage.domain.model.ThemeMode
import com.nexussync.datausage.domain.model.UserPreferences
import kotlinx.coroutines.flow.StateFlow

interface PreferencesRepository {
    val preferencesFlow: StateFlow<UserPreferences>
    val currentPreferences: UserPreferences

    fun setDailyLimitGb(limitGb: Float)
    fun setMonthlyLimitGb(limitGb: Float)
    fun setWarningThresholdPercent(percent: Int)
    fun setBillingCycleStartDay(dayOfMonth: Int)
    fun setMonitoringActive(active: Boolean)
    fun setPersistentNotification(enabled: Boolean)
    fun setShowLiveSpeedInNotification(enabled: Boolean)
    fun setQuotaAlertsEnabled(enabled: Boolean)
    fun setAutoStartOnBoot(enabled: Boolean)
    fun setIncludeSystemApps(include: Boolean)
    fun setThemeMode(mode: ThemeMode)

    fun getLastQuotaAlertEpochDay(): Long
    fun setLastQuotaAlertEpochDay(epochDay: Long)
}
