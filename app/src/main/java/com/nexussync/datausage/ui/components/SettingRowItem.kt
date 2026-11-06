package com.nexussync.datausage.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nexussync.datausage.ui.icons.SystemIcons
import com.nexussync.datausage.ui.theme.DataSyncTheme

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconTint: Color = DataSyncTheme.colors.accent,
    iconContainerColor: Color = DataSyncTheme.colors.accentContainer,
    enabled: Boolean = true
) {
    val colors = DataSyncTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadgeBox(
            icon = icon,
            tint = if (enabled) iconTint else colors.textMuted,
            containerColor = iconContainerColor,
            size = 38.dp,
            iconSize = 19.dp,
            cornerRadius = 11.dp
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (enabled) colors.textPrimary else colors.textMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onAccent,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.cardElevated,
                uncheckedBorderColor = colors.cardBorder
            )
        )
    }
}

@Composable
fun SettingNavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingBadgeText: String? = null,
    trailingBadgeColor: Color = DataSyncTheme.colors.textSecondary,
    iconTint: Color = DataSyncTheme.colors.accent,
    iconContainerColor: Color = DataSyncTheme.colors.accentContainer,
    trailingIcon: ImageVector = SystemIcons.ChevronRight
) {
    val colors = DataSyncTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconBadgeBox(
            icon = icon,
            tint = iconTint,
            containerColor = iconContainerColor,
            size = 38.dp,
            iconSize = 19.dp,
            cornerRadius = 11.dp
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        if (trailingBadgeText != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = trailingBadgeText,
                style = MaterialTheme.typography.labelLarge,
                color = trailingBadgeColor
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
