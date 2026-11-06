package com.nexussync.datausage.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexussync.datausage.domain.model.ChartNetworkSeries
import com.nexussync.datausage.ui.components.DistributionRingRow
import com.nexussync.datausage.ui.components.PermissionBanner
import com.nexussync.datausage.ui.components.SectionHeader
import com.nexussync.datausage.ui.components.SurfaceCard
import com.nexussync.datausage.ui.components.UsageBarChart
import com.nexussync.datausage.ui.icons.SystemIcons
import com.nexussync.datausage.ui.theme.DataSyncTheme
import com.nexussync.datausage.util.DataFormatter
import com.nexussync.datausage.util.SystemSettingsNavigator

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    contentPadding: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DataSyncTheme.colors
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val today = uiState.usageSnapshot.today
    val currentCycle = uiState.usageSnapshot.currentCycle
    val previousCycle = uiState.usageSnapshot.previousCycle

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ANALYTICS & HISTORY",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = "30-day breakdown & billing cycle intelligence",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
            IconButton(
                onClick = viewModel::loadAnalytics,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = SystemIcons.Refresh,
                    contentDescription = "Refresh analytics",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!uiState.hasUsagePermission) {
            PermissionBanner(
                title = "Usage Access Required",
                description = "Historical 30-day charts and cycle analytics require Usage Access permission.",
                actionLabel = "Grant",
                onActionClick = { SystemSettingsNavigator.openUsageAccessSettings(context) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 30-Day KPI Summary Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiMetricTile(
                label = "30-DAY TOTAL",
                value = DataFormatter.formatBytes(uiState.history.thirtyDayTotalBytes).full,
                modifier = Modifier.weight(1f)
            )
            KpiMetricTile(
                label = "DAILY AVG",
                value = DataFormatter.formatBytes(uiState.history.averageDailyBytes).full,
                modifier = Modifier.weight(1f)
            )
            KpiMetricTile(
                label = "PEAK DAY",
                value = DataFormatter.formatBytes(uiState.history.peakDay?.totalBytes ?: 0L).full,
                subtitle = uiState.history.peakDay?.shortDateLabel,
                modifier = Modifier.weight(1f)
            )
        }

        // 30-Day Interactive Bar Chart Card
        SectionHeader(title = "Daily Breakdown (Last 30 Days)")
        SurfaceCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChartNetworkSeries.entries.forEach { series ->
                    val selected = uiState.selectedSeries == series
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectSeries(series) },
                        label = {
                            Text(
                                text = series.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.accentContainer,
                            selectedLabelColor = colors.accent,
                            containerColor = colors.cardElevated,
                            labelColor = colors.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = colors.cardBorder,
                            selectedBorderColor = colors.accent.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (uiState.isLoading && uiState.history.entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colors.accent,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            } else {
                UsageBarChart(
                    entries = uiState.history.entries,
                    selectedIndex = uiState.selectedBarIndex,
                    series = uiState.selectedSeries,
                    onSelectIndex = viewModel::selectBarIndex
                )
            }
        }

        // Today's Distribution (Donut Ring Visualizations)
        SectionHeader(title = "Today's Traffic Distribution")
        SurfaceCard {
            Text(
                text = "INTERFACE SPLIT",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(14.dp))
            DistributionRingRow(
                primaryLabel = "Mobile Data",
                primaryBytes = today.mobile.totalBytes,
                primaryColor = colors.mobileAccent,
                secondaryLabel = "Wi-Fi Data",
                secondaryBytes = today.wifi.totalBytes,
                secondaryColor = colors.wifiAccent
            )

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "DIRECTIONAL SPLIT",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(14.dp))
            DistributionRingRow(
                primaryLabel = "Downloaded",
                primaryBytes = today.downloadBytes,
                primaryColor = colors.downloadAccent,
                secondaryLabel = "Uploaded",
                secondaryBytes = today.uploadBytes,
                secondaryColor = colors.uploadAccent
            )
        }

        // Current Cycle vs Previous Cycle / Month Comparison
        SectionHeader(title = "Cycle Comparison")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CycleComparisonCard(
                title = "CURRENT CYCLE",
                totalBytes = currentCycle.totalBytes,
                mobileBytes = currentCycle.mobile.totalBytes,
                wifiBytes = currentCycle.wifi.totalBytes,
                highlight = true,
                modifier = Modifier.weight(1f)
            )
            CycleComparisonCard(
                title = "PREVIOUS CYCLE",
                totalBytes = previousCycle.totalBytes,
                mobileBytes = previousCycle.mobile.totalBytes,
                wifiBytes = previousCycle.wifi.totalBytes,
                highlight = false,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun KpiMetricTile(
    label: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = DataSyncTheme.colors
    SurfaceCard(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
        cornerRadius = 16.dp
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textMuted
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.accent
            )
        }
    }
}

@Composable
private fun CycleComparisonCard(
    title: String,
    totalBytes: Long,
    mobileBytes: Long,
    wifiBytes: Long,
    highlight: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = DataSyncTheme.colors
    val totalFormatted = DataFormatter.formatBytes(totalBytes)
    SurfaceCard(
        modifier = modifier,
        borderColor = if (highlight) colors.accent.copy(alpha = 0.4f) else colors.cardBorder,
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (highlight) colors.accent else colors.textMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = totalFormatted.value,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary
            )
            Text(
                text = " ${totalFormatted.unit}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = colors.divider, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Mobile: ${DataFormatter.formatBytes(mobileBytes).full}",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Wi-Fi: ${DataFormatter.formatBytes(wifiBytes).full}",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
    }
}
