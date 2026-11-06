package com.nexussync.datausage.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexussync.datausage.di.AppContainer
import com.nexussync.datausage.domain.model.DataSaverState
import com.nexussync.datausage.domain.model.ThemeMode
import com.nexussync.datausage.domain.model.UserPreferences
import com.nexussync.datausage.domain.repository.PreferencesRepository
import com.nexussync.datausage.domain.repository.UsageRepository
import com.nexussync.datausage.service.ServiceController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val hasUsagePermission: Boolean = false,
    val hasNotificationPermission: Boolean = true,
    val dataSaverState: DataSaverState = DataSaverState.UNKNOWN,
    val showBillingDayDialog: Boolean = false,
    val showThemeSheet: Boolean = false
)

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val usageRepository: UsageRepository,
    private val serviceController: ServiceController
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            preferences = preferencesRepository.currentPreferences,
            hasUsagePermission = usageRepository.hasUsageAccessPermission(),
            hasNotificationPermission = usageRepository.hasNotificationPermission(),
            dataSaverState = usageRepository.getDataSaverState()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }
    }

    fun onScreenResumed() {
        _uiState.update {
            it.copy(
                hasUsagePermission = usageRepository.hasUsageAccessPermission(),
                hasNotificationPermission = usageRepository.hasNotificationPermission(),
                dataSaverState = usageRepository.getDataSaverState()
            )
        }
        serviceController.syncServiceWithPreferences()
    }

    fun setMonitoringActive(active: Boolean) {
        preferencesRepository.setMonitoringActive(active)
        serviceController.syncServiceWithPreferences()
    }

    fun setPersistentNotification(enabled: Boolean) {
        preferencesRepository.setPersistentNotification(enabled)
        serviceController.syncServiceWithPreferences()
    }

    fun setShowLiveSpeedInNotification(enabled: Boolean) {
        preferencesRepository.setShowLiveSpeedInNotification(enabled)
        serviceController.syncServiceWithPreferences()
    }

    fun setQuotaAlertsEnabled(enabled: Boolean) {
        preferencesRepository.setQuotaAlertsEnabled(enabled)
    }

    fun setAutoStartOnBoot(enabled: Boolean) {
        preferencesRepository.setAutoStartOnBoot(enabled)
    }

    fun setDailyLimitGb(limitGb: Float) {
        val stepped = ((limitGb * 2f).roundToInt() / 2f).coerceIn(0.5f, 20.0f)
        preferencesRepository.setDailyLimitGb(stepped)
    }

    fun setMonthlyLimitGb(limitGb: Float) {
        val stepped = ((limitGb / 5f).roundToInt() * 5f).coerceIn(5f, 500f)
        preferencesRepository.setMonthlyLimitGb(stepped)
    }

    fun setWarningThresholdPercent(percent: Int) {
        preferencesRepository.setWarningThresholdPercent(percent)
    }

    fun setBillingCycleStartDay(day: Int) {
        preferencesRepository.setBillingCycleStartDay(day)
        _uiState.update { it.copy(showBillingDayDialog = false) }
    }

    fun setThemeMode(mode: ThemeMode) {
        preferencesRepository.setThemeMode(mode)
        _uiState.update { it.copy(showThemeSheet = false) }
    }

    fun setShowBillingDayDialog(visible: Boolean) {
        _uiState.update { it.copy(showBillingDayDialog = visible) }
    }

    fun setShowThemeSheet(visible: Boolean) {
        _uiState.update { it.copy(showThemeSheet = visible) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(
                        preferencesRepository = container.preferencesRepository,
                        usageRepository = container.usageRepository,
                        serviceController = container.serviceController
                    ) as T
                }
            }
    }
}
