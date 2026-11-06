package com.nexussync.datausage.ui.screens.dashboard

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexussync.datausage.domain.model.AppDataUsage
import com.nexussync.datausage.ui.components.ConnectionStatusBadge
import com.nexussync.datausage.ui.components.IconBadgeBox
import com.nexussync.datausage.ui.components.LiveSpeedTelemetryCard
import com.nexussync.datausage.ui.components.PermissionBanner
import com.nexussync.datausage.ui.components.QuotaArcGauge
import com.nexussync.datausage.ui.components.SectionHeader
import com.nexussync.datausage.ui.components.SurfaceCard
import com.nexussync.datausage.ui.icons.DataSyncIcons
import com.nexussync.datausage.ui.icons.SystemIcons
import com.nexussync.datausage.ui.theme.DataSyncTheme
import com.nexussync.datausage.util.DataFormatter
import com.nexussync.datausage.util.SystemSettingsNavigator

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenAnalytics: () -> Unit,
    onOpenApps: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = DataSyncTheme.colors
    val lifecycleOwner = LocalLifecycleOwner.current

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.onScreenResumed()
    }

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
    val dailyLimitBytes = uiState.preferences.dailyLimitBytes
    val monthlyLimitBytes = uiState.preferences.monthlyLimitBytes
    val currentCycle = uiState.usageSnapshot.currentCycle
    val cycleWindow = uiState.usageSnapshot.cycleWindow

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Top Brand & Connection Telemetry Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadgeBox(
                    icon = DataSyncIcons.DataSyncMark,
                    tint = colors.accent,
                    containerColor = colors.accentContainer,
                    size = 38.dp,
                    iconSize = 20.dp,
                    cornerRadius = 11.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "DATA SYNC",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (uiState.preferences.monitoringActive) {
                            "Telemetry Active"
                        } else {
                            "Monitoring Paused"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                ConnectionStatusBadge(connection = uiState.connection)
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = viewModel::refreshNow,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = SystemIcons.Refresh,
                        contentDescription = "Refresh telemetry",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Permission Banners
        if (!uiState.hasUsagePermission) {
            PermissionBanner(
                title = "Usage Access Required",
                description = "Grant Usage Access so Data Sync can read accurate cellular, Wi-Fi, and per-app consumption.",
                actionLabel = "Grant",
                onActionClick = { SystemSettingsNavigator.openUsageAccessSettings(context) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        } else if (!uiState.hasNotificationPermission &&
            uiState.preferences.persistentNotification &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            PermissionBanner(
                title = "Enable Status Bar Telemetry",
                description = "Allow notifications to display live throughput and daily quota status in your status bar.",
                actionLabel = "Allow",
                onActionClick = {
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Hero Quota Gauge Card
        SurfaceCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuotaArcGauge(
                    usedBytes = today.totalBytes,
                    limitBytes = dailyLimitBytes,
                    warningThresholdPercent = uiState.preferences.warningThresholdPercent
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.divider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HeroSubStat(
                        label = "DOWNLOADED",
                        value = DataFormatter.formatBytes(today.downloadBytes).full,
                        icon = DataSyncIcons.ArrowDown,
                        tint = colors.downloadAccent
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(colors.divider)
                    )
                    HeroSubStat(
                        label = "UPLOADED",
                        value = DataFormatter.formatBytes(today.uploadBytes).full,
                        icon = DataSyncIcons.ArrowUp,
                        tint = colors.uploadAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Speed & Session Sparkline Card
        LiveSpeedTelemetryCard(speed = uiState.liveSpeed)

        // Network Breakdown Cards (Mobile vs Wi-Fi)
        SectionHeader(title = "Network Breakdown")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NetworkBreakdownCard(
                title = "MOBILE DATA",
                totalBytes = today.mobile.totalBytes,
                rxBytes = today.mobile.rxBytes,
                txBytes = today.mobile.txBytes,
                quotaFraction = DataFormatter.calculateClampedFraction(
                    today.mobile.totalBytes,
                    dailyLimitBytes
                ),
                icon = DataSyncIcons.Smartphone,
                accentColor = colors.mobileAccent,
                containerColor = colors.mobileContainer,
                modifier = Modifier.weight(1f)
            )
            NetworkBreakdownCard(
                title = "WI-FI DATA",
                totalBytes = today.wifi.totalBytes,
                rxBytes = today.wifi.rxBytes,
                txBytes = today.wifi.txBytes,
                quotaFraction = DataFormatter.calculateClampedFraction(
                    today.wifi.totalBytes,
                    today.totalBytes.coerceAtLeast(1L)
                ),
                icon = DataSyncIcons.Wifi,
                accentColor = colors.wifiAccent,
                containerColor = colors.wifiContainer,
                modifier = Modifier.weight(1f)
            )
        }

        // Billing Cycle Summary Card
        SectionHeader(
            title = "Billing Cycle Summary",
            trailingText = "Analytics",
            onTrailingClick = onOpenAnalytics
        )
        SurfaceCard(onClick = onOpenAnalytics) {
            val cycleFraction = DataFormatter.calculateClampedFraction(
                currentCycle.totalBytes,
                monthlyLimitBytes
            )
            val cyclePct = DataFormatter.calculatePercentage(
                currentCycle.totalBytes,
                monthlyLimitBytes
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadgeBox(
                        icon = SystemIcons.Calendar,
                        tint = colors.accent,
                        containerColor = colors.accentContainer,
                        size = 40.dp,
                        iconSize = 20.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = DataFormatter.formatBytes(currentCycle.totalBytes).full,
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.textPrimary
                        )
                        val daysSub = if (cycleWindow != null) {
                            "${cycleWindow.daysRemaining} days left in cycle • Resets ${
                                DataFormatter.formatOrdinalDay(uiState.preferences.billingCycleStartDay)
                            }"
                        } else {
                            "Monthly plan limit ${DataFormatter.formatBytes(monthlyLimitBytes).full}"
                        }
                        Text(
                            text = daysSub,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
                Text(
                    text = "$cyclePct%",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.accent
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { cycleFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = colors.accent,
                trackColor = colors.chartTrack,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mobile: ${DataFormatter.formatBytes(currentCycle.mobile.totalBytes).full}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Text(
                    text = "Wi-Fi: ${DataFormatter.formatBytes(currentCycle.wifi.totalBytes).full}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        // Top Consuming Apps Preview
        SectionHeader(
            title = "Top Active Applications Today",
            trailingText = "View All",
            onTrailingClick = onOpenApps
        )
        SurfaceCard(
            onClick = onOpenApps,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (!uiState.hasUsagePermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Grant Usage Access to inspect per-application traffic",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            } else if (uiState.topAppsToday.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No application traffic recorded yet today",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            } else {
                uiState.topAppsToday.forEachIndexed { idx, app ->
                    TopAppPreviewRow(app = app)
                    if (idx < uiState.topAppsToday.lastIndex) {
                        HorizontalDivider(
                            color = colors.divider,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun HeroSubStat(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color
) {
    val colors = DataSyncTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )
        }
    }
}

@Composable
private fun NetworkBreakdownCard(
    title: String,
    totalBytes: Long,
    rxBytes: Long,
    txBytes: Long,
    quotaFraction: Float,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = DataSyncTheme.colors
    val formatted = DataFormatter.formatBytes(totalBytes)
    SurfaceCard(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
            IconBadgeBox(
                icon = icon,
                tint = accentColor,
                containerColor = containerColor,
                size = 32.dp,
                iconSize = 16.dp,
                cornerRadius = 9.dp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = formatted.value,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatted.unit,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { quotaFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = accentColor,
            trackColor = colors.chartTrack,
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "DL ${DataFormatter.formatBytes(rxBytes).full} • UL ${DataFormatter.formatBytes(txBytes).full}",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TopAppPreviewRow(app: AppDataUsage) {
    val colors = DataSyncTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = app.appName,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp))
            )
        } else {
            IconBadgeBox(
                icon = if (app.isSpecialBucket) SystemIcons.Cpu else DataSyncIcons.Apps,
                tint = colors.accent,
                containerColor = colors.accentContainer,
                size = 36.dp,
                iconSize = 18.dp,
                cornerRadius = 9.dp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Mobile ${DataFormatter.formatBytes(app.mobileTotalBytes).full} • Wi-Fi ${DataFormatter.formatBytes(app.wifiTotalBytes).full}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = DataFormatter.formatBytes(app.totalBytes).full,
            style = MaterialTheme.typography.labelLarge,
            color = colors.textPrimary
        )
    }
}
