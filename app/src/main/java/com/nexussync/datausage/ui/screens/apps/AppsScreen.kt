package com.nexussync.datausage.ui.screens.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexussync.datausage.domain.model.AppDataUsage
import com.nexussync.datausage.domain.model.NetworkFilter
import com.nexussync.datausage.domain.model.UsageTimeRange
import com.nexussync.datausage.ui.components.IconBadgeBox
import com.nexussync.datausage.ui.components.PermissionBanner
import com.nexussync.datausage.ui.components.SurfaceCard
import com.nexussync.datausage.ui.icons.DataSyncIcons
import com.nexussync.datausage.ui.icons.SystemIcons
import com.nexussync.datausage.ui.theme.DataSyncTheme
import com.nexussync.datausage.util.DataFormatter
import com.nexussync.datausage.util.SystemSettingsNavigator

@Composable
fun AppsScreen(
    viewModel: AppsViewModel,
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

    uiState.selectedApp?.let { selected ->
        AppDetailBottomSheet(
            app = selected,
            onDismiss = { viewModel.onSelectApp(null) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(contentPadding)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "APPLICATION TRAFFIC",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = "${uiState.filteredApps.size} apps • ${DataFormatter.formatBytes(uiState.totalFilteredBytes).full} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = viewModel::onToggleSystemApps,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = SystemIcons.Cpu,
                        contentDescription = "Toggle system apps",
                        tint = if (uiState.includeSystemApps) colors.accent else colors.textMuted,
                        modifier = Modifier.size(19.dp)
                    )
                }
                IconButton(
                    onClick = viewModel::loadApps,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = SystemIcons.Refresh,
                        contentDescription = "Refresh app usage",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Field
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = "Search application or package...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = SystemIcons.Search,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = SystemIcons.Close,
                            contentDescription = "Clear search",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                null
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.cardBackground,
                unfocusedContainerColor = colors.cardBackground,
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.cardBorder,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = colors.accent
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Time Range & Network Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UsageTimeRange.entries.forEach { range ->
                val selected = uiState.timeRange == range
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.onTimeRangeSelected(range) },
                    label = {
                        Text(
                            text = range.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.accentContainer,
                        selectedLabelColor = colors.accent,
                        containerColor = colors.cardBackground,
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

            NetworkFilter.entries.forEach { netFilter ->
                val selected = uiState.networkFilter == netFilter
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.onNetworkFilterSelected(netFilter) },
                    label = {
                        Text(
                            text = netFilter.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.cardElevated,
                        selectedLabelColor = colors.textPrimary,
                        containerColor = colors.cardBackground,
                        labelColor = colors.textMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = colors.cardBorder,
                        selectedBorderColor = colors.textSecondary.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!uiState.hasUsagePermission) {
            PermissionBanner(
                title = "Usage Access Required",
                description = "Per-application network consumption requires Android Usage Access permission.",
                actionLabel = "Grant",
                onActionClick = { SystemSettingsNavigator.openUsageAccessSettings(context) }
            )
        } else if (uiState.isLoading && uiState.filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colors.accent,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(30.dp)
                )
            }
        } else if (uiState.filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconBadgeBox(
                        icon = DataSyncIcons.Apps,
                        tint = colors.textMuted,
                        containerColor = colors.cardBackground,
                        size = 52.dp,
                        iconSize = 24.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching application traffic",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try switching the time period or clearing filters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(
                    items = uiState.filteredApps,
                    key = { "${it.uid}_${it.packageName}" }
                ) { app ->
                    AppUsageListItem(
                        app = app,
                        networkFilter = uiState.networkFilter,
                        onClick = { viewModel.onSelectApp(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppUsageListItem(
    app: AppDataUsage,
    networkFilter: NetworkFilter,
    onClick: () -> Unit
) {
    val colors = DataSyncTheme.colors
    val displayedBytes = app.bytesForFilter(networkFilter)
    val barColor = when (networkFilter) {
        NetworkFilter.ALL -> colors.accent
        NetworkFilter.MOBILE -> colors.mobileAccent
        NetworkFilter.WIFI -> colors.wifiAccent
    }

    SurfaceCard(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap,
                    contentDescription = app.appName,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(11.dp))
                )
            } else {
                IconBadgeBox(
                    icon = if (app.isSpecialBucket) SystemIcons.Cpu else DataSyncIcons.Apps,
                    tint = barColor,
                    containerColor = barColor.copy(alpha = 0.14f),
                    size = 42.dp,
                    iconSize = 20.dp,
                    cornerRadius = 11.dp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = DataFormatter.formatBytes(displayedBytes).full,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { app.shareFraction.coerceIn(0.02f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = barColor,
                    trackColor = colors.chartTrack,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DL ${DataFormatter.formatBytes(app.downloadTotalBytes).full}  •  UL ${DataFormatter.formatBytes(app.uploadTotalBytes).full}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                    Text(
                        text = "${(app.shareFraction * 100f).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}
