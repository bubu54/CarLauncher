package com.carlauncher.joying.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Tipo de conexión que muestra el indicador de la barra superior. */
enum class ConnectionType {
    NONE,      // sin conexión a Internet
    WIFI,
    CELLULAR,  // datos móviles (4G)
    OTHER      // Ethernet u otro transporte con Internet
}

private val NoConnectionRed = Color(0xFFFF7070)

/**
 * Indicador único de conectividad:
 *  - CELLULAR -> texto con la generación ("2G", "3G", "4G", "5G")
 *  - WIFI     -> icono wifi
 *  - OTHER    -> globo terráqueo
 *  - NONE     -> globo terráqueo tachado
 */
@Composable
fun ConnectivityIcon(
    type: ConnectionType,
    modifier: Modifier = Modifier,
    cellularLabel: String = "4G"
) {
    Box(
        modifier = modifier.size(width = 30.dp, height = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            ConnectionType.CELLULAR -> Text(
                text = cellularLabel,
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            ConnectionType.WIFI -> Canvas(Modifier.size(width = 22.dp, height = 16.dp)) {
                drawWifi(White)
            }

            ConnectionType.OTHER -> Canvas(Modifier.size(19.dp)) {
                drawGlobe(White)
            }

            ConnectionType.NONE -> Canvas(Modifier.size(19.dp)) {
                drawGlobe(Secondary, slashColor = NoConnectionRed)
            }
        }
    }
}

private fun DrawScope.drawWifi(color: Color) {
    val strokeWidth = size.height * 0.14f
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

    val cx = size.width / 2f
    val baseY = size.height - strokeWidth

    // Tres arcos concéntricos (de dentro hacia fuera)
    listOf(0.38f, 0.68f, 0.98f).forEach { factor ->
        val r = size.height * factor
        drawArc(
            color = color,
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - r, baseY - r),
            size = Size(r * 2f, r * 2f),
            style = stroke
        )
    }

    // Punto inferior
    drawCircle(
        color = color,
        radius = strokeWidth * 0.8f,
        center = Offset(cx, baseY)
    )
}

private fun DrawScope.drawGlobe(color: Color, slashColor: Color? = null) {
    val strokeWidth = size.minDimension * 0.09f
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension / 2f - strokeWidth

    // Círculo, meridiano y ecuador
    drawCircle(color = color, radius = r, center = Offset(cx, cy), style = stroke)
    drawOval(
        color = color,
        topLeft = Offset(cx - r * 0.45f, cy - r),
        size = Size(r * 0.9f, r * 2f),
        style = stroke
    )
    drawLine(
        color = color,
        start = Offset(cx - r, cy),
        end = Offset(cx + r, cy),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Tachado diagonal (con "hueco" del color del fondo para que destaque)
    if (slashColor != null) {
        val start = Offset(size.width * 0.10f, size.height * 0.10f)
        val end = Offset(size.width * 0.90f, size.height * 0.90f)

        drawLine(
            color = Background,
            start = start,
            end = end,
            strokeWidth = strokeWidth * 3.2f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = slashColor,
            start = start,
            end = end,
            strokeWidth = strokeWidth * 1.4f,
            cap = StrokeCap.Round
        )
    }
}
