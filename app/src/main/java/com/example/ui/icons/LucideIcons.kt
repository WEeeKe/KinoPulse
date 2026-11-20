package com.example.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Rounded stroke Lucide-style 24dp vector icons (StrokeCap.Round, StrokeJoin.Round, 1.85dp).
 */
object LucideIcons {
    private val strokeBrush = SolidColor(Color.Black)

    private fun lucideIcon(
        name: String,
        strokeWidth: Float = 1.85f,
        block: ImageVector.Builder.(Float) -> Unit
    ): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply { block(strokeWidth) }.build()
    }

    private fun ImageVector.Builder.strokePath(
        strokeWidth: Float = 1.85f,
        fill: Boolean = false,
        pathBuilder: PathBuilder.() -> Unit
    ) {
        path(
            fill = if (fill) strokeBrush else null,
            stroke = strokeBrush,
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero,
            pathBuilder = pathBuilder
        )
    }

    val Home: ImageVector by lazy {
        lucideIcon("LucideHome") { w ->
            strokePath(w) {
                moveTo(3f, 10.5f)
                lineTo(12f, 3f)
                lineTo(21f, 10.5f)
                verticalLineTo(19f)
                arcTo(2f, 2f, 0f, false, true, 19f, 21f)
                horizontalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 3f, 19f)
                close()
            }
            strokePath(w) {
                moveTo(9f, 21f)
                verticalLineTo(13f)
                arcTo(1f, 1f, 0f, false, true, 10f, 12f)
                horizontalLineTo(14f)
                arcTo(1f, 1f, 0f, false, true, 15f, 13f)
                verticalLineTo(21f)
            }
        }
    }

    val Search: ImageVector by lazy {
        lucideIcon("LucideSearch") { w ->
            strokePath(w) {
                moveTo(11f, 3f)
                arcTo(8f, 8f, 0f, true, false, 11f, 19f)
                arcTo(8f, 8f, 0f, true, false, 11f, 3f)
                close()
            }
            strokePath(w) {
                moveTo(21f, 21f)
                lineTo(16.65f, 16.65f)
            }
        }
    }

    val Bookmark: ImageVector by lazy {
        lucideIcon("LucideBookmark") { w ->
            strokePath(w) {
                moveTo(19f, 21f)
                lineTo(12f, 16.5f)
                lineTo(5f, 21f)
                verticalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 7f, 3f)
                horizontalLineTo(17f)
                arcTo(2f, 2f, 0f, false, true, 19f, 5f)
                close()
            }
        }
    }

    val BookmarkFilled: ImageVector by lazy {
        lucideIcon("LucideBookmarkFilled") { w ->
            strokePath(w, fill = true) {
                moveTo(19f, 21f)
                lineTo(12f, 16.5f)
                lineTo(5f, 21f)
                verticalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 7f, 3f)
                horizontalLineTo(17f)
                arcTo(2f, 2f, 0f, false, true, 19f, 5f)
                close()
            }
        }
    }

    val MessageCircle: ImageVector by lazy {
        lucideIcon("LucideMessageCircle") { w ->
            strokePath(w) {
                moveTo(7.9f, 20f)
                arcTo(9f, 9f, 0f, true, false, 4f, 16.1f)
                lineTo(2f, 22f)
                close()
            }
        }
    }

    val User: ImageVector by lazy {
        lucideIcon("LucideUser") { w ->
            strokePath(w) {
                moveTo(12f, 4f)
                arcTo(4f, 4f, 0f, true, false, 12f, 12f)
                arcTo(4f, 4f, 0f, true, false, 12f, 4f)
                close()
            }
            strokePath(w) {
                moveTo(20f, 21f)
                arcTo(8f, 8f, 0f, false, false, 4f, 21f)
            }
        }
    }

    val Play: ImageVector by lazy {
        lucideIcon("LucidePlay") { w ->
            strokePath(w, fill = true) {
                moveTo(6.5f, 4.5f)
                lineTo(19.5f, 12f)
                lineTo(6.5f, 19.5f)
                close()
            }
        }
    }

    val Star: ImageVector by lazy {
        lucideIcon("LucideStar") { w ->
            strokePath(w, fill = true) {
                moveTo(12f, 2.5f)
                lineTo(14.9f, 8.4f)
                lineTo(21.4f, 9.3f)
                lineTo(16.7f, 13.9f)
                lineTo(17.8f, 20.4f)
                lineTo(12f, 17.3f)
                lineTo(6.2f, 20.4f)
                lineTo(7.3f, 13.9f)
                lineTo(2.6f, 9.3f)
                lineTo(9.1f, 8.4f)
                close()
            }
        }
    }

    val Moon: ImageVector by lazy {
        lucideIcon("LucideMoon") { w ->
            strokePath(w) {
                moveTo(12f, 3f)
                arcTo(6f, 6f, 0f, false, false, 21f, 12f)
                arcTo(9f, 9f, 0f, true, true, 12f, 3f)
                close()
            }
        }
    }

    val Sun: ImageVector by lazy {
        lucideIcon("LucideSun") { w ->
            strokePath(w) {
                moveTo(12f, 8f)
                arcTo(4f, 4f, 0f, true, false, 12f, 16f)
                arcTo(4f, 4f, 0f, true, false, 12f, 8f)
                close()
            }
            strokePath(w) {
                moveTo(12f, 2f); verticalLineTo(4f)
                moveTo(12f, 20f); verticalLineTo(22f)
                moveTo(4.93f, 4.93f); lineTo(6.34f, 6.34f)
                moveTo(17.66f, 17.66f); lineTo(19.07f, 19.07f)
                moveTo(2f, 12f); horizontalLineTo(4f)
                moveTo(20f, 12f); horizontalLineTo(22f)
                moveTo(6.34f, 17.66f); lineTo(4.93f, 19.07f)
                moveTo(19.07f, 4.93f); lineTo(17.66f, 6.34f)
            }
        }
    }

    val Bell: ImageVector by lazy {
        lucideIcon("LucideBell") { w ->
            strokePath(w) {
                moveTo(6f, 8f)
                arcTo(6f, 6f, 0f, false, true, 18f, 8f)
                curveTo(18f, 15f, 21f, 17f, 21f, 17f)
                horizontalLineTo(3f)
                reflectiveCurveTo(6f, 15f, 6f, 8f)
            }
            strokePath(w) {
                moveTo(10.3f, 21f)
                arcTo(1.94f, 1.94f, 0f, false, false, 13.7f, 21f)
            }
        }
    }

    val SlidersHorizontal: ImageVector by lazy {
        lucideIcon("LucideSlidersHorizontal") { w ->
            strokePath(w) {
                moveTo(21f, 4f); horizontalLineTo(14f)
                moveTo(10f, 4f); horizontalLineTo(3f)
                moveTo(21f, 12f); horizontalLineTo(12f)
                moveTo(8f, 12f); horizontalLineTo(3f)
                moveTo(21f, 20f); horizontalLineTo(16f)
                moveTo(12f, 20f); horizontalLineTo(3f)
                moveTo(14f, 2f); verticalLineTo(6f)
                moveTo(8f, 10f); verticalLineTo(14f)
                moveTo(16f, 18f); verticalLineTo(22f)
            }
        }
    }

    val ArrowUpDown: ImageVector by lazy {
        lucideIcon("LucideArrowUpDown") { w ->
            strokePath(w) {
                moveTo(21f, 16f); lineTo(17f, 20f); lineTo(13f, 16f)
                moveTo(17f, 20f); verticalLineTo(4f)
                moveTo(3f, 8f); lineTo(7f, 4f); lineTo(11f, 8f)
                moveTo(7f, 4f); verticalLineTo(20f)
            }
        }
    }

    val LayoutGrid: ImageVector by lazy {
        lucideIcon("LucideLayoutGrid") { w ->
            strokePath(w) {
                moveTo(4f, 4f); horizontalLineTo(10f); verticalLineTo(10f); horizontalLineTo(4f); close()
                moveTo(14f, 4f); horizontalLineTo(20f); verticalLineTo(10f); horizontalLineTo(14f); close()
                moveTo(14f, 14f); horizontalLineTo(20f); verticalLineTo(20f); horizontalLineTo(14f); close()
                moveTo(4f, 14f); horizontalLineTo(10f); verticalLineTo(20f); horizontalLineTo(4f); close()
            }
        }
    }

    val Grid3x3: ImageVector by lazy {
        lucideIcon("LucideGrid3x3") { w ->
            strokePath(w) {
                moveTo(4f, 4f); horizontalLineTo(20f); verticalLineTo(20f); horizontalLineTo(4f); close()
                moveTo(9.33f, 4f); verticalLineTo(20f)
                moveTo(14.66f, 4f); verticalLineTo(20f)
                moveTo(4f, 9.33f); horizontalLineTo(20f)
                moveTo(4f, 14.66f); horizontalLineTo(20f)
            }
        }
    }

    val ListIcon: ImageVector by lazy {
        lucideIcon("LucideList") { w ->
            strokePath(w) {
                moveTo(8f, 6f); horizontalLineTo(21f)
                moveTo(8f, 12f); horizontalLineTo(21f)
                moveTo(8f, 18f); horizontalLineTo(21f)
                moveTo(3f, 6f); horizontalLineTo(3.01f)
                moveTo(3f, 12f); horizontalLineTo(3.01f)
                moveTo(3f, 18f); horizontalLineTo(3.01f)
            }
        }
    }

    val Download: ImageVector by lazy {
        lucideIcon("LucideDownload") { w ->
            strokePath(w) {
                moveTo(21f, 15f)
                verticalLineTo(19f)
                arcTo(2f, 2f, 0f, false, true, 19f, 21f)
                horizontalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 3f, 19f)
                verticalLineTo(15f)
            }
            strokePath(w) {
                moveTo(7f, 10f); lineTo(12f, 15f); lineTo(17f, 10f)
                moveTo(12f, 15f); verticalLineTo(3f)
            }
        }
    }

    val CheckCircle: ImageVector by lazy {
        lucideIcon("LucideCheckCircle") { w ->
            strokePath(w) {
                moveTo(22f, 12f)
                arcTo(10f, 10f, 0f, true, true, 12f, 2f)
                arcTo(10f, 10f, 0f, false, true, 22f, 12f)
            }
            strokePath(w) {
                moveTo(8.5f, 12.5f); lineTo(11f, 15f); lineTo(16f, 9.5f)
            }
        }
    }

    val Trash: ImageVector by lazy {
        lucideIcon("LucideTrash") { w ->
            strokePath(w) {
                moveTo(3f, 6f); horizontalLineTo(21f)
                moveTo(19f, 6f); verticalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 17f, 22f)
                horizontalLineTo(7f)
                arcTo(2f, 2f, 0f, false, true, 5f, 20f)
                verticalLineTo(6f)
                moveTo(8f, 6f); verticalLineTo(4f)
                arcTo(2f, 2f, 0f, false, true, 10f, 2f)
                horizontalLineTo(14f)
                arcTo(2f, 2f, 0f, false, true, 16f, 4f)
                verticalLineTo(6f)
            }
        }
    }

    val ChevronLeft: ImageVector by lazy {
        lucideIcon("LucideChevronLeft") { w ->
            strokePath(w) {
                moveTo(15f, 18f); lineTo(9f, 12f); lineTo(15f, 6f)
            }
        }
    }

    val X: ImageVector by lazy {
        lucideIcon("LucideX") { w ->
            strokePath(w) {
                moveTo(18f, 6f); lineTo(6f, 18f)
                moveTo(6f, 6f); lineTo(18f, 18f)
            }
        }
    }

    val WifiOff: ImageVector by lazy {
        lucideIcon("LucideWifiOff") { w ->
            strokePath(w) {
                moveTo(2f, 2f); lineTo(22f, 22f)
                moveTo(8.5f, 16.5f); arcTo(5f, 5f, 0f, false, true, 15.5f, 16.5f)
                moveTo(5f, 12.8f); arcTo(10f, 10f, 0f, false, true, 10.17f, 10.17f)
                moveTo(16.72f, 11.06f); arcTo(10f, 10f, 0f, false, true, 19f, 12.8f)
                moveTo(12f, 20f); horizontalLineTo(12.01f)
            }
        }
    }

    val RefreshCw: ImageVector by lazy {
        lucideIcon("LucideRefreshCw") { w ->
            strokePath(w) {
                moveTo(21f, 12f)
                arcTo(9f, 9f, 0f, false, false, 6f, 5.3f)
                lineTo(3f, 8f)
                moveTo(3f, 3f); verticalLineTo(8f); horizontalLineTo(8f)
                moveTo(3f, 12f)
                arcTo(9f, 9f, 0f, false, false, 18f, 18.7f)
                lineTo(21f, 16f)
                moveTo(21f, 21f); verticalLineTo(16f); horizontalLineTo(16f)
            }
        }
    }

    val Send: ImageVector by lazy {
        lucideIcon("LucideSend") { w ->
            strokePath(w) {
                moveTo(22f, 2f); lineTo(11f, 13f)
                moveTo(22f, 2f); lineTo(15f, 22f); lineTo(11f, 13f); lineTo(2f, 9f); close()
            }
        }
    }

    val Heart: ImageVector by lazy {
        lucideIcon("LucideHeart") { w ->
            strokePath(w) {
                moveTo(19f, 14f)
                curveTo(20.49f, 12.54f, 22f, 10.79f, 22f, 8.5f)
                arcTo(5.5f, 5.5f, 0f, false, false, 16.5f, 3f)
                curveTo(14.74f, 3f, 13.5f, 3.5f, 12f, 5f)
                curveTo(10.5f, 3.5f, 9.26f, 3f, 7.5f, 3f)
                arcTo(5.5f, 5.5f, 0f, false, false, 2f, 8.5f)
                curveTo(2f, 10.8f, 3.5f, 12.55f, 5f, 14f)
                lineTo(12f, 21f)
                close()
            }
        }
    }

    val HeartFilled: ImageVector by lazy {
        lucideIcon("LucideHeartFilled") { w ->
            strokePath(w, fill = true) {
                moveTo(19f, 14f)
                curveTo(20.49f, 12.54f, 22f, 10.79f, 22f, 8.5f)
                arcTo(5.5f, 5.5f, 0f, false, false, 16.5f, 3f)
                curveTo(14.74f, 3f, 13.5f, 3.5f, 12f, 5f)
                curveTo(10.5f, 3.5f, 9.26f, 3f, 7.5f, 3f)
                arcTo(5.5f, 5.5f, 0f, false, false, 2f, 8.5f)
                curveTo(2f, 10.8f, 3.5f, 12.55f, 5f, 14f)
                lineTo(12f, 21f)
                close()
            }
        }
    }

    val Reply: ImageVector by lazy {
        lucideIcon("LucideReply") { w ->
            strokePath(w) {
                moveTo(9f, 17f); lineTo(4f, 12f); lineTo(9f, 7f)
                moveTo(20f, 18f); verticalLineTo(16f)
                arcTo(4f, 4f, 0f, false, false, 16f, 12f)
                horizontalLineTo(4f)
            }
        }
    }

    val Share: ImageVector by lazy {
        lucideIcon("LucideShare") { w ->
            strokePath(w) {
                moveTo(4f, 12f); verticalLineTo(20f)
                arcTo(2f, 2f, 0f, false, false, 6f, 22f)
                horizontalLineTo(18f)
                arcTo(2f, 2f, 0f, false, false, 20f, 20f)
                verticalLineTo(12f)
                moveTo(16f, 6f); lineTo(12f, 2f); lineTo(8f, 6f)
                moveTo(12f, 2f); verticalLineTo(15f)
            }
        }
    }

    val Copy: ImageVector by lazy {
        lucideIcon("LucideCopy") { w ->
            strokePath(w) {
                moveTo(10f, 8f); horizontalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 22f, 10f)
                verticalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 20f, 22f)
                horizontalLineTo(10f)
                arcTo(2f, 2f, 0f, false, true, 8f, 20f)
                verticalLineTo(10f)
                arcTo(2f, 2f, 0f, false, true, 10f, 8f)
                close()
                moveTo(4f, 16f)
                curveTo(2.9f, 16f, 2f, 15.1f, 2f, 14f)
                verticalLineTo(4f)
                curveTo(2f, 2.9f, 2.9f, 2f, 4f, 2f)
                horizontalLineTo(14f)
                curveTo(15.1f, 2f, 16f, 2.9f, 16f, 4f)
            }
        }
    }

    val Sparkles: ImageVector by lazy {
        lucideIcon("LucideSparkles") { w ->
            strokePath(w) {
                moveTo(12f, 3f)
                lineTo(14.1f, 8.9f)
                lineTo(20f, 11f)
                lineTo(14.1f, 13.1f)
                lineTo(12f, 19f)
                lineTo(9.9f, 13.1f)
                lineTo(4f, 11f)
                lineTo(9.9f, 8.9f)
                close()
                moveTo(19f, 3f); verticalLineTo(6f)
                moveTo(17.5f, 4.5f); horizontalLineTo(20.5f)
            }
        }
    }

    val Film: ImageVector by lazy {
        lucideIcon("LucideFilm") { w ->
            strokePath(w) {
                moveTo(4f, 3f); horizontalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 22f, 5f)
                verticalLineTo(19f)
                arcTo(2f, 2f, 0f, false, true, 20f, 21f)
                horizontalLineTo(4f)
                arcTo(2f, 2f, 0f, false, true, 2f, 19f)
                verticalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 4f, 3f)
                close()
                moveTo(7f, 3f); verticalLineTo(21f)
                moveTo(17f, 3f); verticalLineTo(21f)
                moveTo(2f, 12f); horizontalLineTo(22f)
                moveTo(2f, 7.5f); horizontalLineTo(7f)
                moveTo(2f, 16.5f); horizontalLineTo(7f)
                moveTo(17f, 7.5f); horizontalLineTo(22f)
                moveTo(17f, 16.5f); horizontalLineTo(22f)
            }
        }
    }

    val Globe: ImageVector by lazy {
        lucideIcon("LucideGlobe") { w ->
            strokePath(w) {
                moveTo(12f, 2f)
                arcTo(10f, 10f, 0f, true, false, 12f, 22f)
                arcTo(10f, 10f, 0f, true, false, 12f, 2f)
                close()
                moveTo(2f, 12f); horizontalLineTo(22f)
                moveTo(12f, 2f); curveTo(15f, 5.5f, 15f, 18.5f, 12f, 22f)
                moveTo(12f, 2f); curveTo(9f, 5.5f, 9f, 18.5f, 12f, 22f)
            }
        }
    }

    val Eye: ImageVector by lazy {
        lucideIcon("LucideEye") { w ->
            strokePath(w) {
                moveTo(2f, 12f)
                curveTo(4.5f, 7f, 8f, 5f, 12f, 5f)
                curveTo(16f, 5f, 19.5f, 7f, 22f, 12f)
                curveTo(19.5f, 17f, 16f, 19f, 12f, 19f)
                curveTo(8f, 19f, 4.5f, 17f, 2f, 12f)
                close()
                moveTo(12f, 9f)
                arcTo(3f, 3f, 0f, true, false, 12f, 15f)
                arcTo(3f, 3f, 0f, true, false, 12f, 9f)
                close()
            }
        }
    }

    val Maximize: ImageVector by lazy {
        lucideIcon("LucideMaximize") { w ->
            strokePath(w) {
                moveTo(8f, 3f); horizontalLineTo(5f); arcTo(2f, 2f, 0f, false, false, 3f, 5f); verticalLineTo(8f)
                moveTo(21f, 8f); verticalLineTo(5f); arcTo(2f, 2f, 0f, false, false, 19f, 3f); horizontalLineTo(16f)
                moveTo(3f, 16f); verticalLineTo(19f); arcTo(2f, 2f, 0f, false, false, 5f, 21f); horizontalLineTo(8f)
                moveTo(16f, 21f); horizontalLineTo(19f); arcTo(2f, 2f, 0f, false, false, 21f, 19f); verticalLineTo(16f)
            }
        }
    }

    val Gauge: ImageVector by lazy {
        lucideIcon("LucideGauge") { w ->
            strokePath(w) {
                moveTo(12f, 14f); lineTo(16f, 10f)
                moveTo(3.34f, 19f); arcTo(10f, 10f, 0f, true, true, 20.66f, 19f)
            }
        }
    }

    val Check: ImageVector by lazy {
        lucideIcon("LucideCheck") { w ->
            strokePath(w) {
                moveTo(20f, 6f); lineTo(9f, 17f); lineTo(4f, 12f)
            }
        }
    }

    val Info: ImageVector by lazy {
        lucideIcon("LucideInfo") { w ->
            strokePath(w) {
                moveTo(12f, 2f)
                arcTo(10f, 10f, 0f, true, false, 12f, 22f)
                arcTo(10f, 10f, 0f, true, false, 12f, 2f)
                close()
                moveTo(12f, 16f); verticalLineTo(12f)
                moveTo(12f, 8f); horizontalLineTo(12.01f)
            }
        }
    }
}
