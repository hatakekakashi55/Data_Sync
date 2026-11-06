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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nexussync.datausage.domain.model.LiveSpeedSnapshot
import com.nexussync.datausage.ui.icons.DataSyncIcons
import com.nexussync.datausage.ui.theme.DataSyncTheme
import com.nexussync.datausage.util.DataFormatter

@Composable
fun LiveSpeedTelemetryCard(
    speed: LiveSpeedSnapshot,
    modifier: Modifier = Modifier
) {
    val colors = DataSyncTheme.colors
    val dl = DataFormatter.formatSpeed(speed.downloadBytesPerSec)
    val ul = DataFormatter.formatSpeed(speed.uploadBytesPerSec)
    val peakDl = DataFormatter.formatSpeed(speed.peakDownloadBytesPerSec)
    val sessionTotal = DataFormatter.formatBytes(speed.sessionTotalBytes)

    SurfaceCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = DataSyncIcons.Pulse,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REAL-TIME THROUGHPUT",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
            Text(
                text = "Session ${DataFormatter.formatDuration(speed.sessionDurationSec)}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SpeedRateTile(
                label = "DOWNLOAD",
                value = dl.value,
                unit = dl.unit,
                icon = DataSyncIcons.ArrowDown,
                accentColor = colors.downloadAccent,
                modifier = Modifier.weight(1f)
            )
            SpeedRateTile(
                label = "UPLOAD",
                value = ul.value,
                unit = ul.unit,
                icon = DataSyncIcons.ArrowUp,
                accentColor = colors.uploadAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SparklineWaveCanvas(
            downloadPoints = speed.recentDownloadHistory,
            uploadPoints = speed.recentUploadHistory,
            downloadColor = colors.downloadAccent,
            uploadColor = colors.uploadAccent,
            gridColor = colors.divider,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = colors.divider, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Peak DL: ${peakDl.full}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Text(
                text = "Session Traffic: ${sessionTotal.full}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun SpeedRateTile(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = DataSyncTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cardElevated)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun SparklineWaveCanvas(
    downloadPoints: List<Long>,
    uploadPoints: List<Long>,
    downloadColor: Color,
    uploadColor: Color,
    gridColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f || downloadPoints.size < 2) return@Canvas

        // Baseline & mid gridline
        drawLine(
            color = gridColor,
            start = Offset(0f, h * 0.5f),
            end = Offset(w, h * 0.5f),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = gridColor,
            start = Offset(0f, h - 1f),
            end = Offset(w, h - 1f),
            strokeWidth = 1.dp.toPx()
        )

        val maxRate = maxOf(
            downloadPoints.maxOrNull() ?: 0L,
            uploadPoints.maxOrNull() ?: 0L,
            10_240L // minimum 10 KB/s scale so idle noise stays flat
        ).toFloat()

        fun drawSeries(points: List<Long>, lineColor: Color, drawArea: Boolean) {
            if (points.size < 2) return
            val stepX = w / (points.size - 1).toFloat()
            val verticalPadding = 4.dp.toPx()
            val usableHeight = (h - verticalPadding * 2f).coerceAtLeast(1f)

            val coords = points.mapIndexed { idx, value ->
                val normalized = (value.toFloat() / maxRate).coerceIn(0f, 1f)
                Offset(
                    x = idx * stepX,
                    y = h - verticalPadding - (normalized * usableHeight)
                )
            }

            val linePath = Path().apply {
                moveTo(coords.first().x, coords.first().y)
                for (i in 1 until coords.size) {
                    val prev = coords[i - 1]
                    val curr = coords[i]
                    val controlX = (prev.x + curr.x) / 2f
                    cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
                }
            }

            if (drawArea) {
                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(coords.last().x, h)
                    lineTo(coords.first().x, h)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.25f),
                            lineColor.copy(alpha = 0.0f)
                        ),
                        startY = 0f,
                        endY = h
                    )
                )
            }

            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = if (drawArea) 2.2.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        drawSeries(uploadPoints, uploadColor.copy(alpha = 0.75f), drawArea = false)
        drawSeries(downloadPoints, downloadColor, drawArea = true)
    }
}
