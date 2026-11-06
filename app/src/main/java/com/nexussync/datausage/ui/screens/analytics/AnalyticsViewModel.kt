package com.nexussync.datausage.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexussync.datausage.di.AppContainer
import com.nexussync.datausage.domain.model.ChartNetworkSeries
import com.nexussync.datausage.domain.repository.PreferencesRepository
import com.nexussync.datausage.domain.repository.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val usageRepository: UsageRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AnalyticsUiState(
            hasUsagePermission = usageRepository.hasUsageAccessPermission(),
            preferences = preferencesRepository.currentPreferences
        )
    )
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
                loadAnalytics()
            }
        }
    }

    fun onScreenResumed() {
        loadAnalytics()
    }

    fun selectBarIndex(index: Int) {
        _uiState.update { state ->
            val maxIdx = (state.history.entries.size - 1).coerceAtLeast(0)
            state.copy(selectedBarIndex = index.coerceIn(0, maxIdx))
        }
    }

    fun selectSeries(series: ChartNetworkSeries) {
        _uiState.update { it.copy(selectedSeries = series) }
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            val hasPerm = usageRepository.hasUsageAccessPermission()
            if (!hasPerm) {
                _uiState.update {
                    it.copy(isLoading = false, hasUsagePermission = false)
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, hasUsagePermission = true) }
            val prefs = preferencesRepository.currentPreferences
            val snapshot = usageRepository.fetchUsageSnapshot(prefs.billingCycleStartDay)
            val history = usageRepository.fetchThirtyDayHistory()
            val defaultIdx = (history.entries.size - 1).coerceAtLeast(0)

            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    hasUsagePermission = true,
                    usageSnapshot = snapshot,
                    history = history,
                    selectedBarIndex = if (current.history.entries.isEmpty()) {
                        defaultIdx
                    } else {
                        current.selectedBarIndex.coerceIn(0, defaultIdx)
                    }
                )
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AnalyticsViewModel(
                        usageRepository = container.usageRepository,
                        preferencesRepository = container.preferencesRepository
                    ) as T
                }
            }
    }
}
