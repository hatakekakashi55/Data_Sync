package com.nexussync.datausage.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector

object SystemIcons {

    val ArrowLeft: ImageVector by lazy {
        buildStrokeIcon("ArrowLeft") {
            strokePath {
                moveTo(19f, 12f)
                horizontalLineTo(5f)
                moveTo(11f, 6f)
                lineTo(5f, 12f)
                lineTo(11f, 18f)
            }
        }
    }

    val ChevronRight: ImageVector by lazy {
        buildStrokeIcon("ChevronRight") {
            strokePath {
                moveTo(9f, 6f)
                lineTo(15f, 12f)
                lineTo(9f, 18f)
            }
        }
    }

    val Refresh: ImageVector by lazy {
        buildStrokeIcon("Refresh") {
            strokePath {
                moveTo(20f, 11f)
                arcTo(8f, 8f, 0f, false, false, 5.6f, 7.2f)
                lineTo(4f, 9f)
                moveTo(4f, 4.5f)
                verticalLineTo(9f)
                horizontalLineTo(8.5f)

                moveTo(4f, 13f)
                arcTo(8f, 8f, 0f, false, false, 18.4f, 16.8f)
                lineTo(20f, 15f)
                moveTo(20f, 19.5f)
                verticalLineTo(15f)
                horizontalLineTo(15.5f)
            }
        }
    }

    val Search: ImageVector by lazy {
        buildStrokeIcon("Search") {
            strokePath {
                moveTo(11f, 4f)
                arcTo(7f, 7f, 0f, true, true, 10.99f, 4f)
                close()
                moveTo(16.2f, 16.2f)
                lineTo(20.5f, 20.5f)
            }
        }
    }

    val Close: ImageVector by lazy {
        buildStrokeIcon("Close") {
            strokePath {
                moveTo(6f, 6f)
                lineTo(18f, 18f)
                moveTo(18f, 6f)
                lineTo(6f, 18f)
            }
        }
    }

    val Check: ImageVector by lazy {
        buildStrokeIcon("Check") {
            strokePath(2.2f) {
                moveTo(5f, 12.5f)
                lineTo(9.8f, 17.2f)
                lineTo(19f, 7.5f)
            }
        }
    }

    val ShieldCheck: ImageVector by lazy {
        buildStrokeIcon("ShieldCheck") {
            strokePath {
                moveTo(12f, 3f)
                lineTo(19f, 6f)
                verticalLineTo(11.5f)
                curveTo(19f, 16.2f, 15.9f, 19.8f, 12f, 21f)
                curveTo(8.1f, 19.8f, 5f, 16.2f, 5f, 11.5f)
                verticalLineTo(6f)
                close()
                moveTo(9f, 12.2f)
                lineTo(11.2f, 14.4f)
                lineTo(15.2f, 10f)
            }
        }
    }

    val ShieldAlert: ImageVector by lazy {
        buildStrokeIcon("ShieldAlert") {
            strokePath {
                moveTo(12f, 3f)
                lineTo(19f, 6f)
                verticalLineTo(11.5f)
                curveTo(19f, 16.2f, 15.9f, 19.8f, 12f, 21f)
                curveTo(8.1f, 19.8f, 5f, 16.2f, 5f, 11.5f)
                verticalLineTo(6f)
                close()
                moveTo(12f, 8.5f)
                verticalLineTo(12.8f)
                moveTo(12f, 16f)
                horizontalLineTo(12.02f)
            }
        }
    }

    val Bell: ImageVector by lazy {
        buildStrokeIcon("Bell") {
            strokePath {
                moveTo(6f, 9.5f)
                arcTo(6f, 6f, 0f, false, true, 18f, 9.5f)
                curveTo(18f, 14.5f, 20f, 16.5f, 20f, 16.5f)
                horizontalLineTo(4f)
                curveTo(4f, 16.5f, 6f, 14.5f, 6f, 9.5f)
                close()
                moveTo(10f, 19.5f)
                quadTo(12f, 21.5f, 14f, 19.5f)
            }
        }
    }

    val Power: ImageVector by lazy {
        buildStrokeIcon("Power") {
            strokePath {
                moveTo(12f, 3f)
                verticalLineTo(11f)
                moveTo(17.6f, 6.4f)
                arcTo(8f, 8f, 0f, true, true, 6.4f, 6.4f)
            }
        }
    }

    val Calendar: ImageVector by lazy {
        buildStrokeIcon("Calendar") {
            strokePath {
                moveTo(5f, 5f)
                horizontalLineTo(19f)
                curveTo(20.1f, 5f, 21f, 5.9f, 21f, 7f)
                verticalLineTo(19f)
                curveTo(21f, 20.1f, 20.1f, 21f, 19f, 21f)
                horizontalLineTo(5f)
                curveTo(3.9f, 21f, 3f, 20.1f, 3f, 19f)
                verticalLineTo(7f)
                curveTo(3f, 5.9f, 3.9f, 5f, 5f, 5f)
                close()
                moveTo(16f, 3f)
                verticalLineTo(7f)
                moveTo(8f, 3f)
                verticalLineTo(7f)
                moveTo(3f, 10f)
                horizontalLineTo(21f)
            }
        }
    }

    val HardDrive: ImageVector by lazy {
        buildStrokeIcon("HardDrive") {
            strokePath {
                moveTo(3f, 13f)
                lineTo(5.5f, 5.5f)
                curveTo(5.8f, 4.6f, 6.6f, 4f, 7.5f, 4f)
                horizontalLineTo(16.5f)
                curveTo(17.4f, 4f, 18.2f, 4.6f, 18.5f, 5.5f)
                lineTo(21f, 13f)
                verticalLineTo(18f)
                curveTo(21f, 19.1f, 20.1f, 20f, 19f, 20f)
                horizontalLineTo(5f)
                curveTo(3.9f, 20f, 3f, 19.1f, 3f, 18f)
                close()
                moveTo(3f, 13f)
                horizontalLineTo(21f)
                moveTo(7f, 16.5f)
                horizontalLineTo(7.02f)
                moveTo(11f, 16.5f)
                horizontalLineTo(11.02f)
            }
        }
    }

