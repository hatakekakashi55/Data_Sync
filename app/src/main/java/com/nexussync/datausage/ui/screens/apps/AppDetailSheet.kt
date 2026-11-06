@file:OptIn(ExperimentalMaterial3Api::class)

package com.nexussync.datausage.ui.screens.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexussync.datausage.domain.model.AppDataUsage
import com.nexussync.datausage.ui.components.IconBadgeBox
import com.nexussync.datausage.ui.icons.DataSyncIcons
import com.nexussync.datausage.ui.icons.SystemIcons
import com.nexussync.datausage.ui.theme.DataSyncTheme
import com.nexussync.datausage.util.DataFormatter
import com.nexussync.datausage.util.SystemSettingsNavigator

@Composable
fun AppDetailBottomSheet(
    app: AppDataUsage,
    onDismiss: () -> Unit
) {
    val colors = DataSyncTheme.colors
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardBackground,
        contentColor = colors.textPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 10.dp)
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
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                } else {
                    IconBadgeBox(
                        icon = if (app.isSpecialBucket) SystemIcons.Cpu else DataSyncIcons.Apps,
                        tint = colors.accent,
                        containerColor = colors.accentContainer,
                        size = 50.dp,
                        iconSize = 24.dp,
                        cornerRadius = 14.dp
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Total Highlight Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.cardElevated)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMBINED CONSUMPTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = DataFormatter.formatBytes(app.totalBytes).full,
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.textPrimary
                        )
                    }
                    Text(
                        text = "${(app.shareFraction * 100f).toInt()}% share",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SheetMetricBox(
                    title = "MOBILE DATA",
                    total = DataFormatter.formatBytes(app.mobileTotalBytes).full,
                    sub = "DL ${DataFormatter.formatBytes(app.mobileRxBytes).full} • UL ${DataFormatter.formatBytes(app.mobileTxBytes).full}",
                    accent = colors.mobileAccent,
                    modifier = Modifier.weight(1f)
                )
                SheetMetricBox(
                    title = "WI-FI DATA",
                    total = DataFormatter.formatBytes(app.wifiTotalBytes).full,
                    sub = "DL ${DataFormatter.formatBytes(app.wifiRxBytes).full} • UL ${DataFormatter.formatBytes(app.wifiTxBytes).full}",
                    accent = colors.wifiAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!app.isSpecialBucket) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = colors.divider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        SystemSettingsNavigator.openAppDetailsSettings(context, app.packageName)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.onAccent
                    )
                ) {
                    Icon(
                        imageVector = SystemIcons.ExternalLink,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Manage Background Data in Android Settings",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun SheetMetricBox(
    title: String,
    total: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = DataSyncTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cardElevated)
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = total,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
