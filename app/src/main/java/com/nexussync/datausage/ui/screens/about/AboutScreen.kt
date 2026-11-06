package com.nexussync.datausage.ui.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexussync.datausage.BuildConfig
import com.nexussync.datausage.ui.components.SectionHeader
import com.nexussync.datausage.ui.components.SettingNavigationRow
import com.nexussync.datausage.ui.components.SurfaceCard
import com.nexussync.datausage.ui.icons.DataSyncIcons
import com.nexussync.datausage.ui.icons.SystemIcons
import com.nexussync.datausage.ui.theme.DataSyncTheme
import com.nexussync.datausage.util.SystemSettingsNavigator

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val colors = DataSyncTheme.colors
    val context = LocalContext.current
    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = colors.cardBackground,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
            title = {
                Text(
                    text = "Local-First Privacy Policy",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Data Sync processes 100% of network usage statistics locally on your Android device using Android's native NetworkStatsManager and TrafficStats APIs.\n\nNo personal data, browsing history, IP addresses, or app usage metrics are ever transmitted, stored on external servers, or shared with third parties.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(
                        text = "Understood",
                        color = colors.accent,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = SystemIcons.ArrowLeft,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ABOUT DATA SYNC",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Brand Identity Card
        SurfaceCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.accentContainer)
                        .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = DataSyncIcons.DataSyncMark,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Data Sync",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Native Kotlin & Jetpack Compose Architecture",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.cardElevated)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE}) • Engineered by Codedth",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.accent
                    )
                }
            }
        }

        SectionHeader(title = "Architecture Highlights")
        SurfaceCard {
            ArchitecturePoint(
                title = "100% On-Device Native Telemetry",
                description = "Direct Android NetworkStatsManager & TrafficStats hardware counters with zero WebView overhead."
            )
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = colors.divider)
            Spacer(modifier = Modifier.height(14.dp))
            ArchitecturePoint(
                title = "Zero External Tracking",
                description = "No analytics SDKs, no cloud sync servers, and zero advertising identifiers."
            )
        }

        SectionHeader(title = "Support & System")
        SurfaceCard(contentPadding = PaddingValues(0.dp)) {
            SettingNavigationRow(
                icon = SystemIcons.Mail,
                title = "Send Engineering Feedback",
                subtitle = "Report issues or request telemetry enhancements",
                trailingIcon = SystemIcons.ExternalLink,
                onClick = { SystemSettingsNavigator.sendFeedbackEmail(context) }
            )
            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 18.dp))
            SettingNavigationRow(
                icon = SystemIcons.ShieldCheck,
                title = "Privacy Assurance",
                subtitle = "Review how your network metrics stay strictly on-device",
                onClick = { showPrivacyDialog = true }
            )
            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 18.dp))
            SettingNavigationRow(
                icon = DataSyncIcons.Wifi,
                title = "System Network Settings",
                subtitle = "Open Android SIM, cellular, and Wi-Fi controls",
                trailingIcon = SystemIcons.ExternalLink,
                onClick = { SystemSettingsNavigator.openWirelessSettings(context) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ArchitecturePoint(
    title: String,
    description: String
) {
    val colors = DataSyncTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = SystemIcons.Layers,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}