    val Moon: ImageVector by lazy {
        buildStrokeIcon("Moon") {
            strokePath {
                moveTo(20.2f, 14.5f)
                arcTo(8.5f, 8.5f, 0f, true, true, 9.5f, 3.8f)
                arcTo(6.8f, 6.8f, 0f, false, false, 20.2f, 14.5f)
                close()
            }
        }
    }

    val Sun: ImageVector by lazy {
        buildStrokeIcon("Sun") {
            strokePath {
                moveTo(12f, 8f)
                arcTo(4f, 4f, 0f, true, true, 11.99f, 8f)
                close()
                moveTo(12f, 2.5f)
                verticalLineTo(4.5f)
                moveTo(12f, 19.5f)
                verticalLineTo(21.5f)
                moveTo(4.5f, 12f)
                horizontalLineTo(2.5f)
                moveTo(21.5f, 12f)
                horizontalLineTo(19.5f)
                moveTo(6.3f, 6.3f)
                lineTo(4.9f, 4.9f)
                moveTo(19.1f, 19.1f)
                lineTo(17.7f, 17.7f)
                moveTo(17.7f, 6.3f)
                lineTo(19.1f, 4.9f)
                moveTo(4.9f, 19.1f)
                lineTo(6.3f, 17.7f)
            }
        }
    }

    val Mail: ImageVector by lazy {
        buildStrokeIcon("Mail") {
            strokePath {
                moveTo(4f, 5f)
                horizontalLineTo(20f)
                curveTo(20.8f, 5f, 21.5f, 5.7f, 21.5f, 6.5f)
                verticalLineTo(17.5f)
                curveTo(21.5f, 18.3f, 20.8f, 19f, 20f, 19f)
                horizontalLineTo(4f)
                curveTo(3.2f, 19f, 2.5f, 18.3f, 2.5f, 17.5f)
                verticalLineTo(6.5f)
                curveTo(2.5f, 5.7f, 3.2f, 5f, 4f, 5f)
                close()
                moveTo(3f, 6.5f)
                lineTo(12f, 13f)
                lineTo(21f, 6.5f)
            }
        }
    }

    val Info: ImageVector by lazy {
        buildStrokeIcon("Info") {
            strokePath {
                moveTo(12f, 3f)
                arcTo(9f, 9f, 0f, true, true, 11.99f, 3f)
                close()
                moveTo(12f, 11f)
                verticalLineTo(16.5f)
                moveTo(12f, 7.5f)
                horizontalLineTo(12.02f)
            }
        }
    }

    val ExternalLink: ImageVector by lazy {
        buildStrokeIcon("ExternalLink") {
            strokePath {
                moveTo(14f, 4f)
                horizontalLineTo(20f)
                verticalLineTo(10f)
                moveTo(20f, 4f)
                lineTo(10f, 14f)
                moveTo(18f, 13.5f)
                verticalLineTo(18.5f)
                curveTo(18f, 19.3f, 17.3f, 20f, 16.5f, 20f)
                horizontalLineTo(5.5f)
                curveTo(4.7f, 20f, 4f, 19.3f, 4f, 18.5f)
                verticalLineTo(7.5f)
                curveTo(4f, 6.7f, 4.7f, 6f, 5.5f, 6f)
                horizontalLineTo(10.5f)
            }
        }
    }

    val Layers: ImageVector by lazy {
        buildStrokeIcon("Layers") {
            strokePath {
                moveTo(12f, 3f)
                lineTo(21f, 7.5f)
                lineTo(12f, 12f)
                lineTo(3f, 7.5f)
                close()
                moveTo(3f, 12.5f)
                lineTo(12f, 17f)
                lineTo(21f, 12.5f)
                moveTo(3f, 16.5f)
                lineTo(12f, 21f)
                lineTo(21f, 16.5f)
            }
        }
    }

    val Cpu: ImageVector by lazy {
        buildStrokeIcon("Cpu") {
            strokePath {
                moveTo(7f, 5f)
                horizontalLineTo(17f)
                curveTo(18.1f, 5f, 19f, 5.9f, 19f, 7f)
                verticalLineTo(17f)
                curveTo(19f, 18.1f, 18.1f, 19f, 17f, 19f)
                horizontalLineTo(7f)
                curveTo(5.9f, 19f, 5f, 18.1f, 5f, 17f)
                verticalLineTo(7f)
                curveTo(5f, 5.9f, 5.9f, 5f, 7f, 5f)
                close()
                moveTo(9.5f, 9.5f)
                horizontalLineTo(14.5f)
                verticalLineTo(14.5f)
                horizontalLineTo(9.5f)
                close()
                moveTo(9f, 2.5f)
                verticalLineTo(5f)
                moveTo(15f, 2.5f)
                verticalLineTo(5f)
                moveTo(9f, 19f)
                verticalLineTo(21.5f)
                moveTo(15f, 19f)
                verticalLineTo(21.5f)
                moveTo(2.5f, 9f)
                horizontalLineTo(5f)
                moveTo(2.5f, 15f)
                horizontalLineTo(5f)
                moveTo(19f, 9f)
                horizontalLineTo(21.5f)
                moveTo(19f, 15f)
                horizontalLineTo(21.5f)
            }
        }
    }
}
