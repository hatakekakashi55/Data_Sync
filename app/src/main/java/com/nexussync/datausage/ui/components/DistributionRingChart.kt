package com.nexussync.datausage.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nexussync.datausage.ui.theme.DataSyncTheme
import com.nexussync.datausage.util.DataFormatter

@Composable
fun DistributionRingRow(
    primaryLabel: String,
    primaryBytes: Long,
    primaryColor: Color,
    secondaryLabel: String,
    secondaryBytes: Long,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = DataSyncTheme.colors
    val total = (primaryBytes + secondaryBytes).coerceAtLeast(0L)
    val primaryPct = DataFormatter.calculatePercentage(primaryBytes, total)
    val secondaryPct = if (total > 0L) (100 - primaryPct).coerceIn(0, 100) else 0
    val primaryFraction = DataFormatter.calculateClampedFraction(primaryBytes, total)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier.size(92.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(92.dp)) {
                val stroke = 10.dp.toPx()
                val diameter = size.minDimension - stroke
                val topLeft = Offset(stroke / 2f, stroke / 2f)
                val arcSize = Size(diameter, diameter)

                if (total <= 0L) {
                    drawArc(
                        color = colors.chartTrack,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke)
                    )
                } else {
                    val primarySweep = (primaryFraction * 360f).coerceIn(0f, 360f)
                    val secondarySweep = (360f - primarySweep).coerceIn(0f, 360f)

                    if (secondarySweep > 0f) {
                        drawArc(
                            color = secondaryColor,
                            startAngle = -90f + primarySweep,
                            sweepAngle = secondarySweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Butt)
                        )
                    }
                    if (primarySweep > 0f) {
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = primarySweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Butt)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = DataFormatter.formatBytes(total, decimals = 1).value,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = DataFormatter.formatBytes(total, decimals = 1).unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DistributionLegendLine(
                label = primaryLabel,
                bytesText = DataFormatter.formatBytes(primaryBytes).full,
                percent = primaryPct,
                dotColor = primaryColor
            )
            DistributionLegendLine(
                label = secondaryLabel,
                bytesText = DataFormatter.formatBytes(secondaryBytes).full,
                percent = secondaryPct,
                dotColor = secondaryColor
            )
        }
    }
}

@Composable
private fun DistributionLegendLine(
    label: String,
    bytesText: String,
    percent: Int,
    dotColor: Color
) {
    val colors = DataSyncTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
            Text(
                text = "$bytesText ($percent%)",
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
