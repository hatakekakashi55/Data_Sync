package com.nexussync.datausage.util

import com.nexussync.datausage.domain.model.BillingCycleWindow
import java.util.Calendar
import java.util.Locale
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

data class FormattedData(
    val value: String,
    val unit: String,
    val full: String
)

object DataFormatter {
    private val BYTE_UNITS = arrayOf("B", "KB", "MB", "GB", "TB")
    private val SPEED_UNITS = arrayOf("B/s", "KB/s", "MB/s", "GB/s")

    fun formatBytes(bytes: Long, decimals: Int = 1): FormattedData {
        if (bytes <= 0L) {
            return FormattedData(value = "0", unit = "B", full = "0 B")
        }
        if (bytes < 1024L) {
            return FormattedData(value = bytes.toString(), unit = "B", full = "$bytes B")
        }
        val k = 1024.0
        val unitIndex = min(
            (ln(bytes.toDouble()) / ln(k)).toInt(),
            BYTE_UNITS.lastIndex
        )
        val scaled = bytes.toDouble() / k.pow(unitIndex.toDouble())
        val effectiveDecimals = when {
            unitIndex <= 1 -> 0 // B and KB look cleaner as whole numbers
            scaled >= 100.0 -> 1
            else -> decimals
        }
        val formattedVal = String.format(Locale.US, "%.${effectiveDecimals}f", scaled)
        val unit = BYTE_UNITS[unitIndex]
        return FormattedData(
            value = formattedVal,
            unit = unit,
            full = "$formattedVal $unit"
        )
    }

    fun formatSpeed(bytesPerSec: Long): FormattedData {
        if (bytesPerSec <= 0L) {
            return FormattedData(value = "0", unit = "B/s", full = "0 B/s")
        }
        if (bytesPerSec < 1024L) {
            return FormattedData(
                value = bytesPerSec.toString(),
                unit = "B/s",
                full = "$bytesPerSec B/s"
            )
        }
        val k = 1024.0
        val unitIndex = min(
            (ln(bytesPerSec.toDouble()) / ln(k)).toInt(),
            SPEED_UNITS.lastIndex
        )
        val scaled = bytesPerSec.toDouble() / k.pow(unitIndex.toDouble())
        val decimals = when {
            unitIndex == 1 && scaled >= 10.0 -> 0 // e.g., 420 KB/s
            scaled >= 100.0 -> 0
            else -> 1
        }
        val formattedVal = String.format(Locale.US, "%.${decimals}f", scaled)
        val unit = SPEED_UNITS[unitIndex]
        return FormattedData(
            value = formattedVal,
            unit = unit,
            full = "$formattedVal $unit"
        )
    }

    fun calculatePercentage(part: Long, whole: Long): Int {
        if (whole <= 0L || part <= 0L) return 0
        val raw = (part.toDouble() / whole.toDouble()) * 100.0
        return raw.roundToInt().coerceIn(0, 999)
    }

    fun calculateClampedFraction(part: Long, whole: Long): Float {
        if (whole <= 0L || part <= 0L) return 0f
        return (part.toDouble() / whole.toDouble()).toFloat().coerceIn(0f, 1f)
    }

    fun formatDuration(totalSeconds: Long): String {
        val safeSec = totalSeconds.coerceAtLeast(0L)
        val hours = safeSec / 3600L
        val minutes = (safeSec % 3600L) / 60L
        val seconds = safeSec % 60L
        return when {
            hours > 0L -> "${hours}h ${minutes}m"
            minutes > 0L -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    fun formatOrdinalDay(day: Int): String {
        val clamped = day.coerceIn(1, 31)
        val suffix = when {
            clamped in 11..13 -> "th"
            clamped % 10 == 1 -> "st"
            clamped % 10 == 2 -> "nd"
            clamped % 10 == 3 -> "rd"
            else -> "th"
        }
        return "$clamped$suffix"
    }
}

object BillingCycleCalculator {
    private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

    fun computeWindow(
        billingStartDay: Int,
        referenceTimeMillis: Long = System.currentTimeMillis()
    ): BillingCycleWindow {
        val safeDay = billingStartDay.coerceIn(1, 28)

        val nowCal = Calendar.getInstance().apply {
            timeInMillis = referenceTimeMillis
        }

        val currentStartCal = (nowCal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, safeDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (currentStartCal.timeInMillis > referenceTimeMillis) {
            currentStartCal.add(Calendar.MONTH, -1)
        }

        val nextStartCal = (currentStartCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }

        val previousStartCal = (currentStartCal.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }

        val startMillis = currentStartCal.timeInMillis
        val nextStartMillis = nextStartCal.timeInMillis
        val totalDays = (((nextStartMillis - startMillis) + (MILLIS_PER_DAY / 2)) / MILLIS_PER_DAY)
            .toInt()
            .coerceAtLeast(1)
        val elapsedDays = (((referenceTimeMillis - startMillis) / MILLIS_PER_DAY).toInt() + 1)
            .coerceIn(1, totalDays)
        val remainingDays = (totalDays - elapsedDays).coerceAtLeast(0)

        return BillingCycleWindow(
            startMillis = startMillis,
            endMillis = referenceTimeMillis,
            previousStartMillis = previousStartCal.timeInMillis,
            previousEndMillis = startMillis,
            daysElapsed = elapsedDays,
            daysRemaining = remainingDays,
            totalCycleDays = totalDays
        )
    }

    fun startOfTodayMillis(referenceTimeMillis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = referenceTimeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
