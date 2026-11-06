package com.nexussync.datausage.ui.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexussync.datausage.domain.model.ThemeMode
import com.nexussync.datausage.ui.components.IconBadgeBox
import com.nexussync.datausage.ui.components.SectionHeader
import com.nexussync.datausage.ui.components.SettingNavigationRow
import com.nexussync.datausage.ui.components.SettingToggleRow
import com.nexussync.datausage.ui.components.SurfaceCard
import com.nexussync.datausage.ui.icons.DataSyncIcons
import com.nexussync.datausage.ui.icons.SystemIcons
import com.nexussync.datausage.ui.theme.DataSyncTheme
import com.nexussync.datausage.util.DataFormatter
import com.nexussync.datausage.util.SystemSettingsNavigator
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenAbout: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs = uiState.preferences
    val colors = DataSyncTheme.colors
    val context = LocalContext.current
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

    if (uiState.showBillingDayDialog) {
        BillingDayPickerDialog(
            currentDay = prefs.billingCycleStartDay,
            onSelectDay = viewModel::setBillingCycleStartDay,
            onDismiss = { viewModel.setShowBillingDayDialog(false) }
        )
    }

    if (uiState.showThemeSheet) {
        ThemeSelectionDialog(
            currentMode = prefs.themeMode,
            onSelectMode = viewModel::setThemeMode,
            onDismiss = { viewModel.setShowThemeSheet(false) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Text(
                text = "SETTINGS & CONTROLS",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )
            Text(
                text = "Configure quotas, telemetry service, and workspace theme",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
        }

        // 1. Telemetry & Background Service
        SectionHeader(title = "Service & Notifications")
        SurfaceCard(contentPadding = PaddingValues(0.dp)) {
            SettingToggleRow(
                icon = DataSyncIcons.Pulse,
                title = "Live Telemetry Engine",
                subtitle = if (prefs.monitoringActive) {
                    "Real-time speed and usage tracking is active"
                } else {
                    "Telemetry collection is paused"
                },
                checked = prefs.monitoringActive,
                onCheckedChange = viewModel::setMonitoringActive
            )
            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 18.dp))

            SettingToggleRow(
                icon = SystemIcons.Bell,
                title = "Status Bar Telemetry",
                subtitle = "Persistent notification with today's quota and usage",
                checked = prefs.persistentNotification,
                enabled = prefs.monitoringActive,
                onCheckedChange = { enabled ->
                    if (enabled && !uiState.hasNotificationPermission &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ) {
                        viewModel.setPersistentNotification(true)
                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setPersistentNotification(enabled)
                    }
                }
            )
            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 18.dp))

            SettingToggleRow(
                icon = DataSyncIcons.Gauge,
                title = "Live Speed in Notification",
                subtitle = "Display real-time DL & UL throughput in status drawer",
                checked = prefs.showLiveSpeedInNotification,
                enabled = prefs.monitoringActive && prefs.persistentNotification,
                onCheckedChange = viewModel::setShowLiveSpeedInNotification
            )
            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 18.dp))

            SettingToggleRow(
                icon = SystemIcons.Power,
                title = "Auto-Start on Boot",
                subtitle = "Resume background telemetry automatically after restart",
                checked = prefs.autoStartOnBoot,
                onCheckedChange = viewModel::setAutoStartOnBoot
            )
        }

        // 2. Data Quotas & Alert Thresholds
        SectionHeader(title = "Data Plan & Quota Limits")
        SurfaceCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadgeBox(
                        icon = SystemIcons.HardDrive,
                        tint = colors.accent,
                        containerColor = colors.accentContainer,
                        size = 38.dp,
                        iconSize = 19.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Data Quota",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Primary gauge target & alert baseline",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
                Text(
                    text = String.format(Locale.US, "%.1f GB", prefs.dailyLimitGb),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.accent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Slider(
                value = prefs.dailyLimitGb,
                onValueChange = viewModel::setDailyLimitGb,
                valueRange = 0.5f..20.0f,
                steps = 38,
                colors = SliderDefaults.colors(
                    thumbColor = colors.accent,
                    activeTrackColor = colors.accent,
                    inactiveTrackColor = colors.chartTrack
                )
            )

            // Quick Preset Chips
            val dailyPresets = listOf(1.0f, 1.5f, 2.0f, 3.0f, 5.0f, 10.0f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dailyPresets.forEach { preset ->
                    val isSelected = kotlin.math.abs(prefs.dailyLimitGb - preset) < 0.05f
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setDailyLimitGb(preset) },
                        label = {
                            Text(
                                text = "${preset} GB",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape = RoundedCornerShape(9.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.accentContainer,
                            selectedLabelColor = colors.accent,
                            containerColor = colors.cardElevated,
                            labelColor = colors.textSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = colors.divider)
            Spacer(modifier = Modifier.height(16.dp))

            // Monthly Limit Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Cycle Plan",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Billing cycle allowance target",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Text(
                    text = "${prefs.monthlyLimitGb.toInt()} GB",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
            }
            Slider(
                value = prefs.monthlyLimitGb,
                onValueChange = viewModel::setMonthlyLimitGb,
                valueRange = 5f..300f,
                steps = 58,
                colors = SliderDefaults.colors(
                    thumbColor = colors.accent,
                    activeTrackColor = colors.accent,
                    inactiveTrackColor = colors.chartTrack
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = colors.divider)
            Spacer(modifier = Modifier.height(12.dp))

            // Alert Threshold Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Quota Warning Threshold",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Notify when daily usage crosses ${prefs.warningThresholdPercent}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(70, 80, 90).forEach { pct ->
                        val selected = prefs.warningThresholdPercent == pct
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) colors.accent else colors.cardElevated)
                                .clickable { viewModel.setWarningThresholdPercent(pct) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) colors.onAccent else colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // 3. Appearance & Cycle Preferences
        SectionHeader(title = "Appearance & Billing Cycle")
        SurfaceCard(contentPadding = PaddingValues(0.dp)) {
            SettingNavigationRow(
                icon = if (colors.isDark) SystemIcons.Moon else SystemIcons.Sun,
                title = "Appearance Theme",
                subtitle = prefs.themeMode.subtitle,
                trailingBadgeText = prefs.themeMode.label,
                trailingBadgeColor = colors.accent,
                onClick = { viewModel.setShowThemeSheet(true) }
            )
            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 18.dp))
            SettingNavigationRow(
                icon = SystemIcons.Calendar,
                title = "Billing Cycle Reset Day",
                subtitle = "Monthly quota resets on the ${DataFormatter.formatOrdinalDay(prefs.billingCycleStartDay)} of every month",
                trailingBadgeText = DataFormatter.formatOrdinalDay(prefs.billingCycleStartDay),
                trailingBadgeColor = colors.textPrimary,
                onClick = { viewModel.setShowBillingDayDialog(true) }
            )
        }

        // 4. System Permissions & OS Controls
        SectionHeader(title = "System & Privacy")
        SurfaceCard(contentPadding = PaddingValues(0.dp)) {
            SettingNavigationRow(
                icon = if (uiState.hasUsagePermission) SystemIcons.ShieldCheck else SystemIcons.ShieldAlert,
                title = "Usage Access Permission",
                subtitle = if (uiState.hasUsagePermission) {
                    "Granted for accurate cellular & Wi-Fi telemetry"
                } else {
                    "Required — tap to grant in Android Settings"
                },
                trailingBadgeText = if (uiState.hasUsagePermission) "Active" else "Grant",
                trailingBadgeColor = if (uiState.hasUsagePermission) colors.statusHealthy else colors.statusWarning,
                iconTint = if (uiState.hasUsagePermission) colors.statusHealthy else colors.statusWarning,
                iconContainerColor = (if (uiState.hasUsagePermission) colors.statusHealthy else colors.statusWarning)
                    .copy(alpha = 0.14f),
                onClick = { SystemSettingsNavigator.openUsageAccessSettings(context) }
            )
            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 18.dp))

            SettingNavigationRow(
                icon = SystemIcons.Bell,
                title = "Notification Permission",
                subtitle = if (uiState.hasNotificationPermission) {
                    "Authorized for live status bar drawer updates"
                } else {
                    "Disabled — tap to enable notification access"
                },
                trailingBadgeText = if (uiState.hasNotificationPermission) "Allowed" else "Allow",
                trailingBadgeColor = if (uiState.hasNotificationPermission) colors.statusHealthy else colors.statusWarning,
                onClick = {
                    if (!uiState.hasNotificationPermission &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ) {
                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        SystemSettingsNavigator.openNotificationSettings(context)
                    }
                }
            )
            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 18.dp))

            SettingNavigationRow(
                icon = SystemIcons.ShieldCheck,
                title = "Android System Data Saver",
                subtitle = "Restrict background cellular usage across apps",
                trailingBadgeText = uiState.dataSaverState.displayLabel,
                onClick = { SystemSettingsNavigator.openDataSaverSettings(context) }
            )
            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 18.dp))

            SettingNavigationRow(
                icon = SystemIcons.Info,
                title = "About Data Sync",
                subtitle = "Architecture, local privacy guarantee & feedback",
                trailingBadgeText = "v4.0.0",
                onClick = onOpenAbout
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = DataSyncTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBackground,
        titleContentColor = colors.textPrimary,
        title = {
            Text(
                text = "Appearance Theme",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = mode == currentMode
                    val shape = RoundedCornerShape(12.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(if (isSelected) colors.accentContainer else colors.cardElevated)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) colors.accent else colors.cardBorder,
                                shape = shape
                            )
                            .clickable { onSelectMode(mode) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                            Text(
                                text = mode.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = SystemIcons.Check,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Done",
                    color = colors.accent,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}
