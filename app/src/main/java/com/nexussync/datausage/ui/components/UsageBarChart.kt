package com.nexussync.datausage.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.nexussync.datausage.domain.model.ChartNetworkSeries
import com.nexussync.datausage.domain.model.DailyUsageEntry
import com.nexussync.datausage.ui.theme.DataSyncTheme
import com.nexussync.datausage.util.DataFormatter

@Composable
fun UsageBarChart(
    entries: List<DailyUsageEntry>,
    selectedIndex: Int,
    series: ChartNetworkSeries,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DataSyncTheme.colors
    if (entries.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No daily history available",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )
        }
        return
    }

    val safeSelected = selectedIndex.coerceIn(0, entries.lastIndex)
    val selectedEntry = entries[safeSelected]

    val values = entries.map { entry ->
        when (series) {
            ChartNetworkSeries.COMBINED -> entry.totalBytes
            ChartNetworkSeries.MOBILE -> entry.mobileBytes
            ChartNetworkSeries.WIFI -> entry.wifiBytes
        }
    }
    val maxVal = (values.maxOrNull() ?: 1L).coerceAtLeast(1L)

    Column(modifier = modifier.fillMaxWidth()) {
        // Selected Day Callout Strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.cardElevated)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedEntry.fullDateLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.accent
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = DataFormatter.formatBytes(values[safeSelected]).full,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.textPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Mobile: ${DataFormatter.formatBytes(selectedEntry.mobileBytes).full}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.mobileAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Wi-Fi: ${DataFormatter.formatBytes(selectedEntry.wifiBytes).full}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.wifiAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive 30-Bar Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp)
                .pointerInput(entries.size) {
                    detectTapGestures { tapOffset ->
                        if (size.width > 0 && entries.isNotEmpty()) {
                            val slotWidth = size.width.toFloat() / entries.size.toFloat()
                            val idx = (tapOffset.x / slotWidth).toInt().coerceIn(0, entries.lastIndex)
                            onSelectIndex(idx)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val gridFractions = listOf(0.25f, 0.5f, 0.75f, 1.0f)

            for (fraction in gridFractions) {
                val y = h - (h * fraction)
                drawLine(
                    color = colors.divider,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            // Baseline
            drawLine(
                color = colors.cardBorder,
                start = Offset(0f, h),
                end = Offset(w, h),
                strokeWidth = 1.dp.toPx()
            )

            val count = entries.size
            val slotWidth = w / count.toFloat()
            val gap = (slotWidth * 0.28f).coerceAtLeast(2.dp.toPx())
            val barWidth = (slotWidth - gap).coerceAtLeast(2.dp.toPx())
            val corner = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            val minBarHeight = 3.dp.toPx()

            entries.forEachIndexed { idx, entry ->
                val rawValue = values[idx]
                val ratio = (rawValue.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                val barHeight = if (rawValue > 0L) {
                    (ratio * (h - 6.dp.toPx())).coerceAtLeast(minBarHeight)
                } else {
                    minBarHeight * 0.65f
                }

                val left = idx * slotWidth + (gap / 2f)
                val top = h - barHeight
                val isSelected = idx == safeSelected

                val barColor: Color = when {
                    rawValue <= 0L -> colors.chartTrack
                    isSelected -> when (series) {
                        ChartNetworkSeries.COMBINED -> colors.accent
                        ChartNetworkSeries.MOBILE -> colors.mobileAccent
                        ChartNetworkSeries.WIFI -> colors.wifiAccent
                    }
                    entry.isToday -> colors.accent.copy(alpha = 0.8f)
                    else -> when (series) {
                        ChartNetworkSeries.COMBINED -> colors.chartInactiveBar
                        ChartNetworkSeries.MOBILE -> colors.mobileAccent.copy(alpha = 0.38f)
                        ChartNetworkSeries.WIFI -> colors.wifiAccent.copy(alpha = 0.38f)
                    }
                }

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = corner
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Clean X-Axis Date Markers
        val firstLabel = entries.firstOrNull()?.shortDateLabel ?: ""
        val midLabel = entries.getOrNull(entries.size / 2)?.shortDateLabel ?: ""
        val lastLabel = entries.lastOrNull()?.shortDateLabel ?: "Today"

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = firstLabel,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
            Text(
                text = midLabel,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
            Text(
                text = lastLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChartLegendDot(color = colors.accent, label = "Selected / Today")
            ChartLegendDot(color = colors.chartInactiveBar, label = "Previous Days (Tap bar to inspect)")
        }
    }
}

@Composable
private fun ChartLegendDot(color: Color, label: String) {
    val colors = DataSyncTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
    }
}
