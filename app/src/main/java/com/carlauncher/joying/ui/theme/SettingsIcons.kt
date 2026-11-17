package com.carlauncher.joying.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class SettingsIconKind {
    PALETTE, WEATHER, NAVIGATION, MUSIC, GAUGE, MONITOR, GEAR, CHECK
}

/** Iconos de línea dibujados con Canvas (sin dependencia de material-icons). */
@Composable
fun SettingsIcon(
    kind: SettingsIconKind,
    color: Color,
    modifier: Modifier = Modifier.size(28.dp)
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val sw = w * 0.085f
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val center = Offset(w / 2f, h / 2f)

        when (kind) {

            SettingsIconKind.PALETTE -> {
                drawCircle(color, radius = w * 0.42f, center = center, style = stroke)
                listOf(
                    Offset(0.35f, 0.38f),
                    Offset(0.58f, 0.30f),
                    Offset(0.72f, 0.50f),
                    Offset(0.38f, 0.62f)
                ).forEach {
                    drawCircle(color, radius = w * 0.06f, center = Offset(w * it.x, h * it.y))
                }
            }

            SettingsIconKind.WEATHER -> {
                val sun = Offset(w * 0.38f, h * 0.36f)
                drawCircle(color, radius = w * 0.14f, center = sun, style = stroke)
                for (i in 0 until 8) {
                    val a = Math.toRadians(i * 45.0)
                    val r1 = w * 0.22f
                    val r2 = w * 0.30f
                    drawLine(
                        color = color,
                        start = Offset(sun.x + (r1 * cos(a)).toFloat(), sun.y + (r1 * sin(a)).toFloat()),
                        end = Offset(sun.x + (r2 * cos(a)).toFloat(), sun.y + (r2 * sin(a)).toFloat()),
                        strokeWidth = sw,
                        cap = StrokeCap.Round
                    )
                }
                // Nube rellena
                drawCircle(color, radius = w * 0.16f, center = Offset(w * 0.50f, h * 0.68f))
                drawCircle(color, radius = w * 0.20f, center = Offset(w * 0.68f, h * 0.62f))
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.34f, h * 0.66f),
                    size = Size(w * 0.52f, h * 0.18f),
                    cornerRadius = CornerRadius(w * 0.09f)
                )
            }

            SettingsIconKind.NAVIGATION -> {
                val path = Path().apply {
                    moveTo(w * 0.15f, h * 0.48f)
                    lineTo(w * 0.88f, h * 0.14f)
                    lineTo(w * 0.54f, h * 0.88f)
                    lineTo(w * 0.47f, h * 0.55f)
                    close()
                }
                drawPath(path, color)
            }

            SettingsIconKind.MUSIC -> {
                drawCircle(color, radius = w * 0.11f, center = Offset(w * 0.28f, h * 0.76f))
                drawCircle(color, radius = w * 0.11f, center = Offset(w * 0.72f, h * 0.68f))
                drawLine(color, Offset(w * 0.38f, h * 0.76f), Offset(w * 0.38f, h * 0.26f), sw, StrokeCap.Round)
                drawLine(color, Offset(w * 0.82f, h * 0.68f), Offset(w * 0.82f, h * 0.18f), sw, StrokeCap.Round)
                drawLine(color, Offset(w * 0.38f, h * 0.26f), Offset(w * 0.82f, h * 0.18f), sw * 1.6f, StrokeCap.Round)
            }

            SettingsIconKind.GAUGE -> {
                drawArc(
                    color = color,
                    startAngle = 145f,
                    sweepAngle = 250f,
                    useCenter = false,
                    topLeft = Offset(w * 0.10f, h * 0.10f),
                    size = Size(w * 0.80f, h * 0.80f),
                    style = stroke
                )
                drawLine(color, Offset(w * 0.50f, h * 0.56f), Offset(w * 0.72f, h * 0.34f), sw, StrokeCap.Round)
                drawCircle(color, radius = w * 0.06f, center = Offset(w * 0.50f, h * 0.56f))
            }

            SettingsIconKind.MONITOR -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.10f, h * 0.14f),
                    size = Size(w * 0.80f, h * 0.56f),
                    cornerRadius = CornerRadius(w * 0.08f),
                    style = stroke
                )
                drawLine(color, Offset(w * 0.50f, h * 0.70f), Offset(w * 0.50f, h * 0.84f), sw, StrokeCap.Round)
                drawLine(color, Offset(w * 0.30f, h * 0.86f), Offset(w * 0.70f, h * 0.86f), sw, StrokeCap.Round)
            }

            SettingsIconKind.GEAR -> {
                drawCircle(color, radius = w * 0.14f, center = center, style = stroke)
                drawCircle(color, radius = w * 0.30f, center = center, style = stroke)
                for (i in 0 until 8) {
                    val a = Math.toRadians(i * 45.0)
                    val r1 = w * 0.30f
                    val r2 = w * 0.44f
                    drawLine(
                        color = color,
                        start = Offset(center.x + (r1 * cos(a)).toFloat(), center.y + (r1 * sin(a)).toFloat()),
                        end = Offset(center.x + (r2 * cos(a)).toFloat(), center.y + (r2 * sin(a)).toFloat()),
                        strokeWidth = sw * 1.7f,
                        cap = StrokeCap.Round
                    )
                }
            }

            SettingsIconKind.CHECK -> {
                drawCircle(color, radius = w * 0.42f, center = center, style = stroke)
                val check = Path().apply {
                    moveTo(w * 0.30f, h * 0.52f)
                    lineTo(w * 0.45f, h * 0.66f)
                    lineTo(w * 0.72f, h * 0.36f)
                }
                drawPath(check, color, style = stroke)
            }
        }
    }
}
