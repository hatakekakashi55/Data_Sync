package com.nexussync.datausage.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Midnight Slate Palette
val MidnightBg = Color(0xFF090D16)
val MidnightSurface = Color(0xFF0F1726)
val MidnightCard = Color(0xFF141E30)
val MidnightCardElevated = Color(0xFF1B2840)
val MidnightBorder = Color(0xFF23324D)
val MidnightTextPrimary = Color(0xFFF8FAFC)
val MidnightTextSecondary = Color(0xFF94A3B8)
val MidnightTextMuted = Color(0xFF64748B)

// Pure AMOLED Palette
val AmoledBg = Color(0xFF000000)
val AmoledSurface = Color(0xFF070B12)
val AmoledCard = Color(0xFF0C121E)
val AmoledCardElevated = Color(0xFF141D2F)
val AmoledBorder = Color(0xFF1C2940)

// Clean Daylight Palette
val LightBg = Color(0xFFF3F6FB)
val LightSurface = Color(0xFFFFFFFF)
val LightCard = Color(0xFFFFFFFF)
val LightCardElevated = Color(0xFFEEF3FA)
val LightBorder = Color(0xFFDCE4F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF64748B)

// Accents & Semantic Telemetry Colors
val TealElectric = Color(0xFF2DD4BF)
val TealDeep = Color(0xFF0D9488)
val MobileIndigoDark = Color(0xFF818CF8)
val MobileIndigoLight = Color(0xFF4F46E5)
val WifiCyanDark = Color(0xFF38BDF8)
val WifiCyanLight = Color(0xFF0284C7)
val UploadVioletDark = Color(0xFFA78BFA)
val UploadVioletLight = Color(0xFF7C3AED)

val StatusHealthyDark = Color(0xFF34D399)
val StatusHealthyLight = Color(0xFF059669)
val StatusWarningDark = Color(0xFFFBBF24)
val StatusWarningLight = Color(0xFFD97706)
val StatusCriticalDark = Color(0xFFFB7185)
val StatusCriticalLight = Color(0xFFE11D48)

@Immutable
data class DataSyncExtraColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val cardElevated: Color,
    val cardBorder: Color,
    val divider: Color,
    val accent: Color,
    val accentContainer: Color,
    val onAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val mobileAccent: Color,
    val mobileContainer: Color,
    val wifiAccent: Color,
    val wifiContainer: Color,
    val downloadAccent: Color,
    val uploadAccent: Color,
    val statusHealthy: Color,
    val statusWarning: Color,
    val statusCritical: Color,
    val chartTrack: Color,
    val chartInactiveBar: Color
)
