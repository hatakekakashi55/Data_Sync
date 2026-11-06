package com.nexussync.datausage.data.local

import android.content.Context
import android.content.SharedPreferences
import com.nexussync.datausage.domain.model.ThemeMode
import com.nexussync.datausage.domain.model.UserPreferences
import com.nexussync.datausage.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepositoryImpl(context: Context) : PreferencesRepository {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _preferencesFlow = MutableStateFlow(readFromDisk())
    override val preferencesFlow: StateFlow<UserPreferences> = _preferencesFlow.asStateFlow()

    override val currentPreferences: UserPreferences
        get() = _preferencesFlow.value

    override fun setDailyLimitGb(limitGb: Float) {
        val clamped = limitGb.coerceIn(0.5f, 50.0f)
        prefs.edit().putFloat(KEY_DAILY_LIMIT_GB, clamped).apply()
        refreshState()
    }

    override fun setMonthlyLimitGb(limitGb: Float) {
        val clamped = limitGb.coerceIn(5.0f, 1000.0f)
        prefs.edit().putFloat(KEY_MONTHLY_LIMIT_GB, clamped).apply()
        refreshState()
    }

    override fun setWarningThresholdPercent(percent: Int) {
        val clamped = percent.coerceIn(50, 100)
        prefs.edit().putInt(KEY_WARNING_PERCENT, clamped).apply()
        refreshState()
    }

    override fun setBillingCycleStartDay(dayOfMonth: Int) {
        val clamped = dayOfMonth.coerceIn(1, 28)
        prefs.edit().putInt(KEY_BILLING_START_DAY, clamped).apply()
        refreshState()
    }

    override fun setMonitoringActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_MONITORING_ACTIVE, active).apply()
        refreshState()
    }

    override fun setPersistentNotification(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PERSISTENT_NOTIF, enabled).apply()
        refreshState()
    }

    override fun setShowLiveSpeedInNotification(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_SPEED_NOTIF, enabled).apply()
        refreshState()
    }

    override fun setQuotaAlertsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QUOTA_ALERTS, enabled).apply()
        refreshState()
    }

    override fun setAutoStartOnBoot(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_START, enabled).apply()
        refreshState()
    }

    override fun setIncludeSystemApps(include: Boolean) {
        prefs.edit().putBoolean(KEY_INCLUDE_SYSTEM_APPS, include).apply()
        refreshState()
    }

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        refreshState()
    }

    override fun getLastQuotaAlertEpochDay(): Long {
        return prefs.getLong(KEY_LAST_ALERT_EPOCH_DAY, -1L)
    }

    override fun setLastQuotaAlertEpochDay(epochDay: Long) {
        prefs.edit().putLong(KEY_LAST_ALERT_EPOCH_DAY, epochDay).apply()
    }

    private fun refreshState() {
        _preferencesFlow.value = readFromDisk()
    }

    private fun readFromDisk(): UserPreferences {
        val rawTheme = prefs.getString(KEY_THEME_MODE, ThemeMode.MIDNIGHT.name)
        val theme = ThemeMode.entries.firstOrNull { it.name == rawTheme } ?: ThemeMode.MIDNIGHT

        return UserPreferences(
            dailyLimitGb = prefs.getFloat(KEY_DAILY_LIMIT_GB, 2.0f).coerceIn(0.5f, 50.0f),
            monthlyLimitGb = prefs.getFloat(KEY_MONTHLY_LIMIT_GB, 60.0f).coerceIn(5.0f, 1000.0f),
            warningThresholdPercent = prefs.getInt(KEY_WARNING_PERCENT, 80).coerceIn(50, 100),
            billingCycleStartDay = prefs.getInt(KEY_BILLING_START_DAY, 1).coerceIn(1, 28),
            monitoringActive = prefs.getBoolean(KEY_MONITORING_ACTIVE, true),
            persistentNotification = prefs.getBoolean(KEY_PERSISTENT_NOTIF, true),
            showLiveSpeedInNotification = prefs.getBoolean(KEY_SHOW_SPEED_NOTIF, true),
            quotaAlertsEnabled = prefs.getBoolean(KEY_QUOTA_ALERTS, true),
            autoStartOnBoot = prefs.getBoolean(KEY_AUTO_START, true),
            includeSystemApps = prefs.getBoolean(KEY_INCLUDE_SYSTEM_APPS, true),
            themeMode = theme
        )
    }

    companion object {
        const val PREFS_NAME = "DataSyncPrefs"
        private const val KEY_DAILY_LIMIT_GB = "dailyLimitGb"
        private const val KEY_MONTHLY_LIMIT_GB = "monthlyLimitGb"
        private const val KEY_WARNING_PERCENT = "warningThresholdPercent"
        private const val KEY_BILLING_START_DAY = "billingCycleStartDay"
        private const val KEY_MONITORING_ACTIVE = "serviceRunning"
        private const val KEY_PERSISTENT_NOTIF = "notifEnabled"
        private const val KEY_SHOW_SPEED_NOTIF = "showSpeedInNotif"
        private const val KEY_QUOTA_ALERTS = "quotaAlertsEnabled"
        private const val KEY_AUTO_START = "autoStart"
        private const val KEY_INCLUDE_SYSTEM_APPS = "includeSystemApps"
        private const val KEY_THEME_MODE = "themeMode"
        private const val KEY_LAST_ALERT_EPOCH_DAY = "lastQuotaAlertEpochDay"
    }
}
