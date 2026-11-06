package com.nexussync.datausage.ui.screens.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexussync.datausage.di.AppContainer
import com.nexussync.datausage.domain.model.AppDataUsage
import com.nexussync.datausage.domain.model.AppSortOrder
import com.nexussync.datausage.domain.model.NetworkFilter
import com.nexussync.datausage.domain.model.UsageTimeRange
import com.nexussync.datausage.domain.repository.PreferencesRepository
import com.nexussync.datausage.domain.repository.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppsViewModel(
    private val usageRepository: UsageRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AppsUiState(
            hasUsagePermission = usageRepository.hasUsageAccessPermission(),
            includeSystemApps = preferencesRepository.currentPreferences.includeSystemApps
        )
    )
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    fun onScreenResumed() {
        loadApps()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val next = state.copy(searchQuery = query)
            next.copy(filteredApps = applyFilterAndSort(next))
        }
    }

    fun onTimeRangeSelected(range: UsageTimeRange) {
        if (_uiState.value.timeRange == range) return
        _uiState.update { it.copy(timeRange = range) }
        loadApps()
    }

    fun onNetworkFilterSelected(filter: NetworkFilter) {
        _uiState.update { state ->
            val next = state.copy(networkFilter = filter)
            next.copy(filteredApps = applyFilterAndSort(next))
        }
    }

    fun onSortOrderSelected(sort: AppSortOrder) {
        _uiState.update { state ->
            val next = state.copy(sortOrder = sort)
            next.copy(filteredApps = applyFilterAndSort(next))
        }
    }

    fun onToggleSystemApps() {
        val nextInclude = !_uiState.value.includeSystemApps
        preferencesRepository.setIncludeSystemApps(nextInclude)
        _uiState.update { it.copy(includeSystemApps = nextInclude) }
        loadApps()
    }

    fun onSelectApp(app: AppDataUsage?) {
        _uiState.update { it.copy(selectedApp = app) }
    }

    fun loadApps() {
        viewModelScope.launch {
            val hasPerm = usageRepository.hasUsageAccessPermission()
            if (!hasPerm) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasUsagePermission = false,
                        rawApps = emptyList(),
                        filteredApps = emptyList()
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, hasUsagePermission = true) }
            val stateSnap = _uiState.value
            val prefs = preferencesRepository.currentPreferences

            val apps = usageRepository.fetchAppUsageList(
                timeRange = stateSnap.timeRange,
                billingCycleStartDay = prefs.billingCycleStartDay,
                includeSystemApps = stateSnap.includeSystemApps,
                maxApps = 90
            )

            _uiState.update { current ->
                val updated = current.copy(
                    isLoading = false,
                    hasUsagePermission = true,
                    rawApps = apps
                )
                updated.copy(filteredApps = applyFilterAndSort(updated))
            }
        }
    }

    private fun applyFilterAndSort(state: AppsUiState): List<AppDataUsage> {
        val query = state.searchQuery.trim()
        val filtered = state.rawApps.filter { app ->
            val hasTrafficForFilter = app.bytesForFilter(state.networkFilter) > 0L
            val matchesQuery = query.isEmpty() ||
                app.appName.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            hasTrafficForFilter && matchesQuery
        }

        val sorted = when (state.sortOrder) {
            AppSortOrder.TOTAL_DESC -> filtered.sortedByDescending { it.bytesForFilter(state.networkFilter) }
            AppSortOrder.DOWNLOAD_DESC -> filtered.sortedByDescending { it.downloadTotalBytes }
            AppSortOrder.UPLOAD_DESC -> filtered.sortedByDescending { it.uploadTotalBytes }
            AppSortOrder.MOBILE_DESC -> filtered.sortedByDescending { it.mobileTotalBytes }
        }

        val totalForFilter = sorted.sumOf { it.bytesForFilter(state.networkFilter) }.coerceAtLeast(1L)
        return sorted.map { item ->
            item.copy(
                shareFraction = (item.bytesForFilter(state.networkFilter).toDouble() / totalForFilter.toDouble())
                    .toFloat()
                    .coerceIn(0f, 1f)
            )
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AppsViewModel(
                        usageRepository = container.usageRepository,
                        preferencesRepository = container.preferencesRepository
                    ) as T
                }
            }
    }
}
