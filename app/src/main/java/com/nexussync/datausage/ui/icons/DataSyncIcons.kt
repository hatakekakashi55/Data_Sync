package com.nexussync.datausage.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal fun buildStrokeIcon(
    name: String,
    strokeWidth: Float = 2f,
    block: ImageVector.Builder.() -> Unit
): ImageVector {
    return ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply(block).build()
}

internal fun ImageVector.Builder.strokePath(
    strokeWidth: Float = 2f,
    pathBlock: PathBuilder.() -> Unit
) {
    path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = strokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = pathBlock
    )
}

object DataSyncIcons {

    // Brand Mark: Dual precision vertical transfer arrows
    val DataSyncMark: ImageVector by lazy {
        buildStrokeIcon("DataSyncMark") {
            strokePath(2.2f) {
                moveTo(8f, 4f)
                verticalLineTo(20f)
                moveTo(4f, 16f)
                lineTo(8f, 20f)
                lineTo(12f, 16f)
            }
            strokePath(2.2f) {
                moveTo(16f, 20f)
                verticalLineTo(4f)
                moveTo(12f, 8f)
                lineTo(16f, 4f)
                lineTo(20f, 8f)
            }
        }
    }

    // Bottom Nav 1: Overview Dashboard Grid
    val Overview: ImageVector by lazy {
        buildStrokeIcon("Overview") {
            strokePath {
                moveTo(4f, 4f)
                horizontalLineTo(10f)
                verticalLineTo(11f)
                horizontalLineTo(4f)
                close()
            }
            strokePath {
                moveTo(14f, 4f)
                horizontalLineTo(20f)
                verticalLineTo(8f)
                horizontalLineTo(14f)
                close()
            }
            strokePath {
                moveTo(14f, 12f)
                horizontalLineTo(20f)
                verticalLineTo(20f)
                horizontalLineTo(14f)
                close()
            }
            strokePath {
                moveTo(4f, 15f)
                horizontalLineTo(10f)
                verticalLineTo(20f)
                horizontalLineTo(4f)
                close()
            }
        }
    }

    // Bottom Nav 2: Analytics Bar Chart
    val Analytics: ImageVector by lazy {
        buildStrokeIcon("Analytics") {
            strokePath {
                moveTo(4f, 20f)
                verticalLineTo(13f)
                moveTo(9.3f, 20f)
                verticalLineTo(8f)
                moveTo(14.7f, 20f)
                verticalLineTo(11f)
                moveTo(20f, 20f)
                verticalLineTo(4f)
            }
        }
    }

    // Bottom Nav 3: Applications Grid
    val Apps: ImageVector by lazy {
        buildStrokeIcon("Apps") {
            strokePath {
                moveTo(4f, 5.5f)
                curveTo(4f, 4.67f, 4.67f, 4f, 5.5f, 4f)
                horizontalLineTo(9.5f)
                curveTo(10.33f, 4f, 11f, 4.67f, 11f, 5.5f)
                verticalLineTo(9.5f)
                curveTo(11f, 10.33f, 10.33f, 11f, 9.5f, 11f)
                horizontalLineTo(5.5f)
                curveTo(4.67f, 11f, 4f, 10.33f, 4f, 9.5f)
                close()
            }
            strokePath {
                moveTo(13f, 5.5f)
                curveTo(13f, 4.67f, 13.67f, 4f, 14.5f, 4f)
                horizontalLineTo(18.5f)
                curveTo(19.33f, 4f, 20f, 4.67f, 20f, 5.5f)
                verticalLineTo(9.5f)
                curveTo(20f, 10.33f, 19.33f, 11f, 18.5f, 11f)
                horizontalLineTo(14.5f)
                curveTo(13.67f, 11f, 13f, 10.33f, 13f, 9.5f)
                close()
            }
            strokePath {
                moveTo(4f, 14.5f)
                curveTo(4f, 13.67f, 4.67f, 13f, 5.5f, 13f)
                horizontalLineTo(9.5f)
                curveTo(10.33f, 13f, 11f, 13.67f, 11f, 14.5f)
                verticalLineTo(18.5f)
                curveTo(11f, 19.33f, 10.33f, 20f, 9.5f, 20f)
                horizontalLineTo(5.5f)
                curveTo(4.67f, 20f, 4f, 19.33f, 4f, 18.5f)
                close()
            }
            strokePath {
                moveTo(13f, 14.5f)
                curveTo(13f, 13.67f, 13.67f, 13f, 14.5f, 13f)
                horizontalLineTo(18.5f)
                curveTo(19.33f, 13f, 20f, 13.67f, 20f, 14.5f)
                verticalLineTo(18.5f)
                curveTo(20f, 19.33f, 19.33f, 20f, 18.5f, 20f)
                horizontalLineTo(14.5f)
                curveTo(13.67f, 20f, 13f, 19.33f, 13f, 18.5f)
                close()
            }
        }
    }

