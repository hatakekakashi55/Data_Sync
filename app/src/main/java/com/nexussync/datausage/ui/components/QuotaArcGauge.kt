package com.nexussync.datausage.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nexussync.datausage.ui.theme.DataSyncTheme
import com.nexussync.datausage.util.DataFormatter
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QuotaArcGauge(
    usedBytes: Long,
    limitBytes: Long,
    warningThresholdPercent: Int,
    modifier: Modifier = Modifier
) {
    val colors = DataSyncTheme.colors
    val fraction = DataFormatter.calculateClampedFraction(usedBytes, limitBytes)
    val percentage = DataFormatter.calculatePercentage(usedBytes, limitBytes)
    val formattedUsed = DataFormatter.formatBytes(usedBytes)
    val formattedLimit = DataFormatter.formatBytes(limitBytes)
    val remainingBytes = (limitBytes - usedBytes).coerceAtLeast(0L)
    val formattedRemaining = DataFormatter.formatBytes(remainingBytes)

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "quotaArcFraction"
    )

    val activeArcColor = when {
        usedBytes >= limitBytes && limitBytes > 0L -> colors.statusCritical
        percentage >= warningThresholdPercent -> colors.statusWarning
        else -> colors.accent
    }

    Box(
        modifier = modifier.size(226.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 14.dp.toPx()
            val inset = strokePx / 2f + 10.dp.toPx()
            val arcDiameter = size.minDimension - (inset * 2f)
            val topLeft = Offset(
                x = (size.width - arcDiameter) / 2f,
                y = (size.height - arcDiameter) / 2f
            )
            val arcSize = Size(arcDiameter, arcDiameter)

            val startAngle = 140f
            val totalSweep = 260f

            // Subtle precision outer tick marks
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = (arcDiameter / 2f) + 12.dp.toPx()
            val innerTickRadius = outerRadius - 4.dp.toPx()
            val tickCount = 26
            for (i in 0..tickCount) {
                val angleDeg = startAngle + (totalSweep * i / tickCount.toFloat())
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val isFilled = (i.toFloat() / tickCount.toFloat()) <= animatedFraction
                val p1 = Offset(
                    x = center.x + (innerTickRadius * cos(angleRad)).toFloat(),
                    y = center.y + (innerTickRadius * sin(angleRad)).toFloat()
                )
                val p2 = Offset(
                    x = center.x + (outerRadius * cos(angleRad)).toFloat(),
                    y = center.y + (outerRadius * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = if (isFilled) activeArcColor.copy(alpha = 0.7f) else colors.chartTrack,
                    start = p1,
                    end = p2,
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Background arc track
            drawArc(
                color = colors.chartTrack,
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active quota progress arc
            if (animatedFraction > 0.003f) {
                drawArc(
                    color = activeArcColor,
                    startAngle = startAngle,
                    sweepAngle = totalSweep * animatedFraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = "TODAY'S USAGE",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedUsed.value,
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = " ${formattedUsed.unit}",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(activeArcColor.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "$percentage% of ${formattedLimit.full}",
                    style = MaterialTheme.typography.labelMedium,
                    color = activeArcColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (usedBytes >= limitBytes && limitBytes > 0L) {
                    "Daily limit exceeded"
                } else {
                    "${formattedRemaining.full} remaining"
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
        }
    }
}
