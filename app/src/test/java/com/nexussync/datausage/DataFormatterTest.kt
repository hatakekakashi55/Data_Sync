package com.nexussync.datausage

import com.nexussync.datausage.util.BillingCycleCalculator
import com.nexussync.datausage.util.DataFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DataFormatterTest {

    @Test
    fun formatBytes_handlesZeroAndSmallBytes() {
        assertEquals("0 B", DataFormatter.formatBytes(0L).full)
        assertEquals("0 B", DataFormatter.formatBytes(-50L).full)
        assertEquals("512 B", DataFormatter.formatBytes(512L).full)
    }

    @Test
    fun formatBytes_scalesKilobytesMegabytesAndGigabytes() {
        assertEquals("2 KB", DataFormatter.formatBytes(2048L).full)
        assertEquals("1.5 MB", DataFormatter.formatBytes((1.5 * 1024 * 1024).toLong()).full)
        assertEquals("2.0 GB", DataFormatter.formatBytes(2L * 1024L * 1024L * 1024L).full)
    }

    @Test
    fun formatSpeed_formatsThroughputCleanly() {
        assertEquals("0 B/s", DataFormatter.formatSpeed(0L).full)
        assertEquals("420 KB/s", DataFormatter.formatSpeed(420L * 1024L).full)
        assertEquals("4.5 MB/s", DataFormatter.formatSpeed((4.5 * 1024 * 1024).toLong()).full)
    }

    @Test
    fun formatOrdinalDay_handlesEnglishOrdinals() {
        assertEquals("1st", DataFormatter.formatOrdinalDay(1))
        assertEquals("2nd", DataFormatter.formatOrdinalDay(2))
        assertEquals("3rd", DataFormatter.formatOrdinalDay(3))
        assertEquals("11th", DataFormatter.formatOrdinalDay(11))
        assertEquals("12th", DataFormatter.formatOrdinalDay(12))
        assertEquals("13th", DataFormatter.formatOrdinalDay(13))
        assertEquals("21st", DataFormatter.formatOrdinalDay(21))
        assertEquals("28th", DataFormatter.formatOrdinalDay(28))
    }

    @Test
    fun billingCycleCalculator_computesValidCycleBoundaries() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 6, 14, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val window = BillingCycleCalculator.computeWindow(
            billingStartDay = 10,
            referenceTimeMillis = cal.timeInMillis
        )
        assertTrue(window.startMillis <= cal.timeInMillis)
        assertEquals(window.startMillis, window.previousEndMillis)
        assertTrue(window.previousStartMillis < window.startMillis)
        assertTrue(window.totalCycleDays in 28..31)
        assertTrue(window.daysElapsed in 1..window.totalCycleDays)
    }
}
