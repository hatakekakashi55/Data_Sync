package com.nexussync.datausage.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexussync.datausage.di.AppContainer
import com.nexussync.datausage.domain.model.LiveSpeedSnapshot
import com.nexussync.datausage.domain.model.UsageTimeRange
import com.nexussync.datausage.domain.repository.PreferencesRepository
import com.nexussync.datausage.domain.repository.UsageRepository
import com.nexussync.datausage.service.ServiceController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val usageRepository: UsageRepository,
    private val preferencesRepository: PreferencesRepository,
    private val serviceController: ServiceController
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            hasUsagePermission = usageRepository.hasUsageAccessPermission(),
            hasNotificationPermission = usageRepository.hasNotificationPermission(),
            connection = usageRepository.getActiveConnectionInfo(),
            preferences = preferencesRepository.currentPreferences
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var speedJob: Job? = null
    private var periodicSyncJob: Job? = null

    init {
        observePreferences()
        startPeriodicUsageRefresh()
        serviceController.syncServiceWithPreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
                if (prefs.monitoringActive) {
                    ensureLiveSpeedActive()
                } else {
                    stopLiveSpeed()
                }
                refreshUsageData()
            }
        }
    }

    private fun ensureLiveSpeedActive() {
        if (speedJob?.isActive == true) return
        speedJob = viewModelScope.launch {
            usageRepository.observeLiveSpeed(intervalMillis = 1200L).collect { speed ->
                _uiState.update { current ->
                    current.copy(
                        liveSpeed = speed,
                        connection = usageRepository.getActiveConnectionInfo()
                    )
                }
            }
        }
    }

    private fun stopLiveSpeed() {
        speedJob?.cancel()
        speedJob = null
        _uiState.update { it.copy(liveSpeed = LiveSpeedSnapshot()) }
    }

    private fun startPeriodicUsageRefresh() {
        periodicSyncJob?.cancel()
        periodicSyncJob = viewModelScope.launch {
            var tick = 0
            while (isActive) {
                refreshUsageData(fetchAppsToo = (tick % 4 == 0))
                tick++
                delay(4000L)
            }
        }
    }

    fun onScreenResumed() {
        serviceController.syncServiceWithPreferences()
        viewModelScope.launch {
            refreshUsageData(fetchAppsToo = true)
        }
    }

    fun refreshNow() {
        viewModelScope.launch {
            refreshUsageData(fetchAppsToo = true)
        }
    }

    private suspend fun refreshUsageData(fetchAppsToo: Boolean = false) {
        val hasPerm = usageRepository.hasUsageAccessPermission()
        val hasNotif = usageRepository.hasNotificationPermission()
        val conn = usageRepository.getActiveConnectionInfo()
        val prefs = preferencesRepository.currentPreferences

        if (!hasPerm) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    hasUsagePermission = false,
                    hasNotificationPermission = hasNotif,
                    connection = conn
                )
            }
            return
        }

        val snapshot = usageRepository.fetchUsageSnapshot(prefs.billingCycleStartDay)
        val topApps = if (fetchAppsToo || _uiState.value.topAppsToday.isEmpty()) {
            usageRepository.fetchAppUsageList(
                timeRange = UsageTimeRange.TODAY,
                billingCycleStartDay = prefs.billingCycleStartDay,
                includeSystemApps = prefs.includeSystemApps,
                maxApps = 4
            )
        } else {
            _uiState.value.topAppsToday
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                hasUsagePermission = true,
                hasNotificationPermission = hasNotif,
                connection = conn,
                usageSnapshot = snapshot,
                topAppsToday = topApps
            )
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DashboardViewModel(
                        usageRepository = container.usageRepository,
                        preferencesRepository = container.preferencesRepository,
                        serviceController = container.serviceController
                    ) as T
                }
            }
    }
}
