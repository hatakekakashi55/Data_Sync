package com.nexussync.datausage.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.nexussync.datausage.domain.model.ThemeMode

private val MidnightExtraColors = DataSyncExtraColors(
    isDark = true,
    background = MidnightBg,
    surface = MidnightSurface,
    cardBackground = MidnightCard,
    cardElevated = MidnightCardElevated,
    cardBorder = MidnightBorder,
    divider = Color(0xFF1E2B42),
    accent = TealElectric,
    accentContainer = Color(0xFF132E35),
    onAccent = Color(0xFF042F2E),
    textPrimary = MidnightTextPrimary,
    textSecondary = MidnightTextSecondary,
    textMuted = MidnightTextMuted,
    mobileAccent = MobileIndigoDark,
    mobileContainer = Color(0xFF1D2242),
    wifiAccent = WifiCyanDark,
    wifiContainer = Color(0xFF132A42),
    downloadAccent = TealElectric,
    uploadAccent = UploadVioletDark,
    statusHealthy = StatusHealthyDark,
    statusWarning = StatusWarningDark,
    statusCritical = StatusCriticalDark,
    chartTrack = Color(0xFF1C2940),
    chartInactiveBar = Color(0xFF283B5B)
)

private val AmoledExtraColors = MidnightExtraColors.copy(
    background = AmoledBg,
    surface = AmoledSurface,
    cardBackground = AmoledCard,
    cardElevated = AmoledCardElevated,
    cardBorder = AmoledBorder,
    divider = Color(0xFF162033),
    chartTrack = Color(0xFF162033),
    chartInactiveBar = Color(0xFF22334F)
)

private val DaylightExtraColors = DataSyncExtraColors(
    isDark = false,
    background = LightBg,
    surface = LightSurface,
    cardBackground = LightCard,
    cardElevated = LightCardElevated,
    cardBorder = LightBorder,
    divider = Color(0xFFE5ECF6),
    accent = TealDeep,
    accentContainer = Color(0xFFCCFBF1),
    onAccent = Color(0xFFFFFFFF),
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    mobileAccent = MobileIndigoLight,
    mobileContainer = Color(0xFFE0E7FF),
    wifiAccent = WifiCyanLight,
    wifiContainer = Color(0xFFE0F2FE),
    downloadAccent = TealDeep,
    uploadAccent = UploadVioletLight,
    statusHealthy = StatusHealthyLight,
    statusWarning = StatusWarningLight,
    statusCritical = StatusCriticalLight,
    chartTrack = Color(0xFFE2E8F0),
    chartInactiveBar = Color(0xFFCBD5E1)
)

val LocalDataSyncColors = staticCompositionLocalOf { MidnightExtraColors }

object DataSyncTheme {
    val colors: DataSyncExtraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalDataSyncColors.current
}

@Composable
fun DataSyncTheme(
    themeMode: ThemeMode = ThemeMode.MIDNIGHT,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val extraColors = when (themeMode) {
        ThemeMode.MIDNIGHT -> MidnightExtraColors
        ThemeMode.AMOLED -> AmoledExtraColors
        ThemeMode.LIGHT -> DaylightExtraColors
        ThemeMode.SYSTEM -> if (systemDark) MidnightExtraColors else DaylightExtraColors
    }

    val colorScheme = if (extraColors.isDark) {
        darkColorScheme(
            primary = extraColors.accent,
            onPrimary = extraColors.onAccent,
            primaryContainer = extraColors.accentContainer,
            onPrimaryContainer = extraColors.accent,
            secondary = extraColors.wifiAccent,
            tertiary = extraColors.mobileAccent,
            background = extraColors.background,
            onBackground = extraColors.textPrimary,
            surface = extraColors.surface,
            onSurface = extraColors.textPrimary,
            surfaceVariant = extraColors.cardBackground,
            onSurfaceVariant = extraColors.textSecondary,
            outline = extraColors.cardBorder,
            error = extraColors.statusCritical
        )
    } else {
        lightColorScheme(
            primary = extraColors.accent,
            onPrimary = extraColors.onAccent,
            primaryContainer = extraColors.accentContainer,
            onPrimaryContainer = extraColors.accent,
            secondary = extraColors.wifiAccent,
            tertiary = extraColors.mobileAccent,
            background = extraColors.background,
            onBackground = extraColors.textPrimary,
            surface = extraColors.surface,
            onSurface = extraColors.textPrimary,
            surfaceVariant = extraColors.cardBackground,
            onSurfaceVariant = extraColors.textSecondary,
            outline = extraColors.cardBorder,
            error = extraColors.statusCritical
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !extraColors.isDark
                controller.isAppearanceLightNavigationBars = !extraColors.isDark
            }
        }
    }

    CompositionLocalProvider(LocalDataSyncColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DataSyncTypography,
            content = content
        )
    }
}
