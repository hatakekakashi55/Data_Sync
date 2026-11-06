# Data Sync — Native Kotlin & Jetpack Compose Edition

**Data Sync** (`com.nexussync.datausage`) is a native Android network telemetry, real-time throughput monitor, and data quota management application built entirely in **Kotlin** and **Jetpack Compose (Material 3)**.

All network accounting runs 100% locally on-device via Android's `NetworkStatsManager`, `TrafficStats`, and `ConnectivityManager` APIs with zero WebView overhead and zero external data collection.

---

## Core Capabilities

- **Precision Daily Quota Arc Gauge**: Custom Canvas 260-degree arc gauge with tabular byte counters, dynamic threshold coloring (healthy, warning, exceeded), and remaining allowance readouts.
- **Real-Time Throughput & Sparkline Wave**: Live per-second download and upload speeds (`TrafficStats`), peak throughput tracking, session counters, and a smooth cubic-bezier sparkline canvas.
- **Cellular & Wi-Fi Interface Split**: Granular Rx/Tx accounting across mobile data and Wi-Fi networks, plus active transport, metered status, and VPN detection.
- **Interactive 30-Day Historical Analytics**: Tap-to-inspect 30-day bar chart with Combined, Mobile, and Wi-Fi series filters, 30-day daily averages, peak usage identification, donut ring distribution charts, and current-vs-previous billing cycle comparisons.
- **Per-Application Traffic Inspector**: Queries UID-level cellular and Wi-Fi usage across Today, Last 7 Days, or the active Billing Cycle, complete with search, system-app filtering, `LruCache` native icon rendering, and an interactive bottom sheet linking directly to Android's system background data controls.
- **Persistent Status Bar Telemetry & Quota Alerts**: Android 14/15-compliant `specialUse` foreground service displaying live speed and daily quota progress in the notification drawer, alongside configurable daily threshold alerts and boot auto-start.
- **Multi-Theme Enterprise Design System**: Supports **Midnight Slate**, **Pure AMOLED**, **Clean Daylight**, and **System Default** themes, engineered exclusively with custom geometric `ImageVector` iconography.

---

## Project Architecture

The codebase follows **Clean Architecture + MVVM + Unidirectional Data Flow (`StateFlow`)**:

```text
app/src/main/java/com/nexussync/datausage/
├── DataSyncApplication.kt
├── MainActivity.kt
├── di/
│   └── AppContainer.kt
├── domain/
│   ├── model/
│   │   ├── AppDataUsage.kt
│   │   ├── DailyUsageEntry.kt
│   │   ├── DataUsageSummary.kt
│   │   ├── LiveSpeedSnapshot.kt
│   │   ├── NetworkConnectionInfo.kt
│   │   └── UserPreferences.kt
│   └── repository/
│       ├── PreferencesRepository.kt
│       └── UsageRepository.kt
├── data/
│   ├── local/
│   │   └── PreferencesRepositoryImpl.kt
│   ├── repository/
│   │   └── UsageRepositoryImpl.kt
│   └── source/
│       ├── AppUsageDataSource.kt
│       ├── NetworkStatsDataSource.kt
│       ├── NetworkTelemetryDataSource.kt
│       └── PermissionChecker.kt
├── service/
│   ├── DataUsageForegroundService.kt
│   ├── NotificationHelper.kt
│   └── ServiceController.kt
├── receiver/
│   └── BootCompletedReceiver.kt
├── ui/
│   ├── components/
│   │   ├── DistributionRingChart.kt
│   │   ├── LiveSpeedSparkline.kt
│   │   ├── PermissionBanner.kt
│   │   ├── QuotaArcGauge.kt
│   │   ├── SettingRowItem.kt
│   │   ├── SurfaceCard.kt
│   │   └── UsageBarChart.kt
│   ├── icons/
│   │   ├── DataSyncIcons.kt
│   │   └── SystemIcons.kt
│   ├── navigation/
│   │   ├── DataSyncNavHost.kt
│   │   └── ScreenDestination.kt
│   ├── screens/
│   │   ├── about/AboutScreen.kt
│   │   ├── analytics/
│   │   │   ├── AnalyticsScreen.kt
│   │   │   ├── AnalyticsUiState.kt
│   │   │   └── AnalyticsViewModel.kt
│   │   ├── apps/
│   │   │   ├── AppDetailSheet.kt
│   │   │   ├── AppsScreen.kt
│   │   │   ├── AppsUiState.kt
│   │   │   └── AppsViewModel.kt
│   │   ├── dashboard/
│   │   │   ├── DashboardScreen.kt
│   │   │   ├── DashboardUiState.kt
│   │   │   └── DashboardViewModel.kt
│   │   └── settings/
│   │       ├── BillingDayDialog.kt
│   │       ├── SettingsScreen.kt
│   │       └── SettingsViewModel.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── util/
    ├── DataFormatter.kt
    ├── DrawableIconConverter.kt
    └── SystemSettingsNavigator.kt
```

---

## Building & Running

### Prerequisites
- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 17** (bundled with Android Studio)
- **Android SDK 35** (`minSdk = 24`, `targetSdk = 35`)

### Build via CLI
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Build optimized release APK
./gradlew assembleRelease
```

The generated debug APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`