    // Bottom Nav 4: Precision Sliders / Settings
    val Settings: ImageVector by lazy {
        buildStrokeIcon("Settings") {
            strokePath {
                moveTo(4f, 6f)
                horizontalLineTo(10f)
                moveTo(14f, 6f)
                horizontalLineTo(20f)
                moveTo(12f, 4f)
                verticalLineTo(8f)

                moveTo(4f, 12f)
                horizontalLineTo(14f)
                moveTo(18f, 12f)
                horizontalLineTo(20f)
                moveTo(16f, 10f)
                verticalLineTo(14f)

                moveTo(4f, 18f)
                horizontalLineTo(6f)
                moveTo(10f, 18f)
                horizontalLineTo(20f)
                moveTo(8f, 16f)
                verticalLineTo(20f)
            }
        }
    }

    // Wi-Fi Signal Arcs
    val Wifi: ImageVector by lazy {
        buildStrokeIcon("Wifi") {
            strokePath {
                moveTo(2.5f, 9f)
                quadTo(12f, 1.5f, 21.5f, 9f)
                moveTo(5.8f, 12.5f)
                quadTo(12f, 7.2f, 18.2f, 12.5f)
                moveTo(9.2f, 16f)
                quadTo(12f, 13.5f, 14.8f, 16f)
                moveTo(12f, 19.5f)
                horizontalLineTo(12.02f)
            }
        }
    }

    // Cellular Signal Bars / Smartphone
    val Cellular: ImageVector by lazy {
        buildStrokeIcon("Cellular") {
            strokePath {
                moveTo(4f, 20f)
                verticalLineTo(16f)
                moveTo(9.3f, 20f)
                verticalLineTo(12.5f)
                moveTo(14.6f, 20f)
                verticalLineTo(8.5f)
                moveTo(20f, 20f)
                verticalLineTo(4f)
            }
        }
    }

    // Smartphone Device
    val Smartphone: ImageVector by lazy {
        buildStrokeIcon("Smartphone") {
            strokePath {
                moveTo(7f, 2.5f)
                horizontalLineTo(17f)
                curveTo(18.1f, 2.5f, 19f, 3.4f, 19f, 4.5f)
                verticalLineTo(19.5f)
                curveTo(19f, 20.6f, 18.1f, 21.5f, 17f, 21.5f)
                horizontalLineTo(7f)
                curveTo(5.9f, 21.5f, 5f, 20.6f, 5f, 19.5f)
                verticalLineTo(4.5f)
                curveTo(5f, 3.4f, 5.9f, 2.5f, 7f, 2.5f)
                close()
                moveTo(11f, 18f)
                horizontalLineTo(13f)
            }
        }
    }

    // Download Arrow
    val Download: ImageVector by lazy {
        buildStrokeIcon("Download") {
            strokePath {
                moveTo(12f, 4f)
                verticalLineTo(16f)
                moveTo(7.5f, 11.5f)
                lineTo(12f, 16f)
                lineTo(16.5f, 11.5f)
                moveTo(5f, 20f)
                horizontalLineTo(19f)
            }
        }
    }

    // Upload Arrow
    val Upload: ImageVector by lazy {
        buildStrokeIcon("Upload") {
            strokePath {
                moveTo(12f, 16f)
                verticalLineTo(4f)
                moveTo(7.5f, 8.5f)
                lineTo(12f, 4f)
                lineTo(16.5f, 8.5f)
                moveTo(5f, 20f)
                horizontalLineTo(19f)
            }
        }
    }

    // Compact Arrow Down
    val ArrowDown: ImageVector by lazy {
        buildStrokeIcon("ArrowDown") {
            strokePath(2.2f) {
                moveTo(12f, 5f)
                verticalLineTo(19f)
                moveTo(6.5f, 13.5f)
                lineTo(12f, 19f)
                lineTo(17.5f, 13.5f)
            }
        }
    }

    // Compact Arrow Up
    val ArrowUp: ImageVector by lazy {
        buildStrokeIcon("ArrowUp") {
            strokePath(2.2f) {
                moveTo(12f, 19f)
                verticalLineTo(5f)
                moveTo(6.5f, 10.5f)
                lineTo(12f, 5f)
                lineTo(17.5f, 10.5f)
            }
        }
    }

    // Live Pulse / Telemetry Wave
    val Pulse: ImageVector by lazy {
        buildStrokeIcon("Pulse") {
            strokePath {
                moveTo(2f, 12f)
                horizontalLineTo(6f)
                lineTo(9f, 4f)
                lineTo(15f, 20f)
                lineTo(18f, 12f)
                horizontalLineTo(22f)
            }
        }
    }

    // Speedometer Gauge
    val Gauge: ImageVector by lazy {
        buildStrokeIcon("Gauge") {
            strokePath {
                moveTo(4.2f, 18f)
                arcTo(9f, 9f, 0f, true, true, 19.8f, 18f)
                moveTo(12f, 14f)
                lineTo(16f, 9.5f)
            }
        }
    }
}
