package com.example.ui.theme

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
 * Compact bundled vector icon set that replaces the 14 MB material-icons-extended
 * dependency, cutting APK size sharply while keeping crisp Material 24dp iconography.
 */
object HKPayIcons {

    private fun vector(
        name: String,
        autoMirror: Boolean = false,
        block: PathBuilder.() -> Unit
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = autoMirror
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.NonZero,
            pathBuilder = block
        )
    }.build()

    val CreditCard: ImageVector by lazy {
        vector("CreditCard") {
            moveTo(20f, 4f); horizontalLineTo(4f)
            curveTo(2.89f, 4f, 2.01f, 4.89f, 2.01f, 6f)
            lineTo(2f, 18f); curveTo(2f, 19.11f, 2.89f, 20f, 4f, 20f)
            horizontalLineTo(20f); curveTo(21.11f, 20f, 22f, 19.11f, 22f, 18f)
            verticalLineTo(6f); curveTo(22f, 4.89f, 21.11f, 4f, 20f, 4f); close()
            moveTo(20f, 18f); horizontalLineTo(4f); verticalLineTo(12f); horizontalLineTo(20f); verticalLineTo(18f); close()
            moveTo(20f, 8f); horizontalLineTo(4f); verticalLineTo(6f); horizontalLineTo(20f); verticalLineTo(8f); close()
        }
    }

    val AddCard: ImageVector by lazy {
        vector("AddCard") {
            moveTo(20f, 4f); horizontalLineTo(4f)
            curveTo(2.89f, 4f, 2.01f, 4.89f, 2.01f, 6f)
            lineTo(2f, 18f); curveTo(2f, 19.11f, 2.89f, 20f, 4f, 20f)
            horizontalLineTo(14f); verticalLineTo(18f); horizontalLineTo(4f)
            verticalLineTo(12f); horizontalLineTo(22f); verticalLineTo(6f)
            curveTo(22f, 4.89f, 21.11f, 4f, 20f, 4f); close()
            moveTo(20f, 8f); horizontalLineTo(4f); verticalLineTo(6f); horizontalLineTo(20f); verticalLineTo(8f); close()
            moveTo(21f, 14f); verticalLineTo(17f); horizontalLineTo(24f); verticalLineTo(19f)
            horizontalLineTo(21f); verticalLineTo(22f); horizontalLineTo(19f)
            verticalLineTo(19f); horizontalLineTo(16f); verticalLineTo(17f)
            horizontalLineTo(19f); verticalLineTo(14f); horizontalLineTo(21f); close()
        }
    }

    val LocalShipping: ImageVector by lazy {
        vector("LocalShipping") {
            moveTo(20f, 8f); horizontalLineTo(17f); verticalLineTo(4f)
            horizontalLineTo(3f); curveTo(1.9f, 4f, 1f, 4.9f, 1f, 6f)
            verticalLineTo(17f); horizontalLineTo(3f)
            curveTo(3f, 18.66f, 4.34f, 20f, 6f, 20f)
            reflectiveCurveTo(9f, 18.66f, 9f, 17f)
            horizontalLineTo(15f); curveTo(15f, 18.66f, 16.34f, 20f, 18f, 20f)
            reflectiveCurveTo(21f, 18.66f, 21f, 17f)
            horizontalLineTo(23f); verticalLineTo(12f); lineTo(20f, 8f); close()
            moveTo(6f, 18.5f); curveTo(5.17f, 18.5f, 4.5f, 17.83f, 4.5f, 17f)
            reflectiveCurveTo(5.17f, 15.5f, 6f, 15.5f)
            reflectiveCurveTo(7.5f, 16.17f, 7.5f, 17f)
            reflectiveCurveTo(6.83f, 18.5f, 6f, 18.5f); close()
            moveTo(19.5f, 9.5f); lineTo(21.46f, 12f); horizontalLineTo(17f); verticalLineTo(9.5f); horizontalLineTo(19.5f); close()
            moveTo(18f, 18.5f); curveTo(17.17f, 18.5f, 16.5f, 17.83f, 16.5f, 17f)
            reflectiveCurveTo(17.17f, 15.5f, 18f, 15.5f)
            reflectiveCurveTo(19.5f, 16.17f, 19.5f, 17f)
            reflectiveCurveTo(18.83f, 18.5f, 18f, 18.5f); close()
        }
    }

    val AccountBalance: ImageVector by lazy {
        vector("AccountBalance") {
            moveTo(4f, 10f); verticalLineTo(17f); horizontalLineTo(7f); verticalLineTo(10f); horizontalLineTo(4f); close()
            moveTo(10.5f, 10f); verticalLineTo(17f); horizontalLineTo(13.5f); verticalLineTo(10f); horizontalLineTo(10.5f); close()
            moveTo(2f, 22f); horizontalLineTo(22f); verticalLineTo(19f); horizontalLineTo(2f); verticalLineTo(22f); close()
            moveTo(17f, 10f); verticalLineTo(17f); horizontalLineTo(20f); verticalLineTo(10f); horizontalLineTo(17f); close()
            moveTo(12f, 2f); lineTo(2f, 7f); verticalLineTo(9f); horizontalLineTo(22f); verticalLineTo(7f); lineTo(12f, 2f); close()
        }
    }

    val Badge: ImageVector by lazy {
        vector("Badge") {
            moveTo(20f, 7f); horizontalLineTo(15f); verticalLineTo(4f)
            curveTo(15f, 2.9f, 14.1f, 2f, 13f, 2f); horizontalLineTo(11f)
            curveTo(9.9f, 2f, 9f, 2.9f, 9f, 4f); verticalLineTo(7f)
            horizontalLineTo(4f); curveTo(2.9f, 7f, 2f, 7.9f, 2f, 9f)
            verticalLineTo(20f); curveTo(2f, 21.1f, 2.9f, 22f, 4f, 22f)
            horizontalLineTo(20f); curveTo(21.1f, 22f, 22f, 21.1f, 22f, 20f)
            verticalLineTo(9f); curveTo(22f, 7.9f, 21.1f, 7f, 20f, 7f); close()
            moveTo(11f, 4f); horizontalLineTo(13f); verticalLineTo(9f); horizontalLineTo(11f); verticalLineTo(4f); close()
            moveTo(12f, 13.5f); curveTo(12f, 14.33f, 11.33f, 15f, 10.5f, 15f)
            reflectiveCurveTo(9f, 14.33f, 9f, 13.5f)
            reflectiveCurveTo(9.67f, 12f, 10.5f, 12f)
            reflectiveCurveTo(12f, 12.67f, 12f, 13.5f); close()
            moveTo(18f, 18f); horizontalLineTo(14f); verticalLineTo(16.5f); horizontalLineTo(18f); verticalLineTo(18f); close()
            moveTo(18f, 15f); horizontalLineTo(14f); verticalLineTo(13.5f); horizontalLineTo(18f); verticalLineTo(15f); close()
        }
    }

    val CalendarMonth: ImageVector by lazy {
        vector("CalendarMonth") {
            moveTo(19f, 4f); horizontalLineTo(18f); verticalLineTo(2f); horizontalLineTo(16f)
            verticalLineTo(4f); horizontalLineTo(8f); verticalLineTo(2f); horizontalLineTo(6f)
            verticalLineTo(4f); horizontalLineTo(5f); curveTo(3.89f, 4f, 3f, 4.9f, 3f, 6f)
            verticalLineTo(20f); curveTo(3f, 21.1f, 3.89f, 22f, 5f, 22f)
            horizontalLineTo(19f); curveTo(20.1f, 22f, 21f, 21.1f, 21f, 20f)
            verticalLineTo(6f); curveTo(21f, 4.9f, 20.1f, 4f, 19f, 4f); close()
            moveTo(19f, 20f); horizontalLineTo(5f); verticalLineTo(10f); horizontalLineTo(19f); verticalLineTo(20f); close()
            moveTo(19f, 8f); horizontalLineTo(5f); verticalLineTo(6f); horizontalLineTo(19f); verticalLineTo(8f); close()
        }
    }

    val CameraAlt: ImageVector by lazy {
        vector("CameraAlt") {
            moveTo(12f, 12f); curveTo(10.35f, 12f, 9f, 13.35f, 9f, 15f)
            reflectiveCurveTo(10.35f, 18f, 12f, 18f)
            reflectiveCurveTo(15f, 16.65f, 15f, 15f)
            reflectiveCurveTo(13.65f, 12f, 12f, 12f); close()
            moveTo(9f, 3f); lineTo(7.17f, 5f); horizontalLineTo(4f)
            curveTo(2.9f, 5f, 2f, 5.9f, 2f, 7f); verticalLineTo(19f)
            curveTo(2f, 20.1f, 2.9f, 21f, 4f, 21f); horizontalLineTo(20f)
            curveTo(21.1f, 21f, 22f, 20.1f, 22f, 19f); verticalLineTo(7f)
            curveTo(22f, 5.9f, 21.1f, 5f, 20f, 5f); horizontalLineTo(16.83f)
            lineTo(15f, 3f); horizontalLineTo(9f); close()
            moveTo(12f, 19.5f); curveTo(9.52f, 19.5f, 7.5f, 17.48f, 7.5f, 15f)
            reflectiveCurveTo(9.52f, 10.5f, 12f, 10.5f)
            reflectiveCurveTo(16.5f, 12.52f, 16.5f, 15f)
            reflectiveCurveTo(14.48f, 19.5f, 12f, 19.5f); close()
        }
    }

    val CameraFront: ImageVector by lazy { CameraAlt }

    val CardGiftcard: ImageVector by lazy {
        vector("CardGiftcard") {
            moveTo(20f, 6f); horizontalLineTo(17.82f)
            curveTo(17.93f, 5.69f, 18f, 5.35f, 18f, 5f)
            curveTo(18f, 3.34f, 16.66f, 2f, 15f, 2f)
            curveTo(13.95f, 2f, 13.04f, 2.54f, 12.5f, 3.35f)
            lineTo(12f, 4.02f); lineTo(11.5f, 3.34f)
            curveTo(10.96f, 2.54f, 10.05f, 2f, 9f, 2f)
            curveTo(7.34f, 2f, 6f, 3.34f, 6f, 5f)
            curveTo(6f, 5.35f, 6.07f, 5.69f, 6.18f, 6f)
            horizontalLineTo(4f); curveTo(2.89f, 6f, 2.01f, 6.89f, 2.01f, 8f)
            lineTo(2f, 19f); curveTo(2f, 20.11f, 2.89f, 21f, 4f, 21f)
            horizontalLineTo(20f); curveTo(21.11f, 21f, 22f, 20.11f, 22f, 19f)
            verticalLineTo(8f); curveTo(22f, 6.89f, 21.11f, 6f, 20f, 6f); close()
            moveTo(15f, 4f); curveTo(15.55f, 4f, 16f, 4.45f, 16f, 5f)
            reflectiveCurveTo(15.55f, 6f, 15f, 6f); horizontalLineTo(13f)
            curveTo(13f, 4.9f, 13.9f, 4f, 15f, 4f); close()
            moveTo(9f, 4f); curveTo(10.1f, 4f, 11f, 4.9f, 11f, 6f)
            horizontalLineTo(9f); curveTo(8.45f, 6f, 8f, 5.55f, 8f, 5f)
            reflectiveCurveTo(8.45f, 4f, 9f, 4f); close()
            moveTo(20f, 19f); horizontalLineTo(4f); verticalLineTo(17f); horizontalLineTo(20f); verticalLineTo(19f); close()
            moveTo(20f, 14f); horizontalLineTo(4f); verticalLineTo(8f); horizontalLineTo(9.08f)
            lineTo(7f, 10.83f); lineTo(8.62f, 12f); lineTo(12f, 7.4f); lineTo(15.38f, 12f)
            lineTo(17f, 10.83f); lineTo(14.92f, 8f); horizontalLineTo(20f); verticalLineTo(14f); close()
        }
    }

    val Contactless: ImageVector by lazy {
        vector("Contactless") {
            moveTo(12f, 2f); curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            reflectiveCurveTo(6.48f, 22f, 12f, 22f)
            reflectiveCurveTo(22f, 17.52f, 22f, 12f)
            reflectiveCurveTo(17.52f, 2f, 12f, 2f); close()
            moveTo(8.46f, 15.54f); lineTo(7.24f, 14.32f)
            curveTo(8.56f, 13f, 8.56f, 11f, 7.24f, 9.68f); lineTo(8.46f, 8.46f)
            curveTo(10.45f, 10.45f, 10.45f, 13.55f, 8.46f, 15.54f); close()
            moveTo(11.3f, 17f); lineTo(10.08f, 15.78f)
            curveTo(12.22f, 13.64f, 12.22f, 10.36f, 10.08f, 8.22f); lineTo(11.3f, 7f)
            curveTo(14.11f, 9.81f, 14.11f, 14.19f, 11.3f, 17f); close()
            moveTo(14.14f, 18.46f); lineTo(12.92f, 17.24f)
            curveTo(15.86f, 14.3f, 15.86f, 9.7f, 12.92f, 6.76f); lineTo(14.14f, 5.54f)
            curveTo(17.75f, 9.15f, 17.75f, 14.85f, 14.14f, 18.46f); close()
        }
    }

    val ContentCopy: ImageVector by lazy {
        vector("ContentCopy") {
            moveTo(16f, 1f); horizontalLineTo(4f); curveTo(2.9f, 1f, 2f, 1.9f, 2f, 3f)
            verticalLineTo(17f); horizontalLineTo(4f); verticalLineTo(3f)
            horizontalLineTo(16f); verticalLineTo(1f); close()
            moveTo(19f, 5f); horizontalLineTo(8f); curveTo(6.9f, 5f, 6f, 5.9f, 6f, 7f)
            verticalLineTo(21f); curveTo(6f, 22.1f, 6.9f, 23f, 8f, 23f)
            horizontalLineTo(19f); curveTo(20.1f, 23f, 21f, 22.1f, 21f, 21f)
            verticalLineTo(7f); curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f); close()
            moveTo(19f, 21f); horizontalLineTo(8f); verticalLineTo(7f); horizontalLineTo(19f); verticalLineTo(21f); close()
        }
    }

    val CurrencyExchange: ImageVector by lazy {
        vector("CurrencyExchange") {
            moveTo(12.89f, 11.1f); curveTo(11.11f, 10.6f, 10.55f, 10.22f, 10.55f, 9.5f)
            curveTo(10.55f, 8.78f, 11.2f, 8.3f, 12.22f, 8.3f)
            curveTo(13.3f, 8.3f, 13.73f, 8.8f, 13.79f, 9.5f); horizontalLineTo(15.2f)
            curveTo(15.14f, 8.35f, 14.43f, 7.45f, 13.11f, 7.12f); verticalLineTo(6f)
            horizontalLineTo(11.33f); verticalLineTo(7.11f)
            curveTo(9.9f, 7.42f, 9f, 8.32f, 9f, 9.54f)
            curveTo(9f, 11f, 10.2f, 11.73f, 12f, 12.18f)
            curveTo(13.6f, 12.57f, 13.94f, 13.11f, 13.94f, 13.72f)
            curveTo(13.94f, 14.2f, 13.55f, 14.88f, 12.22f, 14.88f)
            curveTo(10.95f, 14.88f, 10.45f, 14.3f, 10.37f, 13.5f); horizontalLineTo(8.94f)
            curveTo(9.03f, 14.95f, 10.09f, 15.8f, 11.33f, 16.09f); verticalLineTo(17.2f)
            horizontalLineTo(13.11f); verticalLineTo(16.1f)
            curveTo(14.57f, 15.83f, 15.5f, 14.98f, 15.5f, 13.65f)
            curveTo(15.5f, 12f, 14.08f, 11.43f, 12.89f, 11.1f); close()
            moveTo(20f, 12f); curveTo(20f, 16.41f, 16.41f, 20f, 12f, 20f)
            curveTo(9.21f, 20f, 6.75f, 18.56f, 5.32f, 16.38f); lineTo(7f, 15f)
            horizontalLineTo(2f); verticalLineTo(20f); lineTo(3.8f, 18.2f)
            curveTo(5.65f, 20.5f, 8.64f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f); horizontalLineTo(20f); close()
            moveTo(4f, 12f); curveTo(4f, 7.59f, 7.59f, 4f, 12f, 4f)
            curveTo(14.79f, 4f, 17.25f, 5.44f, 18.68f, 7.62f); lineTo(17f, 9f)
            horizontalLineTo(22f); verticalLineTo(4f); lineTo(20.2f, 5.8f)
            curveTo(18.35f, 3.5f, 15.36f, 2f, 12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f); horizontalLineTo(4f); close()
        }
    }

    val DarkMode: ImageVector by lazy {
        vector("DarkMode") {
            moveTo(12f, 3f); curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
            reflectiveCurveTo(7.03f, 21f, 12f, 21f)
            reflectiveCurveTo(21f, 16.97f, 21f, 12f)
            curveTo(21f, 11.54f, 20.96f, 11.08f, 20.9f, 10.64f)
            curveTo(19.92f, 12.01f, 18.32f, 12.9f, 16.5f, 12.9f)
            curveTo(13.52f, 12.9f, 11.1f, 10.48f, 11.1f, 7.5f)
            curveTo(11.1f, 5.69f, 11.99f, 4.08f, 13.36f, 3.1f)
            curveTo(12.92f, 3.04f, 12.46f, 3f, 12f, 3f); close()
        }
    }

    val LightMode: ImageVector by lazy {
        vector("LightMode") {
            moveTo(12f, 7f); curveTo(9.24f, 7f, 7f, 9.24f, 7f, 12f)
            reflectiveCurveTo(9.24f, 17f, 12f, 17f)
            reflectiveCurveTo(17f, 14.76f, 17f, 12f)
            reflectiveCurveTo(14.76f, 7f, 12f, 7f); close()
            moveTo(12f, 1f); lineTo(12f, 4f); horizontalLineTo(12f); close()
            moveTo(11f, 1f); verticalLineTo(4f); horizontalLineTo(13f); verticalLineTo(1f); close()
            moveTo(11f, 20f); verticalLineTo(23f); horizontalLineTo(13f); verticalLineTo(20f); close()
            moveTo(4f, 11f); horizontalLineTo(1f); verticalLineTo(13f); horizontalLineTo(4f); close()
            moveTo(23f, 11f); horizontalLineTo(20f); verticalLineTo(13f); horizontalLineTo(23f); close()
            moveTo(5.64f, 4.22f); lineTo(3.52f, 6.34f); lineTo(4.93f, 7.75f); lineTo(7.05f, 5.63f); close()
            moveTo(19.07f, 16.25f); lineTo(16.95f, 18.37f); lineTo(18.36f, 19.78f); lineTo(20.48f, 17.66f); close()
            moveTo(18.36f, 4.22f); lineTo(16.24f, 6.34f); lineTo(17.65f, 7.75f); lineTo(19.77f, 5.63f); close()
            moveTo(5.64f, 19.78f); lineTo(7.76f, 17.66f); lineTo(6.35f, 16.25f); lineTo(4.23f, 18.37f); close()
        }
    }

    val NavyTheme: ImageVector by lazy {
        vector("NavyTheme") {
            moveTo(11.1f, 12.08f); curveTo(8.77f, 7.57f, 10.6f, 3.6f, 11.63f, 2.01f)
            curveTo(6.27f, 2.2f, 1.98f, 6.59f, 2f, 12.0f)
            curveTo(2.02f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(16.92f, 22f, 21.02f, 18.43f, 21.84f, 13.73f)
            curveTo(18.8f, 15.1f, 13.5f, 16.7f, 11.1f, 12.08f); close()
            moveTo(18f, 5f); lineTo(18.75f, 6.75f); lineTo(20.5f, 7.5f)
            lineTo(18.75f, 8.25f); lineTo(18f, 10f); lineTo(17.25f, 8.25f)
            lineTo(15.5f, 7.5f); lineTo(17.25f, 6.75f); close()
        }
    }

    val Fingerprint: ImageVector by lazy {
        vector("Fingerprint") {
            moveTo(12f, 2f); curveTo(7.03f, 2f, 3f, 6.03f, 3f, 11f)
            verticalLineTo(14f); horizontalLineTo(5f); verticalLineTo(11f)
            curveTo(5f, 7.13f, 8.13f, 4f, 12f, 4f); reflectiveCurveTo(19f, 7.13f, 19f, 11f)
            verticalLineTo(15f); horizontalLineTo(21f); verticalLineTo(11f)
            curveTo(21f, 6.03f, 16.97f, 2f, 12f, 2f); close()
            moveTo(12f, 6f); curveTo(9.24f, 6f, 7f, 8.24f, 7f, 11f)
            verticalLineTo(17f); horizontalLineTo(9f); verticalLineTo(11f)
            curveTo(9f, 9.34f, 10.34f, 8f, 12f, 8f); reflectiveCurveTo(15f, 9.34f, 15f, 11f)
            verticalLineTo(18f); horizontalLineTo(17f); verticalLineTo(11f)
            curveTo(17f, 8.24f, 14.76f, 6f, 12f, 6f); close()
            moveTo(12f, 10f); curveTo(11.45f, 10f, 11f, 10.45f, 11f, 11f)
            verticalLineTo(21f); horizontalLineTo(13f); verticalLineTo(11f)
            curveTo(13f, 10.45f, 12.55f, 10f, 12f, 10f); close()
        }
    }

    val GroupAdd: ImageVector by lazy {
        vector("GroupAdd") {
            moveTo(15f, 12f); curveTo(17.21f, 12f, 19f, 10.21f, 19f, 8f)
            reflectiveCurveTo(17.21f, 4f, 15f, 4f); reflectiveCurveTo(11f, 5.79f, 11f, 8f)
            reflectiveCurveTo(12.79f, 12f, 15f, 12f); close()
            moveTo(6f, 10f); verticalLineTo(7f); horizontalLineTo(4f); verticalLineTo(10f)
            horizontalLineTo(1f); verticalLineTo(12f); horizontalLineTo(4f); verticalLineTo(15f)
            horizontalLineTo(6f); verticalLineTo(12f); horizontalLineTo(9f); verticalLineTo(10f); horizontalLineTo(6f); close()
            moveTo(15f, 14f); curveTo(12.33f, 14f, 7f, 15.34f, 7f, 18f)
            verticalLineTo(20f); horizontalLineTo(23f); verticalLineTo(18f)
            curveTo(23f, 15.34f, 17.67f, 14f, 15f, 14f); close()
        }
    }

    val LockReset: ImageVector by lazy {
        vector("LockReset") {
            moveTo(13f, 3f); curveTo(8.03f, 3f, 4f, 7.03f, 4f, 12f)
            horizontalLineTo(1f); lineTo(4.89f, 15.89f); lineTo(4.96f, 16.03f)
            lineTo(9f, 12f); horizontalLineTo(6f); curveTo(6f, 8.13f, 9.13f, 5f, 13f, 5f)
            reflectiveCurveTo(20f, 8.13f, 20f, 12f); reflectiveCurveTo(16.87f, 19f, 13f, 19f)
            curveTo(11.07f, 19f, 9.32f, 18.21f, 8.06f, 16.94f); lineTo(6.64f, 18.36f)
            curveTo(8.27f, 19.99f, 10.51f, 21f, 13f, 21f)
            curveTo(17.97f, 21f, 22f, 16.97f, 22f, 12f); reflectiveCurveTo(17.97f, 3f, 13f, 3f); close()
            moveTo(15f, 11f); verticalLineTo(10f); curveTo(15f, 8.9f, 14.1f, 8f, 13f, 8f)
            reflectiveCurveTo(11f, 8.9f, 11f, 10f); verticalLineTo(11f)
            horizontalLineTo(10f); verticalLineTo(16f); horizontalLineTo(16f); verticalLineTo(11f); horizontalLineTo(15f); close()
        }
    }

    val Logout: ImageVector by lazy {
        vector("Logout", autoMirror = true) {
            moveTo(17f, 7f); lineTo(15.59f, 8.41f); lineTo(18.17f, 11f)
            horizontalLineTo(8f); verticalLineTo(13f); horizontalLineTo(18.17f)
            lineTo(15.59f, 15.58f); lineTo(17f, 17f); lineTo(22f, 12f); close()
            moveTo(4f, 5f); horizontalLineTo(12f); verticalLineTo(3f); horizontalLineTo(4f)
            curveTo(2.9f, 3f, 2f, 3.9f, 2f, 5f); verticalLineTo(19f)
            curveTo(2f, 20.1f, 2.9f, 21f, 4f, 21f); horizontalLineTo(12f)
            verticalLineTo(19f); horizontalLineTo(4f); verticalLineTo(5f); close()
        }
    }

    val MarkEmailRead: ImageVector by lazy {
        vector("MarkEmailRead") {
            moveTo(12f, 19f); curveTo(12f, 15.13f, 15.13f, 12f, 19f, 12f)
            curveTo(20.08f, 12f, 21.09f, 12.25f, 22f, 12.68f); verticalLineTo(6f)
            curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f); horizontalLineTo(4f)
            curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f); verticalLineTo(18f)
            curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f); horizontalLineTo(12.08f)
            curveTo(12.03f, 19.67f, 12f, 19.34f, 12f, 19f); close()
            moveTo(4f, 6f); lineTo(12f, 11f); lineTo(20f, 6f); verticalLineTo(8f)
            lineTo(12f, 13f); lineTo(4f, 8f); verticalLineTo(6f); close()
            moveTo(17.34f, 22f); lineTo(13.8f, 18.46f); lineTo(15.21f, 17.05f)
            lineTo(17.33f, 19.17f); lineTo(21.57f, 14.93f); lineTo(23f, 16.34f); close()
        }
    }

    val NotificationsActive: ImageVector by lazy {
        vector("NotificationsActive") {
            moveTo(12f, 22f); curveTo(13.1f, 22f, 14f, 21.1f, 14f, 20f)
            horizontalLineTo(10f); curveTo(10f, 21.1f, 10.9f, 22f, 12f, 22f); close()
            moveTo(18f, 16f); verticalLineTo(11f)
            curveTo(18f, 7.93f, 16.36f, 5.36f, 13.5f, 4.68f); verticalLineTo(4f)
            curveTo(13.5f, 3.17f, 12.83f, 2.5f, 12f, 2.5f)
            reflectiveCurveTo(10.5f, 3.17f, 10.5f, 4f); verticalLineTo(4.68f)
            curveTo(7.63f, 5.36f, 6f, 7.92f, 6f, 11f); verticalLineTo(16f)
            lineTo(4f, 18f); verticalLineTo(19f); horizontalLineTo(20f); verticalLineTo(18f); lineTo(18f, 16f); close()
        }
    }

    val OpenInNew: ImageVector by lazy {
        vector("OpenInNew", autoMirror = true) {
            moveTo(19f, 19f); horizontalLineTo(5f); verticalLineTo(5f); horizontalLineTo(12f)
            verticalLineTo(3f); horizontalLineTo(5f); curveTo(3.89f, 3f, 3f, 3.9f, 3f, 5f)
            verticalLineTo(19f); curveTo(3f, 20.1f, 3.89f, 21f, 5f, 21f); horizontalLineTo(19f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f); verticalLineTo(12f)
            horizontalLineTo(19f); verticalLineTo(19f); close()
            moveTo(14f, 3f); verticalLineTo(5f); horizontalLineTo(17.59f)
            lineTo(7.76f, 14.83f); lineTo(9.17f, 16.24f); lineTo(19f, 6.41f)
            verticalLineTo(10f); horizontalLineTo(21f); verticalLineTo(3f); horizontalLineTo(14f); close()
        }
    }

    val Palette: ImageVector by lazy {
        vector("Palette") {
            moveTo(12f, 3f); curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
            curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f)
            curveTo(12.83f, 21f, 13.5f, 20.33f, 13.5f, 19.5f)
            curveTo(13.5f, 19.11f, 13.35f, 18.76f, 13.11f, 18.49f)
            curveTo(12.88f, 18.23f, 12.73f, 17.88f, 12.73f, 17.5f)
            curveTo(12.73f, 16.67f, 13.4f, 16f, 14.23f, 16f); horizontalLineTo(16f)
            curveTo(19.31f, 16f, 22f, 13.31f, 22f, 10f)
            curveTo(22f, 6.13f, 17.52f, 3f, 12f, 3f); close()
            moveTo(6.5f, 12f); curveTo(5.67f, 12f, 5f, 11.33f, 5f, 10.5f); reflectiveCurveTo(5.67f, 9f, 6.5f, 9f); reflectiveCurveTo(8f, 9.67f, 8f, 10.5f); reflectiveCurveTo(7.33f, 12f, 6.5f, 12f); close()
            moveTo(9.5f, 8f); curveTo(8.67f, 8f, 8f, 7.33f, 8f, 6.5f); reflectiveCurveTo(8.67f, 5f, 9.5f, 5f); reflectiveCurveTo(11f, 5.67f, 11f, 6.5f); reflectiveCurveTo(10.33f, 8f, 9.5f, 8f); close()
            moveTo(14.5f, 8f); curveTo(13.67f, 8f, 13f, 7.33f, 13f, 6.5f); reflectiveCurveTo(13.67f, 5f, 14.5f, 5f); reflectiveCurveTo(16f, 5.67f, 16f, 6.5f); reflectiveCurveTo(15.33f, 8f, 14.5f, 8f); close()
            moveTo(17.5f, 12f); curveTo(16.67f, 12f, 16f, 11.33f, 16f, 10.5f); reflectiveCurveTo(16.67f, 9f, 17.5f, 9f); reflectiveCurveTo(19f, 9.67f, 19f, 10.5f); reflectiveCurveTo(18.33f, 12f, 17.5f, 12f); close()
        }
    }

    val Payment: ImageVector by lazy { CreditCard }

    val PlayLesson: ImageVector by lazy {
        vector("PlayLesson") {
            moveTo(18f, 11f); curveTo(14.69f, 11f, 12f, 13.69f, 12f, 17f)
            reflectiveCurveTo(14.69f, 23f, 18f, 23f)
            reflectiveCurveTo(24f, 20.31f, 24f, 17f)
            reflectiveCurveTo(21.31f, 11f, 18f, 11f); close()
            moveTo(16.5f, 20f); verticalLineTo(14f); lineTo(21f, 17f); lineTo(16.5f, 20f); close()
            moveTo(16f, 2f); horizontalLineTo(4f); curveTo(2.9f, 2f, 2f, 2.9f, 2f, 4f)
            verticalLineTo(20f); curveTo(2f, 21.1f, 2.9f, 22f, 4f, 22f)
            horizontalLineTo(11.3f); curveTo(10.5f, 20.6f, 10f, 18.85f, 10f, 17f)
            curveTo(10f, 12.58f, 13.58f, 9f, 18f, 9f)
            verticalLineTo(4f); curveTo(18f, 2.9f, 17.1f, 2f, 16f, 2f); close()
            moveTo(6f, 4f); horizontalLineTo(11f); verticalLineTo(11f); lineTo(8.5f, 9.5f); lineTo(6f, 11f); verticalLineTo(4f); close()
        }
    }

    val RadioButtonUnchecked: ImageVector by lazy {
        vector("RadioButtonUnchecked") {
            moveTo(12f, 2f); curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            reflectiveCurveTo(6.48f, 22f, 12f, 22f)
            reflectiveCurveTo(22f, 17.52f, 22f, 12f)
            reflectiveCurveTo(17.52f, 2f, 12f, 2f); close()
            moveTo(12f, 20f); curveTo(7.58f, 20f, 4f, 16.42f, 4f, 12f)
            reflectiveCurveTo(7.58f, 4f, 12f, 4f)
            reflectiveCurveTo(20f, 7.58f, 20f, 12f)
            reflectiveCurveTo(16.42f, 20f, 12f, 20f); close()
        }
    }

    val ReceiptLong: ImageVector by lazy {
        vector("ReceiptLong", autoMirror = true) {
            moveTo(19.5f, 3.5f); lineTo(18f, 2f); lineTo(16.5f, 3.5f); lineTo(15f, 2f)
            lineTo(13.5f, 3.5f); lineTo(12f, 2f); lineTo(10.5f, 3.5f); lineTo(9f, 2f)
            lineTo(7.5f, 3.5f); lineTo(6f, 2f); verticalLineTo(16f); horizontalLineTo(3f)
            verticalLineTo(19f); curveTo(3f, 20.66f, 4.34f, 22f, 6f, 22f)
            horizontalLineTo(18f); curveTo(19.66f, 22f, 21f, 20.66f, 21f, 19f)
            verticalLineTo(2f); lineTo(19.5f, 3.5f); close()
            moveTo(15f, 20f); horizontalLineTo(6f); curveTo(5.45f, 20f, 5f, 19.55f, 5f, 19f)
            verticalLineTo(18f); horizontalLineTo(15f); verticalLineTo(20f); close()
            moveTo(19f, 19f); curveTo(19f, 19.55f, 18.55f, 20f, 18f, 20f)
            reflectiveCurveTo(17f, 19.55f, 17f, 19f); verticalLineTo(16f); horizontalLineTo(8f)
            verticalLineTo(5f); horizontalLineTo(19f); verticalLineTo(19f); close()
            moveTo(9f, 7f); horizontalLineTo(15f); verticalLineTo(9f); horizontalLineTo(9f); verticalLineTo(7f); close()
            moveTo(9f, 10f); horizontalLineTo(15f); verticalLineTo(12f); horizontalLineTo(9f); verticalLineTo(10f); close()
        }
    }

    val Shield: ImageVector by lazy {
        vector("Shield") {
            moveTo(12f, 2f); lineTo(4f, 5f); verticalLineTo(11.09f)
            curveTo(4f, 16.14f, 7.41f, 20.85f, 12f, 22f)
            curveTo(16.59f, 20.85f, 20f, 16.14f, 20f, 11.09f)
            verticalLineTo(5f); lineTo(12f, 2f); close()
        }
    }

    val Security: ImageVector by lazy {
        vector("Security") {
            moveTo(12f, 1f); lineTo(3f, 5f); verticalLineTo(11f)
            curveTo(3f, 16.55f, 6.84f, 21.74f, 12f, 23f)
            curveTo(17.16f, 21.74f, 21f, 16.55f, 21f, 11f)
            verticalLineTo(5f); lineTo(12f, 1f); close()
            moveTo(12f, 11.99f); horizontalLineTo(19f)
            curveTo(18.47f, 16.11f, 15.72f, 19.78f, 12f, 20.93f)
            verticalLineTo(12f); horizontalLineTo(5f); verticalLineTo(6.3f)
            lineTo(12f, 3.19f); verticalLineTo(11.99f); close()
        }
    }

    val Speed: ImageVector by lazy {
        vector("Speed") {
            moveTo(20.38f, 8.57f); lineTo(19.15f, 10.42f)
            curveTo(19.69f, 11.49f, 20f, 12.7f, 20f, 14f)
            curveTo(20f, 15.13f, 19.76f, 16.21f, 19.34f, 17.19f); horizontalLineTo(4.66f)
            curveTo(4.24f, 16.21f, 4f, 15.13f, 4f, 14f)
            curveTo(4f, 9.58f, 7.58f, 6f, 12f, 6f)
            curveTo(13.29f, 6f, 14.51f, 6.31f, 15.58f, 6.85f); lineTo(17.43f, 5.62f)
            curveTo(15.86f, 4.59f, 14f, 4f, 12f, 4f)
            curveTo(6.48f, 4f, 2f, 8.48f, 2f, 14f)
            curveTo(2f, 16.21f, 2.72f, 18.25f, 3.94f, 19.9f); horizontalLineTo(20.06f)
            curveTo(21.28f, 18.25f, 22f, 16.21f, 22f, 14f)
            curveTo(22f, 12f, 21.41f, 10.14f, 20.38f, 8.57f); close()
            moveTo(10.59f, 15.41f); curveTo(11.37f, 16.19f, 12.63f, 16.19f, 13.41f, 15.41f)
            lineTo(19.07f, 6.93f); lineTo(10.59f, 12.59f)
            curveTo(9.81f, 13.37f, 9.81f, 14.63f, 10.59f, 15.41f); close()
        }
    }

    val Storefront: ImageVector by lazy {
        vector("Storefront") {
            moveTo(21.9f, 8.89f); lineTo(20.85f, 4.16f)
            curveTo(20.66f, 3.48f, 20.04f, 3f, 19.34f, 3f); horizontalLineTo(4.66f)
            curveTo(3.96f, 3f, 3.34f, 3.48f, 3.15f, 4.16f); lineTo(2.1f, 8.89f)
            curveTo(1.88f, 9.88f, 2.41f, 10.88f, 3.31f, 11.27f); verticalLineTo(19f)
            curveTo(3.31f, 20.1f, 4.21f, 21f, 5.31f, 21f); horizontalLineTo(18.69f)
            curveTo(19.79f, 21f, 20.69f, 20.1f, 20.69f, 19f); verticalLineTo(11.27f)
            curveTo(21.59f, 10.88f, 22.12f, 9.88f, 21.9f, 8.89f); close()
            moveTo(18.69f, 19f); horizontalLineTo(5.31f); verticalLineTo(11.5f)
            horizontalLineTo(18.69f); verticalLineTo(19f); close()
        }
    }

    val SupportAgent: ImageVector by lazy {
        vector("SupportAgent") {
            moveTo(21f, 12.22f); curveTo(21f, 6.73f, 16.74f, 3f, 12f, 3f)
            curveTo(7.31f, 3f, 3f, 6.65f, 3f, 12.28f)
            curveTo(2.4f, 12.62f, 2f, 13.26f, 2f, 14f); verticalLineTo(16f)
            curveTo(2f, 17.1f, 2.9f, 18f, 4f, 18f); horizontalLineTo(5f)
            verticalLineTo(11.9f); curveTo(5f, 8.03f, 8.13f, 4.9f, 12f, 4.9f)
            reflectiveCurveTo(19f, 8.03f, 19f, 11.9f); verticalLineTo(19f)
            horizontalLineTo(11f); verticalLineTo(21f); horizontalLineTo(19f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f); verticalLineTo(17.78f)
            curveTo(21.59f, 17.45f, 22f, 16.83f, 22f, 16.1f); verticalLineTo(13.9f)
            curveTo(22f, 13.17f, 21.59f, 12.55f, 21f, 12.22f); close()
            moveTo(9f, 14f); curveTo(9.55f, 14f, 10f, 13.55f, 10f, 13f)
            reflectiveCurveTo(9.55f, 12f, 9f, 12f); reflectiveCurveTo(8f, 12.45f, 8f, 13f)
            reflectiveCurveTo(8.45f, 14f, 9f, 14f); close()
            moveTo(15f, 14f); curveTo(15.55f, 14f, 16f, 13.55f, 16f, 13f)
            reflectiveCurveTo(15.55f, 12f, 15f, 12f); reflectiveCurveTo(14f, 12.45f, 14f, 13f)
            reflectiveCurveTo(14.45f, 14f, 15f, 14f); close()
        }
    }

    val UploadFile: ImageVector by lazy {
        vector("UploadFile") {
            moveTo(14f, 2f); horizontalLineTo(6f); curveTo(4.9f, 2f, 4.01f, 2.9f, 4.01f, 4f)
            lineTo(4f, 20f); curveTo(4f, 21.1f, 4.89f, 22f, 5.99f, 22f)
            horizontalLineTo(18f); curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
            verticalLineTo(8f); lineTo(14f, 2f); close()
            moveTo(18f, 20f); horizontalLineTo(6f); verticalLineTo(4f); horizontalLineTo(13f)
            verticalLineTo(9f); horizontalLineTo(18f); verticalLineTo(20f); close()
            moveTo(8f, 15.01f); lineTo(9.41f, 16.42f); lineTo(11f, 14.84f)
            verticalLineTo(19f); horizontalLineTo(13f); verticalLineTo(14.84f)
            lineTo(14.59f, 16.43f); lineTo(16f, 15.01f); lineTo(12.01f, 11f); close()
        }
    }

    val VerifiedUser: ImageVector by lazy {
        vector("VerifiedUser") {
            moveTo(12f, 1f); lineTo(3f, 5f); verticalLineTo(11f)
            curveTo(3f, 16.55f, 6.84f, 21.74f, 12f, 23f)
            curveTo(17.16f, 21.74f, 21f, 16.55f, 21f, 11f)
            verticalLineTo(5f); lineTo(12f, 1f); close()
            moveTo(10f, 17f); lineTo(6f, 13f); lineTo(7.41f, 11.59f)
            lineTo(10f, 14.17f); lineTo(16.59f, 7.58f); lineTo(18f, 9f); close()
        }
    }

    val Visibility: ImageVector by lazy {
        vector("Visibility") {
            moveTo(12f, 4.5f); curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
            curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
            reflectiveCurveTo(21.27f, 16.39f, 23f, 12f)
            curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f); close()
            moveTo(12f, 17f); curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
            reflectiveCurveTo(9.24f, 7f, 12f, 7f)
            reflectiveCurveTo(17f, 9.24f, 17f, 12f)
            reflectiveCurveTo(14.76f, 17f, 12f, 17f); close()
            moveTo(12f, 9f); curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
            reflectiveCurveTo(10.34f, 15f, 12f, 15f)
            reflectiveCurveTo(15f, 13.66f, 15f, 12f)
            reflectiveCurveTo(13.66f, 9f, 12f, 9f); close()
        }
    }

    val VisibilityOff: ImageVector by lazy {
        vector("VisibilityOff") {
            moveTo(12f, 7f); curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
            curveTo(17f, 12.65f, 16.87f, 13.26f, 16.64f, 13.83f); lineTo(19.56f, 16.75f)
            curveTo(21.07f, 15.49f, 22.26f, 13.86f, 23f, 12f)
            curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
            curveTo(10.6f, 4.5f, 9.26f, 4.75f, 8.02f, 5.2f); lineTo(10.17f, 7.35f)
            curveTo(10.74f, 7.13f, 11.35f, 7f, 12f, 7f); close()
            moveTo(2f, 4.27f); lineTo(4.28f, 6.55f); lineTo(4.74f, 7.01f)
            curveTo(3.08f, 8.3f, 1.78f, 10.02f, 1f, 12f)
            curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
            curveTo(13.55f, 19.5f, 15.03f, 19.2f, 16.38f, 18.66f); lineTo(16.8f, 19.08f)
            lineTo(19.73f, 22f); lineTo(21f, 20.73f); lineTo(3.27f, 3f); lineTo(2f, 4.27f); close()
            moveTo(7.53f, 9.8f); lineTo(9.08f, 11.35f); curveTo(9.03f, 11.56f, 9f, 11.78f, 9f, 12f)
            curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f); curveTo(12.22f, 15f, 12.44f, 14.97f, 12.65f, 14.92f)
            lineTo(14.2f, 16.47f); curveTo(13.53f, 16.8f, 12.79f, 17f, 12f, 17f)
            curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
            curveTo(7f, 11.21f, 7.2f, 10.47f, 7.53f, 9.8f); close()
        }
    }

    val Phone: ImageVector by lazy {
        vector("Phone") {
            moveTo(6.62f, 10.79f); curveTo(8.06f, 13.62f, 10.38f, 15.94f, 13.21f, 17.38f)
            lineTo(15.41f, 15.18f); curveTo(15.68f, 14.91f, 16.08f, 14.82f, 16.43f, 14.94f)
            curveTo(17.55f, 15.31f, 18.76f, 15.51f, 20f, 15.51f)
            curveTo(20.55f, 15.51f, 21f, 15.96f, 21f, 16.51f); verticalLineTo(20f)
            curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
            curveTo(10.61f, 21f, 3f, 13.39f, 3f, 4f)
            curveTo(3f, 3.45f, 3.45f, 3f, 4f, 3f); horizontalLineTo(7.5f)
            curveTo(8.05f, 3f, 8.5f, 3.45f, 8.5f, 4f)
            curveTo(8.5f, 5.25f, 8.7f, 6.45f, 9.07f, 7.57f)
            curveTo(9.18f, 7.92f, 9.1f, 8.31f, 8.82f, 8.59f); lineTo(6.62f, 10.79f); close()
        }
    }

    val PersonAdd: ImageVector by lazy { GroupAdd }
    val Refresh: ImageVector by lazy { LockReset }
    val FaceCheck: ImageVector by lazy {
        vector("FaceCheck") {
            moveTo(9f, 11.75f); curveTo(8.31f, 11.75f, 7.75f, 12.31f, 7.75f, 13f)
            reflectiveCurveTo(8.31f, 14.25f, 9f, 14.25f); reflectiveCurveTo(10.25f, 13.69f, 10.25f, 13f)
            reflectiveCurveTo(9.69f, 11.75f, 9f, 11.75f); close()
            moveTo(15f, 11.75f); curveTo(14.31f, 11.75f, 13.75f, 12.31f, 13.75f, 13f)
            reflectiveCurveTo(14.31f, 14.25f, 15f, 14.25f); reflectiveCurveTo(16.25f, 13.69f, 16.25f, 13f)
            reflectiveCurveTo(15.69f, 11.75f, 15f, 11.75f); close()
            moveTo(12f, 2f); curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            reflectiveCurveTo(6.48f, 22f, 12f, 22f)
            reflectiveCurveTo(22f, 17.52f, 22f, 12f)
            reflectiveCurveTo(17.52f, 2f, 12f, 2f); close()
            moveTo(12f, 20f); curveTo(7.59f, 20f, 4f, 16.41f, 4f, 12f)
            curveTo(4f, 11.71f, 4.02f, 11.42f, 4.05f, 11.14f)
            curveTo(6.41f, 10.09f, 8.28f, 8.16f, 9.26f, 5.77f)
            curveTo(11.07f, 8.33f, 14.05f, 10f, 17.42f, 10f)
            curveTo(18.2f, 10f, 18.95f, 9.91f, 19.67f, 9.74f)
            curveTo(19.88f, 10.45f, 20f, 11.21f, 20f, 12f)
            curveTo(20f, 16.41f, 16.41f, 20f, 12f, 20f); close()
        }
    }
}
